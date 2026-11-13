@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.astro.kundali.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.astro.kundali.accounts.*
import com.astro.kundali.astro.*
import java.time.*
import kotlinx.coroutines.launch

private const val CLOUD_DISCLOSURE =
    "Saving uploads birth details to your Firebase account. The single app owner/admin can view saved kundalis for support and moderation. Calculated chart tables stay on-device. No analytics are added."

@Composable
internal fun AccountWorkspace(vm: AccountViewModel = viewModel()) {
    val repo = vm.repository
    val user by repo.session.collectAsState()
    val rows by repo.saved.collectAsState()
    val count by repo.count.collectAsState()
    val notice by repo.notice.collectAsState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var route by rememberSaveable { mutableStateOf("welcome") }
    var about by rememberSaveable { mutableStateOf(false) }
    val chartSaver =
        Saver<Chart?, List<String>>(
            save = { c ->
                c?.birth?.let {
                    listOf(
                        it.name,
                        it.date.toString(),
                        it.time.toString(),
                        it.place.name,
                        it.place.lat.toString(),
                        it.place.lon.toString(),
                        it.place.zone,
                    )
                } ?: emptyList()
            },
            restore = { a ->
                if (a.isEmpty()) null
                else
                    Engine.calculate(
                        Birth(
                            a[0],
                            LocalDate.parse(a[1]),
                            LocalTime.parse(a[2]),
                            Place(a[3], a[4].toDouble(), a[5].toDouble(), a[6]),
                        )
                    )
            },
        )
    var chart by rememberSaveable(stateSaver = chartSaver) { mutableStateOf<Chart?>(null) }
    var readOnly by rememberSaveable { mutableStateOf(false) }
    var saveDialog by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var edit by remember { mutableStateOf<SavedKundali?>(null) }
    var deleting by remember { mutableStateOf<SavedKundali?>(null) }
    var deleteAccount by remember { mutableStateOf(false) }
    fun task(work: suspend () -> Unit) {
        if (!busy) {
            busy = true
            error = null
            scope.launch {
                try {
                    work()
                } catch (e: Exception) {
                    error = accountError(e)
                } finally {
                    busy = false
                }
            }
        }
    }
    fun open(row: SavedKundali, admin: Boolean) {
        runCatching { Engine.calculate(row.birth.toBirth()) }
            .onSuccess {
                chart = it
                readOnly = admin
                route = "chart"
            }
            .onFailure {
                error =
                    "This record has invalid birth details. Edit it in your own list or contact the owner."
            }
    }
    LaunchedEffect(user?.uid, user?.admin) {
        if (readOnly && user?.admin != true) {
            chart = null
            readOnly = false
            route = "mine"
        }
    }
    BackHandler(enabled = route != "welcome" && route != "chart") {
        if (!busy) {
            route = if (chart == null) "welcome" else "chart"
            edit = null
        }
    }
    when {
        edit != null ->
            BirthEditor(
                edit!!,
                busy,
                error,
                onCancel = { edit = null },
                onSave = { birth ->
                    task {
                        repo.edit(edit!!, birth)
                        edit = null
                    }
                },
            )
        route == "welcome" ->
            AuthScreen(
                repo.configured,
                user,
                notice,
                busy,
                error,
                onEmail = { email, password, name, signup ->
                    task {
                        if (signup) repo.signUp(email, password, name)
                        else repo.signInEmail(email, password)
                        route = "mine"
                    }
                },
                onGoogle = {
                    task {
                        repo.signInGoogle(context)
                        route = "mine"
                    }
                },
                onReset = { email ->
                    task {
                        repo.resetPassword(email)
                        error =
                            "If an eligible account exists, Firebase will send a password-reset email. Check your inbox."
                    }
                },
                onGuest = {
                    task {
                        repo.signOut()
                        chart = null
                        readOnly = false
                        route = "chart"
                    }
                },
                onContinue = { route = "mine" },
                onAbout = { about = true },
            )
        route == "admin" ->
            if (user?.admin == true)
                AdminPanel(
                    repo,
                    busy,
                    error,
                    onTask = { task(it) },
                    onOpen = { open(it, true) },
                    onBack = { route = "mine" },
                )
            else
                ScreenColumn {
                    Text("Owner/admin verification is required.")
                    TextButton(onClick = { route = "mine" }) { Text("Back") }
                }
        route == "mine" ->
            ScreenColumn {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = { route = "chart" }) {
                        Text(if (chart == null) "New Kundali" else "Back to chart")
                    }
                    TextButton(onClick = { about = true }) { Text("Accuracy & Privacy") }
                }
                Text("My Kundalis", style = MaterialTheme.typography.headlineMedium)
                if (user == null) {
                    Text(
                        "Sign in to save up to two kundalis. Guests can calculate and learn without saving."
                    )
                    Button(onClick = { route = "welcome" }) { Text("Sign in / Create account") }
                } else {
                    Text("${user!!.displayName.ifBlank { user!!.email }} • ${user!!.email}")
                    Text(
                        if (user!!.admin) "Owner account • no personal save cap"
                        else "${maxOf(count,rows.size)} of 2 saved"
                    )
                    Text(CLOUD_DISCLOSURE)
                    Text(notice, style = MaterialTheme.typography.bodySmall)
                    if (!user!!.admin && maxOf(count, rows.size) >= 2) Text(LIMIT_MESSAGE)
                    if (busy) LinearProgressIndicator(Modifier.fillMaxWidth())
                    error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            enabled = !busy,
                            onClick = {
                                chart = null
                                readOnly = false
                                route = "chart"
                            },
                        ) {
                            Text("Calculate a new Kundali")
                        }
                        OutlinedButton(enabled = !busy, onClick = { task { repo.refresh() } }) {
                            Text("Refresh account & sync")
                        }
                        if (user!!.admin)
                            OutlinedButton(enabled = !busy, onClick = { route = "admin" }) {
                                Text("Admin Panel")
                            }
                        OutlinedButton(
                            enabled = !busy,
                            onClick = {
                                task {
                                    repo.signOut()
                                    repo.clearGoogleChooserState(context)
                                    chart = null
                                    readOnly = false
                                    route = "welcome"
                                }
                            },
                        ) {
                            Text("Sign out")
                        }
                        OutlinedButton(enabled = !busy, onClick = { deleteAccount = true }) {
                            Text("Delete account & cloud data")
                        }
                    }
                    if (rows.isEmpty())
                        Text(
                            "No saved kundalis in this account's local cache. Sign in online once to sync. Saving, editing and deleting need a connection."
                        )
                    rows.forEach { row ->
                        SavedCard(
                            row,
                            busy,
                            onOpen = { open(row, false) },
                            onEdit = { edit = row },
                            onDelete = { deleting = row },
                        )
                    }
                }
            }
        else -> {
            val total = maxOf(count, rows.size)
            val actions = {
                route = "mine"
                error = null
            }
            if (chart == null)
                BirthScreen(onAbout = { about = true }, onAccounts = actions) {
                    chart = it
                    readOnly = false
                }
            else
                Results(
                    chart!!,
                    onAbout = { about = true },
                    onAccounts = actions,
                    readOnly = readOnly,
                    saveLabel =
                        when {
                            user == null -> "Sign in to save Kundali"
                            user!!.admin || total < 2 -> "Save Kundali"
                            else -> "Manage saved Kundalis"
                        },
                    saveMessage =
                        if (user != null && !user!!.admin && total >= 2) LIMIT_MESSAGE else null,
                    onSave = {
                        if (user == null) route = "welcome"
                        else if (!user!!.admin && total >= 2) route = "mine" else saveDialog = true
                    },
                ) {
                    chart = null
                    readOnly = false
                }
        }
    }
    if (about)
        Dialog(
            onDismissRequest = { about = false },
            properties = DialogProperties(usePlatformDefaultWidth = false),
        ) {
            AboutScreen { about = false }
        }
    if (saveDialog && chart != null && user != null) {
        var country by
            remember(chart!!.birth) { mutableStateOf(defaultCountry(chart!!.birth.place.zone)) }
        AlertDialog(
            onDismissRequest = { if (!busy) saveDialog = false },
            title = { Text("Save birth details") },
            text = {
                Column(
                    Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text("${chart!!.birth.name} • ${chart!!.birth.date} • ${chart!!.birth.time}")
                    Text(CLOUD_DISCLOSURE)
                    OutlinedTextField(
                        country,
                        { country = it },
                        label = { Text("Country") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Text(
                        if (user!!.admin) "Owner account: saves are uncapped."
                        else
                            "Up to 2 saved kundalis per login. Server transactions and rules enforce this limit."
                    )
                    error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    if (busy) LinearProgressIndicator(Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                TextButton(
                    enabled = !busy,
                    onClick = {
                        task {
                            repo.create(SavedBirth.from(chart!!.birth, country))
                            saveDialog = false
                            route = "mine"
                        }
                    },
                ) {
                    Text("Save to my account")
                }
            },
            dismissButton = {
                TextButton(enabled = !busy, onClick = { saveDialog = false }) { Text("Cancel") }
            },
        )
    }
    deleting?.let { row ->
        ConfirmDelete(
            "Delete ${row.birth.personName}?",
            "This removes its cloud birth details and frees a save slot. It also disappears from the local cache after sync.",
            busy,
            onCancel = { deleting = null },
            onConfirm = {
                task {
                    repo.delete(row)
                    deleting = null
                }
            },
        )
    }
    if (deleteAccount && user != null) {
        var password by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { if (!busy) deleteAccount = false },
            title = { Text("Delete account and saved kundalis?") },
            text = {
                Column(
                    Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        "Reauthenticate to permanently remove your Firebase account, user profile and all saved birth details. A failed connection may leave a partially completed deletion; retry to finish. Existing exports and immutable admin audit records are not removed. Device-local lesson scores can be cleared in Android Settings."
                    )
                    if (user!!.admin)
                        Text(
                            "Deleting the owner login leaves no active admin. Owner recovery must be performed outside the app by the Firebase project administrator."
                        )
                    if (user!!.provider != "google.com")
                        OutlinedTextField(
                            password,
                            { password = it },
                            label = { Text("Current password") },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions =
                                KeyboardOptions(
                                    keyboardType = KeyboardType.Password,
                                    autoCorrect = false,
                                ),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    if (busy) LinearProgressIndicator(Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                TextButton(
                    enabled = !busy,
                    onClick = {
                        task {
                            repo.deleteAccount(
                                if (user!!.provider == "google.com") null else password,
                                if (user!!.provider == "google.com") context else null,
                            )
                            deleteAccount = false
                            chart = null
                            readOnly = false
                            route = "welcome"
                        }
                    },
                ) {
                    Text(
                        if (user!!.provider == "google.com") "Verify with Google & delete"
                        else "Verify password & delete"
                    )
                }
            },
            dismissButton = {
                TextButton(enabled = !busy, onClick = { deleteAccount = false }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun ScreenColumn(content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier.fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        content = content,
    )
}

@Composable
private fun AuthScreen(
    configured: Boolean,
    user: AccountSession?,
    notice: String,
    busy: Boolean,
    error: String?,
    onEmail: (String, String, String, Boolean) -> Unit,
    onGoogle: () -> Unit,
    onReset: (String) -> Unit,
    onGuest: () -> Unit,
    onContinue: () -> Unit,
    onAbout: () -> Unit,
) {
    var signup by rememberSaveable { mutableStateOf(false) }
    var email by rememberSaveable { mutableStateOf("") }
    var name by rememberSaveable { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    ScreenColumn {
        Text("Astro", style = MaterialTheme.typography.headlineLarge)
        Text("Calculate and learn as a guest. Sign in only if you want to save birth charts.")
        Button(enabled = !busy, onClick = onGuest, modifier = Modifier.fillMaxWidth()) {
            Text("Continue as guest")
        }
        TextButton(onClick = onAbout) { Text("Accuracy & Privacy") }
        if (user != null)
            OutlinedButton(
                enabled = !busy,
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Continue as ${user.email}")
            }
        Text(notice, style = MaterialTheme.typography.bodySmall)
        Text(CLOUD_DISCLOSURE)
        Text(
            if (signup) "Create account" else "Sign in",
            style = MaterialTheme.typography.titleLarge,
        )
        if (signup)
            OutlinedTextField(
                name,
                { name = it },
                label = { Text("Display name") },
                modifier = Modifier.fillMaxWidth(),
            )
        OutlinedTextField(
            email,
            { email = it },
            label = { Text("Email") },
            keyboardOptions =
                KeyboardOptions(keyboardType = KeyboardType.Email, autoCorrect = false),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            password,
            { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions =
                KeyboardOptions(keyboardType = KeyboardType.Password, autoCorrect = false),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        if (busy) LinearProgressIndicator(Modifier.fillMaxWidth())
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Button(
            enabled = configured && !busy && email.isNotBlank() && password.isNotBlank(),
            onClick = { onEmail(email, password, name, signup) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (signup) "Create account with email" else "Sign in with email")
        }
        OutlinedButton(
            enabled = configured && !busy,
            onClick = onGoogle,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Sign in with Google")
        }
        TextButton(
            enabled = !busy,
            onClick = {
                signup = !signup
                password = ""
            },
        ) {
            Text(if (signup) "Already have an account? Sign in" else "New here? Create an account")
        }
        TextButton(
            enabled = configured && !busy && email.isNotBlank(),
            onClick = { onReset(email) },
        ) {
            Text("Forgot password? Send reset email")
        }
        Text(
            "The owner/admin must use Google sign-in and protect that Google account with 2-Step Verification.",
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun SavedCard(
    row: SavedKundali,
    busy: Boolean,
    onOpen: () -> Unit,
    onEdit: (() -> Unit)? = null,
    onDelete: () -> Unit,
) {
    val preview =
        remember(row.birth) { runCatching { Engine.calculate(row.birth.toBirth()) }.getOrNull() }
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(row.birth.personName, style = MaterialTheme.typography.titleLarge)
            Text(
                "${row.birth.birthDate} ${row.birth.birthTime} • ${row.birth.placeName}, ${row.birth.country}\n${row.birth.latitude}, ${row.birth.longitude} • ${row.birth.timeZoneId}"
            )
            Text(
                preview?.let {
                    "Lagna ${signs[it.asc.sign]} • Moon ${signs[it.moon.sign]} (computed on-device)"
                } ?: "Invalid birth details; edit your record or contact the owner."
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(enabled = !busy && preview != null, onClick = onOpen) {
                    Text("Open chart")
                }
                if (onEdit != null)
                    TextButton(enabled = !busy, onClick = onEdit) { Text("Edit birth details") }
                TextButton(enabled = !busy, onClick = onDelete) { Text("Delete") }
            }
        }
    }
}

@Composable
private fun BirthEditor(
    row: SavedKundali,
    busy: Boolean,
    error: String?,
    onCancel: () -> Unit,
    onSave: (SavedBirth) -> Unit,
) {
    val b = row.birth
    var name by rememberSaveable(row.id) { mutableStateOf(b.personName) }
    var date by rememberSaveable(row.id) { mutableStateOf(b.birthDate) }
    var time by rememberSaveable(row.id) { mutableStateOf(b.birthTime) }
    var place by rememberSaveable(row.id) { mutableStateOf(b.placeName) }
    var country by rememberSaveable(row.id) { mutableStateOf(b.country) }
    var lat by rememberSaveable(row.id) { mutableStateOf(b.latitude.toString()) }
    var lon by rememberSaveable(row.id) { mutableStateOf(b.longitude.toString()) }
    var zone by rememberSaveable(row.id) { mutableStateOf(b.timeZoneId) }
    var validation by remember { mutableStateOf<String?>(null) }
    ScreenColumn {
        Text("Edit birth details", style = MaterialTheme.typography.headlineMedium)
        Text(
            "Only birth details are stored. The chart is recomputed on save/open; editing does not consume another slot."
        )
        OutlinedTextField(
            name,
            { name = it },
            label = { Text("Full name") },
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            date,
            { date = it },
            label = { Text("Birth date • YYYY-MM-DD") },
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            time,
            { time = it },
            label = { Text("Birth time • HH:mm, 24-hour") },
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            place,
            { place = it },
            label = { Text("Place name") },
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            country,
            { country = it },
            label = { Text("Country") },
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            lat,
            { lat = it },
            label = { Text("Latitude") },
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            lon,
            { lon = it },
            label = { Text("Longitude") },
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            zone,
            { zone = it },
            label = { Text("IANA time zone") },
            modifier = Modifier.fillMaxWidth(),
        )
        (validation ?: error)?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        if (busy) LinearProgressIndicator(Modifier.fillMaxWidth())
        Button(
            enabled = !busy,
            onClick = {
                runCatching {
                        SavedBirth(
                                name,
                                date.trim(),
                                time.trim(),
                                place,
                                country,
                                lat.toDouble(),
                                lon.toDouble(),
                                zone.trim(),
                            )
                            .also { Engine.calculate(it.toBirth()) }
                    }
                    .onSuccess(onSave)
                    .onFailure {
                        validation =
                            "Check the date, time, coordinates and time zone: ${it.message}"
                    }
            },
        ) {
            Text("Recompute & save changes")
        }
        TextButton(enabled = !busy, onClick = onCancel) { Text("Cancel") }
    }
}

@Composable
private fun ConfirmDelete(
    title: String,
    message: String,
    busy: Boolean,
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = { if (!busy) onCancel() },
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            TextButton(enabled = !busy, onClick = onConfirm) { Text("Delete permanently") }
        },
        dismissButton = { TextButton(enabled = !busy, onClick = onCancel) { Text("Cancel") } },
    )
}

@Composable
private fun AdminPanel(
    repo: CloudRepository,
    busy: Boolean,
    error: String?,
    onTask: (suspend () -> Unit) -> Unit,
    onOpen: (SavedKundali) -> Unit,
    onBack: () -> Unit,
) {
    var users by remember { mutableStateOf(emptyList<UserProfile>()) }
    var target by remember { mutableStateOf<UserProfile?>(null) }
    var records by remember { mutableStateOf(emptyList<SavedKundali>()) }
    var query by rememberSaveable { mutableStateOf("") }
    var deleting by remember { mutableStateOf<SavedKundali?>(null) }
    var erase by remember { mutableStateOf<UserProfile?>(null) }
    LaunchedEffect(Unit) { onTask { users = repo.allUsers() } }
    ScreenColumn {
        TextButton(
            enabled = !busy,
            onClick = {
                if (target == null) onBack()
                else {
                    target = null
                    records = emptyList()
                }
            },
        ) {
            Text("Back")
        }
        Text("Admin Panel", style = MaterialTheme.typography.headlineMedium)
        Text(
            "Owner-only online support view. Other users' charts open read-only. Every administrative deletion is audited."
        )
        Text(
            "Total user profiles: ${users.size} • Total saved kundalis: ${users.sumOf {it.kundaliCount}}"
        )
        if (busy) LinearProgressIndicator(Modifier.fillMaxWidth())
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        OutlinedButton(
            enabled = !busy,
            onClick = {
                onTask {
                    users = repo.allUsers()
                    target?.let { records = repo.userKundalis(it.uid) }
                }
            },
        ) {
            Text("Refresh support data")
        }
        if (target == null) {
            OutlinedTextField(
                query,
                { query = it },
                label = { Text("Search email or display name") },
                modifier = Modifier.fillMaxWidth(),
            )
            users
                .filter { it.email.contains(query, true) || it.displayName.contains(query, true) }
                .forEach { profile ->
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(14.dp)) {
                            Text("${profile.displayName} • ${profile.email}")
                            Text(
                                "${profile.kundaliCount} saved • created ${Instant.ofEpochMilli(profile.createdAt).atZone(ZoneId.systemDefault()).toLocalDate()}"
                            )
                            TextButton(
                                enabled = !busy,
                                onClick = {
                                    onTask {
                                        records = repo.userKundalis(profile.uid)
                                        target = profile
                                    }
                                },
                            ) {
                                Text("View saved kundalis")
                            }
                        }
                    }
                }
        } else {
            Text(
                "${target!!.displayName} • ${target!!.email}",
                style = MaterialTheme.typography.titleLarge,
            )
            OutlinedButton(enabled = !busy, onClick = { erase = target }) {
                Text("Delete all this user's saved data")
            }
            if (records.isEmpty()) Text("No saved kundalis.")
            records.forEach { row ->
                SavedCard(row, busy, onOpen = { onOpen(row) }, onDelete = { deleting = row })
            }
        }
    }
    deleting?.let { row ->
        ConfirmDelete(
            "Delete ${row.birth.personName}?",
            "This support action removes the saved birth details and is recorded in adminAudit.",
            busy,
            onCancel = { deleting = null },
            onConfirm = {
                onTask {
                    repo.delete(row)
                    records = repo.userKundalis(row.uid)
                    users = repo.allUsers()
                    deleting = null
                }
            },
        )
    }
    erase?.let { profile ->
        ConfirmDelete(
            "Delete all saved data for ${profile.email}?",
            "This removes all kundalis and the Firestore user profile, not their Firebase Auth login. They may sign in again with an empty profile. Partial failures can be retried. The action is audited.",
            busy,
            onCancel = { erase = null },
            onConfirm = {
                onTask {
                    repo.deleteUserData(profile.uid)
                    target = null
                    records = emptyList()
                    users = repo.allUsers()
                    erase = null
                }
            },
        )
    }
}
