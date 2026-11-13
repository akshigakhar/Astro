"""Validate an intentional vMAJOR.MINOR.PATCH tag and derive monotonic Android metadata."""
import os
import re


def release_metadata(tag):
    match = re.fullmatch(r"v(0|[1-9][0-9]{0,2})\.(0|[1-9][0-9]{0,2})\.(0|[1-9][0-9]{0,2})", tag)
    if not match:
        raise ValueError("Use a version tag vMAJOR.MINOR.PATCH, with each component 0–999 and no leading zeros.")
    major, minor, patch = map(int, match.groups())
    code = major * 1_000_000 + minor * 1_000 + patch
    if code == 0:
        raise ValueError("v0.0.0 cannot produce a positive Android version code.")
    return {"ASTRO_VERSION_NAME": f"{major}.{minor}.{patch}", "ASTRO_VERSION_CODE": str(code)}


if __name__ == "__main__":
    values = release_metadata(os.environ["RELEASE_TAG"])
    with open(os.environ["GITHUB_ENV"], "a", encoding="utf-8") as output:
        for key, value in values.items():
            output.write(f"{key}={value}\n")
