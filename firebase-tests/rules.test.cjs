const { before, after, beforeEach, test } = require("node:test");
const assert = require("node:assert/strict");
const fs = require("node:fs");
const path = require("node:path");
const {
  initializeTestEnvironment,
  assertSucceeds,
  assertFails,
} = require("@firebase/rules-unit-testing");
const {
  doc,
  collection,
  setDoc,
  getDoc,
  getDocs,
  updateDoc,
  deleteDoc,
  runTransaction,
  writeBatch,
  serverTimestamp,
} = require("firebase/firestore");
let env;
const owner = "only-owner";
function client(uid = "alice", admin = false, provider = "password") {
  return env
    .authenticatedContext(uid, {
      email: `${uid}@example.test`,
      admin,
      firebase: { sign_in_provider: provider },
    })
    .firestore();
}
function profile(db, uid) {
  return doc(db, `users/${uid}`);
}
function birth() {
  return {
    personName: "Learning sample",
    birthDate: "1947-08-15",
    birthTime: "00:00",
    placeName: "Delhi",
    country: "India",
    latitude: 28.6139,
    longitude: 77.209,
    timeZoneId: "Asia/Kolkata",
    createdAt: serverTimestamp(),
    updatedAt: serverTimestamp(),
  };
}
async function initProfile(db, uid) {
  await setDoc(profile(db, uid), {
    email: `${uid}@example.test`,
    displayName: uid,
    kundaliCount: 0,
    createdAt: serverTimestamp(),
    lastKundaliId: "",
    lastOperation: "",
    lastAuditId: "",
  });
}
async function save(db, uid, id) {
  const p = profile(db, uid);
  const k = doc(db, `users/${uid}/kundalis/${id}`);
  return runTransaction(db, async (tx) => {
    const d = (await tx.get(p)).data();
    tx.set(k, birth());
    tx.update(p, {
      kundaliCount: d.kundaliCount + 1,
      lastKundaliId: id,
      lastOperation: "create",
      lastAuditId: "",
    });
  });
}
async function remove(db, uid, id, isAdmin = false) {
  const p = profile(db, uid);
  const k = doc(db, `users/${uid}/kundalis/${id}`);
  const audit = doc(collection(db, "adminAudit"));
  return runTransaction(db, async (tx) => {
    const d = (await tx.get(p)).data();
    await tx.get(k);
    tx.delete(k);
    tx.update(p, {
      kundaliCount: d.kundaliCount - 1,
      lastKundaliId: id,
      lastOperation: "delete",
      lastAuditId: isAdmin ? audit.id : "",
    });
    if (isAdmin)
      tx.set(audit, {
        adminUid: owner,
        action: "delete_kundali",
        targetUid: uid,
        kundaliId: id,
        timestamp: serverTimestamp(),
      });
  });
}
before(async () => {
  env = await initializeTestEnvironment({
    projectId: "demo-astro-kundali",
    firestore: {
      host: "127.0.0.1",
      port: 8080,
      rules: fs.readFileSync(
        path.resolve(__dirname, "../firestore.rules"),
        "utf8",
      ),
    },
  });
});
after(async () => {
  if (env) await env.cleanup();
});
beforeEach(async () => {
  await env.clearFirestore();
  await env.withSecurityRulesDisabled(async (context) => {
    await setDoc(doc(context.firestore(), "config/admin"), { uid: owner });
  });
});

test("guest cannot read cloud birth details or write data", async () => {
  const db = env.unauthenticatedContext().firestore();
  await assertFails(getDoc(profile(db, "alice")));
  await assertFails(setDoc(doc(db, "users/alice/kundalis/one"), birth()));
});
test("two saves, third denied; deletion frees exactly one slot", async () => {
  const db = client();
  await initProfile(db, "alice");
  await assertSucceeds(save(db, "alice", "one"));
  await assertSucceeds(save(db, "alice", "two"));
  await assertFails(save(db, "alice", "three"));
  assert.equal((await getDoc(profile(db, "alice"))).data().kundaliCount, 2);
  await assertSucceeds(remove(db, "alice", "one"));
  await assertSucceeds(save(db, "alice", "three"));
  assert.equal((await getDocs(collection(db, "users/alice/kundalis"))).size, 2);
});
test("client cannot bypass count with direct creates, forged count resets or counter-only writes", async () => {
  const db = client();
  await initProfile(db, "alice");
  await assertFails(setDoc(doc(db, "users/alice/kundalis/raw"), birth()));
  await assertFails(
    updateDoc(profile(db, "alice"), {
      kundaliCount: 1,
      lastKundaliId: "nonexistent",
      lastOperation: "create",
    }),
  );
  await save(db, "alice", "one");
  await assertFails(updateDoc(profile(db, "alice"), { kundaliCount: 0 }));
  await assertFails(deleteDoc(doc(db, "users/alice/kundalis/one")));
});
test("multiple creates cannot share a single counter increment", async () => {
  const db = client();
  await initProfile(db, "alice");
  const batch = writeBatch(db);
  batch.set(doc(db, "users/alice/kundalis/one"), birth());
  batch.set(doc(db, "users/alice/kundalis/two"), birth());
  batch.update(profile(db, "alice"), {
    kundaliCount: 1,
    lastKundaliId: "one",
    lastOperation: "create",
    lastAuditId: "",
  });
  await assertFails(batch.commit());
  assert.equal((await getDoc(profile(db, "alice"))).data().kundaliCount, 0);
});
test("concurrent creates cannot exceed two", async () => {
  const db = client();
  await initProfile(db, "alice");
  const outcomes = await Promise.allSettled(
    ["one", "two", "three"].map((id) => save(db, "alice", id)),
  );
  assert.equal(outcomes.filter((x) => x.status === "fulfilled").length, 2);
  assert.equal((await getDoc(profile(db, "alice"))).data().kundaliCount, 2);
});
test("regular users cannot read or mutate another user tree, list users or read audit", async () => {
  const alice = client();
  await initProfile(alice, "alice");
  await save(alice, "alice", "one");
  const bob = client("bob");
  await assertFails(getDoc(profile(bob, "alice")));
  await assertFails(getDoc(doc(bob, "users/alice/kundalis/one")));
  await assertFails(
    updateDoc(doc(bob, "users/alice/kundalis/one"), {
      personName: "Stolen",
      updatedAt: serverTimestamp(),
    }),
  );
  await assertFails(getDocs(collection(bob, "users")));
  await assertFails(getDocs(collection(bob, "adminAudit")));
});
test("owner can read all profiles and charts; owner own saves are uncapped", async () => {
  const alice = client();
  await initProfile(alice, "alice");
  await save(alice, "alice", "one");
  const db = client(owner, true, "google.com");
  await initProfile(db, owner);
  for (const id of ["one", "two", "three"])
    await assertSucceeds(save(db, owner, id));
  await assertSucceeds(getDocs(collection(db, "users")));
  await assertSucceeds(getDoc(doc(db, "users/alice/kundalis/one")));
  await assertFails(
    updateDoc(doc(db, "users/alice/kundalis/one"), {
      personName: "Support edit",
      updatedAt: serverTimestamp(),
    }),
  );
});
test("a second admin claim or password-authenticated owner cannot access admin data", async () => {
  const a = client();
  await initProfile(a, "alice");
  await save(a, "alice", "one");
  for (const db of [
    client("intruder", true, "google.com"),
    client(owner, true, "password"),
  ]) {
    await assertFails(getDocs(collection(db, "users")));
    await assertFails(getDoc(doc(db, "users/alice/kundalis/one")));
    await assertFails(setDoc(doc(db, "config/admin"), { uid: "intruder" }));
  }
});
test("admin deletion requires a matching new immutable audit record", async () => {
  const a = client();
  await initProfile(a, "alice");
  await save(a, "alice", "one");
  const db = client(owner, true, "google.com");
  await assertFails(remove(db, "alice", "one", false));
  await assertSucceeds(remove(db, "alice", "one", true));
  const logs = await getDocs(collection(db, "adminAudit"));
  assert.equal(logs.size, 1);
  assert.equal(logs.docs[0].data().action, "delete_kundali");
  await assertFails(updateDoc(logs.docs[0].ref, { action: "rewritten" }));
  await assertFails(deleteDoc(logs.docs[0].ref));
  assert.equal((await getDoc(profile(db, "alice"))).data().kundaliCount, 0);
});
test("parent deletion cannot orphan charts; audited empty-profile deletion is allowed", async () => {
  const a = client();
  await initProfile(a, "alice");
  await save(a, "alice", "one");
  await assertFails(deleteDoc(profile(a, "alice")));
  const db = client(owner, true, "google.com");
  await assertFails(deleteDoc(profile(db, "alice")));
  await remove(db, "alice", "one", true);
  await assertFails(deleteDoc(profile(db, "alice")));
  const audit = doc(collection(db, "adminAudit"));
  const batch = writeBatch(db);
  batch.update(profile(db, "alice"), {
    lastOperation: "erase",
    lastKundaliId: "",
    lastAuditId: audit.id,
  });
  batch.set(audit, {
    adminUid: owner,
    action: "delete_user_data",
    targetUid: "alice",
    kundaliId: "",
    timestamp: serverTimestamp(),
  });
  await assertSucceeds(batch.commit());
  await assertSucceeds(deleteDoc(profile(db, "alice")));
});
test("birth-only schema rejects calculated tables and edits preserve count/timestamps", async () => {
  const db = client();
  await initProfile(db, "alice");
  await save(db, "alice", "one");
  const ref = doc(db, "users/alice/kundalis/one");
  await assertFails(
    updateDoc(ref, {
      calculatedPlanets: ["Sun"],
      updatedAt: serverTimestamp(),
    }),
  );
  await assertSucceeds(
    updateDoc(ref, {
      personName: "Updated name",
      updatedAt: serverTimestamp(),
    }),
  );
  assert.equal((await getDoc(profile(db, "alice"))).data().kundaliCount, 1);
  await assertFails(
    updateDoc(ref, { createdAt: new Date(0), updatedAt: serverTimestamp() }),
  );
});

test("owner account removal can revoke still-valid admin tokens, never re-enable them in-app", async () => {
  const db = client(owner, true, "google.com");
  await initProfile(db, owner);
  await assertFails(updateDoc(doc(db, "config/admin"), { disabled: true }));
  const audit = doc(collection(db, "adminAudit"));
  const batch = writeBatch(db);
  batch.update(profile(db, owner), {
    lastOperation: "erase",
    lastKundaliId: "",
    lastAuditId: audit.id,
  });
  batch.set(audit, {
    adminUid: owner,
    action: "delete_user_data",
    targetUid: owner,
    kundaliId: "",
    timestamp: serverTimestamp(),
  });
  await batch.commit();
  await deleteDoc(profile(db, owner));
  await assertSucceeds(updateDoc(doc(db, "config/admin"), { disabled: true }));
  await assertFails(getDocs(collection(db, "users")));
  await assertFails(getDocs(collection(db, "adminAudit")));
  await assertFails(updateDoc(doc(db, "config/admin"), { disabled: false }));
});
