"""Build-only integration check with a disposable key. Never publishes an APK."""
import os
from pathlib import Path
import secrets
import subprocess
import tempfile

ROOT = Path(__file__).resolve().parents[2]
KEYS = ["ASTRO_KEYSTORE_PATH", "ASTRO_KEYSTORE_PASSWORD", "ASTRO_KEY_ALIAS", "ASTRO_KEY_PASSWORD"]

def gradle(env, *tasks, expect_failure=False):
    result = subprocess.run([str(ROOT / "gradlew"), "--no-daemon", "--max-workers=2", *tasks], cwd=ROOT, env=env, text=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
    if expect_failure:
        if result.returncode == 0 or "Release signing is required" not in result.stdout:
            raise RuntimeError("Missing signing credentials did not produce the expected release-signing failure.")
        print("PASS: release without credentials is rejected.", flush=True)
    else:
        print(result.stdout, flush=True)
        result.check_returncode()


def main():
    env = os.environ.copy()
    for key in KEYS + ["ASTRO_VERSION_NAME", "ASTRO_VERSION_CODE"]:
        env.pop(key, None)
    local = ROOT / "local.properties"
    original = local.read_bytes() if local.exists() else None
    sdk = os.environ.get("ANDROID_HOME") or os.environ.get("ANDROID_SDK_ROOT")
    if not sdk:
        # The local development workspace normally specifies sdk.dir here.
        values = dict(line.split("=", 1) for line in (original or b"").decode().splitlines() if "=" in line and not line.startswith("#"))
        sdk = values.get("sdk.dir")
    if not sdk:
        raise RuntimeError("Set ANDROID_HOME for the signing smoke test.")
    base = f"sdk.dir={sdk}\n"
    try:
        local.write_text(base, encoding="utf-8")
        gradle(env, "assembleRelease", expect_failure=True)
        with tempfile.TemporaryDirectory(prefix="astro-signing-") as directory:
            signing = dict(zip(KEYS, [str(Path(directory) / "ci.jks"), secrets.token_hex(24), "astro-ci", secrets.token_hex(24)]))
            test_env = env | signing
            subprocess.run([str(Path(env["JAVA_HOME"]) / "bin" / "keytool") if env.get("JAVA_HOME") else "keytool", "-genkeypair", "-keystore", signing[KEYS[0]], "-storetype", "JKS", "-storepass:env", KEYS[1], "-keypass:env", KEYS[3], "-alias", "astro-ci", "-keyalg", "RSA", "-keysize", "2048", "-validity", "2", "-dname", "CN=Disposable CI Test", "-noprompt"], check=True, env=test_env)
            apksigner = str(Path(sdk) / "build-tools" / "34.0.0" / "apksigner")
            apk = str(ROOT / "app/build/outputs/apk/release/app-release.apk")
            gradle(test_env | {"ASTRO_VERSION_NAME": "1.2.3", "ASTRO_VERSION_CODE": "1002003"}, "assembleRelease")
            subprocess.run([apksigner, "verify", "--verbose", apk], check=True, env=test_env)
            metadata = subprocess.check_output([str(Path(sdk) / "build-tools" / "34.0.0" / "aapt"), "dump", "badging", apk], text=True)
            if "versionCode='1002003'" not in metadata or "versionName='1.2.3'" not in metadata or "application-debuggable" in metadata:
                raise RuntimeError("Release metadata or non-debuggable flag is incorrect.")
            print("PASS: signed environment-configured release and version metadata verified.", flush=True)
            props = ["astro.keystorePath", "astro.keystorePassword", "astro.keyAlias", "astro.keyPassword"]
            local.write_text(base + "".join(f"{key}={signing[source]}\n" for key, source in zip(props, KEYS)), encoding="utf-8")
            local.chmod(0o600)
            gradle(env, "assembleRelease")
            subprocess.run([apksigner, "verify", apk], check=True, env=env)
            print("PASS: signed local.properties-configured release verified.", flush=True)
    finally:
        if original is None:
            local.unlink(missing_ok=True)
        else:
            local.write_bytes(original)


if __name__ == "__main__":
    main()
