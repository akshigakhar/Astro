package com.astro.kundali.accounts

import android.app.Application
import android.content.Context
import androidx.credentials.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.astro.kundali.BuildConfig
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.*
import com.google.firebase.firestore.*
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await

class AccountViewModel(app: Application) : AndroidViewModel(app) {
    val repository = CloudRepository(app, viewModelScope)

    override fun onCleared() {
        repository.close()
        super.onCleared()
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class CloudRepository(context: Context, private val scope: CoroutineScope) {
    val configured = BuildConfig.FIREBASE_CONFIGURED
    private val appContext = context.applicationContext
    private val cache =
        Room.databaseBuilder(appContext, KundaliCache::class.java, "saved-kundalis.db").build()
    private val dao = cache.kundalis()
    private val ownerPrefs =
        appContext.getSharedPreferences("kundali_cache_owner", Context.MODE_PRIVATE)
    private val app =
        if (configured)
            FirebaseApp.getApps(appContext).firstOrNull { it.name == FirebaseApp.DEFAULT_APP_NAME }
                ?: FirebaseApp.initializeApp(appContext)
        else null
    private val auth = app?.let { FirebaseAuth.getInstance(it) }
    private val db = app?.let { CloudFirestore.instance(it) }
    private val _session = MutableStateFlow<AccountSession?>(null)
    val session: StateFlow<AccountSession?> = _session.asStateFlow()
    private val _notice =
        MutableStateFlow(
            if (configured) ""
            else
                "Guest-only build: Firebase is not configured. Calculation and lessons work offline; account features need the owner's Firebase setup."
        )
    val notice: StateFlow<String> = _notice.asStateFlow()
    private val _count = MutableStateFlow(0)
    val count: StateFlow<Int> = _count.asStateFlow()
    val saved =
        session
            .flatMapLatest { user ->
                if (user == null) flowOf(emptyList())
                else dao.observe(user.uid).map { rows -> rows.map { it.saved() } }
            }
            .stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())
    private val generation = AtomicInteger(0)
    private val cacheMutex = Mutex()
    private var savedListener: ListenerRegistration? = null
    private var profileListener: ListenerRegistration? = null
    private val authListener =
        FirebaseAuth.AuthStateListener { service ->
            val g = generation.incrementAndGet()
            savedListener?.remove()
            profileListener?.remove()
            scope.launch { activate(service.currentUser, g) }
        }

    init {
        if (auth != null) auth.addAuthStateListener(authListener)
        else
            scope.launch {
                cacheMutex.withLock {
                    dao.clear()
                    ownerPrefs.edit().clear().apply()
                }
            }
    }

    private fun service() =
        auth ?: error("Firebase is not configured in this build. Continue as guest.")

    private fun database() = db ?: error("Firebase is not configured in this build.")

    private fun requireSession() = session.value ?: error("Sign in to save kundalis.")

    private suspend fun activate(user: FirebaseUser?, g: Int) {
        cacheMutex.withLock {
            if (g != generation.get()) return
            val uid = user?.uid
            if (uid == null || ownerPrefs.getString("uid", null) != uid) dao.clear()
            ownerPrefs.edit().apply { if (uid == null) clear() else putString("uid", uid) }.apply()
            _session.value =
                user?.let {
                    AccountSession(
                        it.uid,
                        it.email.orEmpty(),
                        it.displayName.orEmpty(),
                        provider =
                            if (it.providerData.any { p -> p.providerId == "google.com" })
                                "google.com"
                            else "password",
                    )
                }
            _count.value = 0
        }
        if (user == null || g != generation.get()) return
        try {
            val token = user.getIdToken(false).await()
            if (g != generation.get()) return
            val provider =
                (token.claims["firebase"] as? Map<*, *>)?.get("sign_in_provider") as? String ?: ""
            val claimed = token.claims["admin"] == true
            if (claimed && provider != "google.com") {
                signOut()
                _notice.value = "The owner/admin account must sign in with Google."
                return
            }
            val ownerConfig =
                if (claimed) database().document("config/admin").get(Source.SERVER).await()
                else null
            val admin =
                claimed &&
                    ownerConfig?.getString("uid") == user.uid &&
                    ownerConfig.getBoolean("disabled") != true
            check(!claimed || admin) {
                "This admin claim does not match the configured owner. Contact the Firebase project owner."
            }
            if (g != generation.get()) return
            _session.value =
                AccountSession(
                    user.uid,
                    user.email.orEmpty(),
                    user.displayName.orEmpty(),
                    admin,
                    provider,
                )
            runCatching { ensureProfile(user) }
                .onFailure {
                    if (g == generation.get())
                        _notice.value =
                            "Offline or unable to sync. Cached kundalis remain available; connect to change saved data."
                }
            if (g != generation.get()) return
            profileListener =
                database().document("users/${user.uid}").addSnapshotListener { snap, error ->
                    if (g == generation.get()) {
                        if (error != null)
                            _notice.value =
                                "Unable to sync account. Check connection and deployed Firestore rules."
                        else if (
                            snap != null &&
                                !snap.metadata.isFromCache &&
                                !snap.metadata.hasPendingWrites()
                        )
                            _count.value = (snap.getLong("kundaliCount") ?: 0).toInt()
                    }
                }
            savedListener =
                database().collection("users/${user.uid}/kundalis").addSnapshotListener {
                    snap,
                    error ->
                    if (g == generation.get()) {
                        if (error != null)
                            _notice.value =
                                "Unable to sync saved charts. The on-device cache is still available."
                        else if (
                            snap != null &&
                                !snap.metadata.isFromCache &&
                                !snap.metadata.hasPendingWrites()
                        )
                            scope.launch {
                                cacheMutex.withLock {
                                    if (g == generation.get() && session.value?.uid == user.uid) {
                                        dao.replace(
                                            user.uid,
                                            snap.documents
                                                .mapNotNull { decode(user.uid, it) }
                                                .map { CachedKundali.from(it) },
                                        )
                                        _notice.value =
                                            "Synced. Saved charts can be opened offline; changes require a connection."
                                    }
                                }
                            }
                    }
                }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            if (g == generation.get())
                _notice.value =
                    if (e.message?.contains("admin claim") == true) e.message.orEmpty()
                    else
                        "Offline or unable to finish account sync. Cached personal charts remain available; admin access requires online owner verification."
        }
    }

    private suspend fun ensureProfile(user: FirebaseUser) {
        val ref = database().document("users/${user.uid}")
        database()
            .runTransaction { tx ->
                val current = tx.get(ref)
                if (!current.exists())
                    tx.set(
                        ref,
                        mapOf(
                            "email" to user.email.orEmpty(),
                            "displayName" to user.displayName.orEmpty().take(120),
                            "kundaliCount" to 0,
                            "createdAt" to FieldValue.serverTimestamp(),
                            "lastKundaliId" to "",
                            "lastOperation" to "",
                            "lastAuditId" to "",
                        ),
                    )
                else if (
                    current.getString("displayName").isNullOrBlank() &&
                        !user.displayName.isNullOrBlank()
                )
                    tx.update(ref, "displayName", user.displayName.orEmpty())
            }
            .await()
    }

    suspend fun signInEmail(email: String, password: String) {
        service().signInWithEmailAndPassword(email.trim(), password).await()
    }

    suspend fun signUp(email: String, password: String, name: String) {
        require(name.length <= 120) { "Use a display name of at most 120 characters." }
        val result = service().createUserWithEmailAndPassword(email.trim(), password).await()
        val user = result.user ?: error("Could not create account.")
        user
            .updateProfile(UserProfileChangeRequest.Builder().setDisplayName(name.trim()).build())
            .await()
        ensureProfile(user)
    }

    suspend fun resetPassword(email: String) {
        service().sendPasswordResetEmail(email.trim()).await()
    }

    private suspend fun googleCredential(context: Context): AuthCredential {
        val id =
            context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
        check(id != 0) {
            "Google sign-in needs a Web client ID in the owner's Firebase configuration. Register SHA fingerprints and download an updated google-services.json."
        }
        val option =
            GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(context.getString(id))
                .setAutoSelectEnabled(false)
                .build()
        val result =
            CredentialManager.create(context)
                .getCredential(
                    context,
                    GetCredentialRequest.Builder().addCredentialOption(option).build(),
                )
        val credential = result.credential
        check(
            credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            "Choose a Google account."
        }
        val token = GoogleIdTokenCredential.createFrom(credential.data).idToken
        return GoogleAuthProvider.getCredential(token, null)
    }

    suspend fun signInGoogle(context: Context) {
        service().signInWithCredential(googleCredential(context)).await()
    }

    suspend fun refresh() {
        val user = service().currentUser ?: return
        user.getIdToken(true).await()
        val g = generation.incrementAndGet()
        savedListener?.remove()
        profileListener?.remove()
        activate(user, g)
    }

    suspend fun signOut() {
        generation.incrementAndGet()
        savedListener?.remove()
        profileListener?.remove()
        _session.value = null
        _count.value = 0
        cacheMutex.withLock {
            dao.clear()
            ownerPrefs.edit().clear().apply()
        }
        auth?.signOut()
        if (configured)
            _notice.value =
                "Signed out. Local saved-kundali cache cleared. Guest calculation and learning stay available."
    }

    suspend fun clearGoogleChooserState(context: Context) {
        runCatching {
            CredentialManager.create(context).clearCredentialState(ClearCredentialStateRequest())
        }
    }

    suspend fun create(birth: SavedBirth): String {
        birth.toBirth()
        val current = requireSession()
        val parent = database().document("users/${current.uid}")
        val child = parent.collection("kundalis").document()
        database()
            .runTransaction { tx ->
                val profile = tx.get(parent)
                check(profile.exists()) { "Refresh account sync before saving." }
                val next =
                    SaveLimit.afterCreate(
                        (profile.getLong("kundaliCount") ?: 0).toInt(),
                        current.admin,
                    )
                tx.set(
                    child,
                    birth.fields() +
                        mapOf(
                            "createdAt" to FieldValue.serverTimestamp(),
                            "updatedAt" to FieldValue.serverTimestamp(),
                        ),
                )
                tx.update(
                    parent,
                    mapOf(
                        "kundaliCount" to next,
                        "lastKundaliId" to child.id,
                        "lastOperation" to "create",
                        "lastAuditId" to "",
                    ),
                )
            }
            .await()
        return child.id
    }

    suspend fun edit(saved: SavedKundali, birth: SavedBirth) {
        birth.toBirth()
        check(requireSession().uid == saved.uid) { "Other users' charts are read-only." }
        val ref = database().document("users/${saved.uid}/kundalis/${saved.id}")
        database()
            .runTransaction { tx ->
                check(tx.get(ref).exists()) { "This saved chart was deleted. Refresh the list." }
                tx.update(ref, birth.fields() + mapOf("updatedAt" to FieldValue.serverTimestamp()))
            }
            .await()
    }

    suspend fun delete(saved: SavedKundali) {
        val me = requireSession()
        check(me.uid == saved.uid || me.admin)
        val parent = database().document("users/${saved.uid}")
        val child = parent.collection("kundalis").document(saved.id)
        val audit = if (me.admin) database().collection("adminAudit").document() else null
        database()
            .runTransaction { tx ->
                val profile = tx.get(parent)
                val record = tx.get(child)
                if (record.exists()) {
                    val next = SaveLimit.afterDelete((profile.getLong("kundaliCount") ?: 0).toInt())
                    tx.delete(child)
                    tx.update(
                        parent,
                        mapOf(
                            "kundaliCount" to next,
                            "lastKundaliId" to child.id,
                            "lastOperation" to "delete",
                            "lastAuditId" to (audit?.id ?: ""),
                        ),
                    )
                    if (audit != null)
                        tx.set(audit, auditData(me.uid, "delete_kundali", saved.uid, saved.id))
                }
            }
            .await()
    }

    private fun auditData(admin: String, action: String, target: String, kundali: String) =
        mapOf(
            "adminUid" to admin,
            "action" to action,
            "targetUid" to target,
            "kundaliId" to kundali,
            "timestamp" to FieldValue.serverTimestamp(),
        )

    suspend fun allUsers(): List<UserProfile> {
        check(requireSession().admin) { "Owner/admin access required." }
        return database().collection("users").get(Source.SERVER).await().documents.map { d ->
            UserProfile(
                d.id,
                d.getString("email").orEmpty(),
                d.getString("displayName").orEmpty(),
                (d.getLong("kundaliCount") ?: 0).toInt(),
                d.getTimestamp("createdAt")?.toDate()?.time ?: 0,
            )
        }
    }

    suspend fun userKundalis(uid: String): List<SavedKundali> {
        val me = requireSession()
        check(me.uid == uid || me.admin)
        return database()
            .collection("users/$uid/kundalis")
            .get(Source.SERVER)
            .await()
            .documents
            .mapNotNull { decode(uid, it) }
    }

    suspend fun deleteUserData(uid: String) {
        val me = requireSession()
        check(me.uid == uid || me.admin)
        userKundalis(uid).forEach { delete(it) }
        val parent = database().document("users/$uid")
        if (me.admin) {
            val audit = database().collection("adminAudit").document()
            database()
                .runTransaction { tx ->
                    val profile = tx.get(parent)
                    check((profile.getLong("kundaliCount") ?: 0) == 0L) {
                        "Charts changed during deletion. Refresh and retry."
                    }
                    if (profile.exists()) {
                        tx.update(
                            parent,
                            mapOf(
                                "lastOperation" to "erase",
                                "lastKundaliId" to "",
                                "lastAuditId" to audit.id,
                            ),
                        )
                        tx.set(audit, auditData(me.uid, "delete_user_data", uid, ""))
                    }
                }
                .await()
        }
        database()
            .runTransaction { tx ->
                val p = tx.get(parent)
                if (p.exists()) {
                    check((p.getLong("kundaliCount") ?: 0) == 0L) {
                        "Charts changed during deletion. Retry."
                    }
                    tx.delete(parent)
                }
            }
            .await()
    }

    suspend fun deleteAccount(password: String?, googleContext: Context?) {
        val user = service().currentUser ?: error("Sign in first.")
        if (googleContext != null) user.reauthenticate(googleCredential(googleContext)).await()
        else
            user
                .reauthenticate(
                    EmailAuthProvider.getCredential(
                        user.email ?: error("Use Google reauthentication."),
                        password ?: "",
                    )
                )
                .await()
        val claimed = user.getIdToken(false).await().claims["admin"] == true
        val ownerConfig =
            if (claimed) database().document("config/admin").get(Source.SERVER).await() else null
        val disabledOwner =
            claimed &&
                ownerConfig?.getString("uid") == user.uid &&
                ownerConfig.getBoolean("disabled") == true
        if (!disabledOwner) {
            deleteUserData(user.uid)
            if (claimed && ownerConfig?.getString("uid") == user.uid) {
                // Data is gone before token-level administrative authorization is disabled.
                database().document("config/admin").update("disabled", true).await()
            }
        }
        if (claimed && ownerConfig?.getString("uid") == user.uid)
            _session.value = _session.value?.copy(admin = false)
        cacheMutex.withLock { dao.clear() }
        _count.value = 0
        user.delete().await()
        signOut()
    }

    private fun decode(uid: String, d: DocumentSnapshot): SavedKundali? =
        runCatching {
                SavedKundali(
                    uid,
                    d.id,
                    SavedBirth(
                        d.getString("personName")!!,
                        d.getString("birthDate")!!,
                        d.getString("birthTime")!!,
                        d.getString("placeName")!!,
                        d.getString("country")!!,
                        (d.get("latitude") as Number).toDouble(),
                        (d.get("longitude") as Number).toDouble(),
                        d.getString("timeZoneId")!!,
                    ),
                    d.getTimestamp("createdAt")?.toDate()?.time ?: 0,
                    d.getTimestamp("updatedAt")?.toDate()?.time ?: 0,
                )
            }
            .getOrNull()

    fun close() {
        generation.incrementAndGet()
        savedListener?.remove()
        profileListener?.remove()
        auth?.removeAuthStateListener(authListener)
        cache.close()
    }
}

fun accountError(error: Throwable): String =
    when (error) {
        is com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException ->
            "Please sign in again, then retry account deletion. Saved cloud data may already have been removed."
        is com.google.firebase.firestore.FirebaseFirestoreException ->
            when (error.code) {
                FirebaseFirestoreException.Code.UNAVAILABLE ->
                    "Connect to the internet to save, edit or delete cloud kundalis. Cached charts still open offline."
                FirebaseFirestoreException.Code.PERMISSION_DENIED ->
                    "Access denied. Check sign-in, owner claim and the deployed Firestore rules."
                else ->
                    "The cloud operation did not finish. Refresh and retry; no local slot is allocated until the server confirms."
            }
        is FirebaseAuthException ->
            when (error.errorCode) {
                "ERROR_EMAIL_ALREADY_IN_USE" ->
                    "This email already has an account. Sign in or reset its password."
                "ERROR_WEAK_PASSWORD" -> "Choose a stronger password (at least 6 characters)."
                "ERROR_USER_MISMATCH" -> "Choose the Google account belonging to this login."
                else ->
                    "Could not authenticate. Check your details and connection, or reset your password."
            }
        else ->
            error.message?.takeIf { !it.contains("password", true) && it.length < 300 }
                ?: "The operation did not finish. Check your connection and retry."
    }

// Firebase's process-wide Firestore instance must be configured only before its first use.
private object CloudFirestore {
    private var firestore: FirebaseFirestore? = null

    @Synchronized
    fun instance(app: FirebaseApp): FirebaseFirestore =
        firestore
            ?: FirebaseFirestore.getInstance(app)
                .apply {
                    firestoreSettings =
                        FirebaseFirestoreSettings.Builder()
                            .setLocalCacheSettings(MemoryCacheSettings.newBuilder().build())
                            .build()
                }
                .also { firestore = it }
}
