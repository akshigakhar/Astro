// Run outside the app with Application Default Credentials. Never bundle credentials.
import { initializeApp, applicationDefault } from "firebase-admin/app";
import { getAuth } from "firebase-admin/auth";
import { getFirestore, FieldValue } from "firebase-admin/firestore";
const uid = process.argv[2];
if (!uid || uid.includes("/") || uid.length > 128)
  throw new Error("Usage: node set-admin.mjs OWNER_UID");
initializeApp({ credential: applicationDefault() });
const auth = getAuth();
const db = getFirestore();
const owner = await auth.getUser(uid);
if (
  !owner.providerData.some((p) => p.providerId === "google.com") ||
  owner.providerData.some((p) => p.providerId === "password")
)
  throw new Error(
    "The owner must have a Google-only account. Do not link Email/Password to it.",
  );
const config = db.doc("config/admin");
const current = await config.get();
if (current.exists && current.get("uid") !== uid) {
  let oldExists = true;
  try {
    await auth.getUser(current.get("uid"));
  } catch (e) {
    if (e.code === "auth/user-not-found") oldExists = false;
    else throw e;
  }
  if (oldExists)
    throw new Error(
      "A different owner already exists. Refusing to grant a second owner. Use the Firebase project administrator for an intentional out-of-app transfer.",
    );
}
await db.runTransaction(async (tx) => {
  const d = await tx.get(config);
  if (d.exists && current.exists && d.get("uid") !== current.get("uid"))
    throw new Error("Owner configuration changed. Retry after review.");
  if (d.exists && !current.exists)
    throw new Error("Another owner was configured concurrently.");
  tx.set(config, {
    uid,
    disabled: false,
    configuredAt: FieldValue.serverTimestamp(),
  });
});
await auth.setCustomUserClaims(uid, {
  ...(owner.customClaims ?? {}),
  admin: true,
});
console.log(
  "Single owner configured. Sign out and sign in with Google, or refresh account claims in Astro. Enable Google 2-Step Verification.",
);
