# Astro — Kundali & Learn Jyotish

An offline Android learning app with a real Vedic birth chart at its centre. Enter a name, date, 24-hour birth time and a city, or use the India independence sample. Search 77 built-in cities or supply coordinates and an IANA time zone. No account, backend, ads, API keys, network permission or paid libraries. Birth input stays in the current session and Android saved UI state for recreation; it is not uploaded or written to an app database.

Nine tabs cover overview, a tappable North Indian D1 chart and placement table, planets, twelve houses, seven life topics, transparent yoga checks, Vimshottari and introductory Chara periods, current transits, and fourteen original lessons with chart-based exercises and a glossary. Planet and house paragraphs expose the reasoning rather than presenting generic horoscopes. Dasha rows expand to show Antardashas; current periods are marked with a dot. Dates are shown in UTC and use a 365.25-day year.

## Method taught

Inspired by K. N. Rao's emphasis on research across many charts, verification against dated events, Parashari and Jaimini together, confirmation in two dashas, Desha-Kala-Patra, accurate birth records and ethical consultation. Independent educational software; no endorsement is claimed and no book text is reproduced. Never frighten a client, predict death, diagnose illness, or sell remedies or gemstones. Astrology is presented as a traditional interpretive framework, not a scientifically established predictor. Health, wealth and relationship sections preserve agency and do not replace qualified professional advice.

Lessons cover the Rao approach; house groups; grahas; signs and dignity; the four-step house checklist; nakshatras; strength, combustion and retrogression; aspects; yogas; Vimshottari; Jaimini; vargas; transits; and an eight-step reading workflow. Each has 250–450 words and a chart-specific exercise. D1, D9 and D10 are drawn in the same North Indian style on the Kundali tab; D60 is taught rather than displayed.

## Install the APK

Open this repository's **Actions → Android build**, select a successful run, and download **astro-debug-apk** from Artifacts (GitHub sign-in may be required). Unzip it, copy `app-debug.apk` to an Android 8.0+ device, open it, and allow installation from that source when Android asks. This is a debug-signed v1 build. Do not use its signing key for a production release. CI also uploads unit-test reports.

## Build / Android Studio

Requirements: JDK 17, Android SDK platform 34 and build-tools 34.0.0. Open the repository in Android Studio, let Gradle sync, then run `app` on an API 26+ emulator/device. Set `ANDROID_HOME` or an untracked `local.properties` containing `sdk.dir=/your/android/sdk` if needed.

```sh
./gradlew testDebugUnitTest assembleDebug
```

The Gradle 8.7 wrapper is committed. AGP 8.5.2, Kotlin/Compose compiler plugin 2.0.20, Compose BOM 2024.09.00, compile/target SDK 34, min SDK 26, Java bytecode 17. Single `:app` module. Engine and cities are pure Kotlin under `astro/`; curriculum, significations and generators under `content/`; Compose screens and Canvas chart under `ui/`.

## Accuracy and conventions

Low-precision Paul Schlyter / Meeus-style orbital algorithms, with iterative Kepler solution, heliocentric-to-geocentric conversion, major lunar longitude terms and dominant Jupiter/Saturn perturbations. Typical errors are a few arc-minutes for Sun/Moon and under about 1° for slow planets; these are estimates, not certified bounds across 1900–today. A planet within about 1° of a sign or nakshatra boundary should be cross-checked in professional software such as **Jagannatha Hora**. Lagna and vargas are particularly sensitive to birth time and location. Historical IANA offsets are used, including historic local-time rules; verify the actual recorded civil time. A nonexistent local time at a daylight-saving jump is rejected; an ambiguous repeated time uses Java's earlier valid offset.

- Lahiri: 23.85708° at 1 January 2000, with 50.29 arcseconds/year precession as specified in the project brief.
- Whole-sign houses; mean lunar node; Ketu exactly opposite Rahu.
- D9: movable starts from itself, fixed from ninth, dual from fifth.
- Parashari seventh aspects, Mars fourth/eighth, Jupiter fifth/ninth, Saturn third/tenth. Node aspects are not assigned. Occupation counts as contact for double transit.
- Sign-based conjunctions; Raja/Dhana checks use distinct lords with conjunction or mutual seventh aspect. Yogakaraka ownership alone is not silently counted as that detector's yoga.
- Basic exaltation/debilitation/own-sign labels; no complete Shadbala or Neecha Bhanga calculator. Kemadruma uses an explicitly explained cancellation subset. Amala uses Jupiter, Venus and Mercury, with association caveats.
- Retrograde is a one-day longitude difference, approximate near stations. Combustion thresholds: Moon 12°, Mars 17°, Mercury 14°, Venus 10°, Jupiter 11°, Saturn 15°.
- Seven-karaka Atmakaraka uses Sun through Saturn, excluding nodes.
- Vimshottari reconstructs the first period's start before birth; the balance at birth is therefore shortened while Antardashas remain correctly anchored.
- **Chara is an introductory Rao version**: Lagna start; forward if odd-numbered sign, backward if even; directional distance to sign lord, 12 years if lord occupies its own sign. It omits advanced variants and is not a claim to implement the full Rao method. Its table shows one twelve-sign cycle.

The acceptance test checks 15 Aug 1947, 00:00 IST, Delhi: Taurus Lagna and Rahu; Cancer Sun/Moon/Mercury/Venus/Saturn; Gemini Mars; Libra Jupiter, within 2° of rounded reference positions. Other tests cover UTC/JD, historical offsets and daylight saving, ayanamsa, nakshatra/pada, D9, dasha continuity and balance, aspects, nodes, basic dignity, yoga fixtures, Chara direction, transit framing, city zones and curriculum lengths. No ephemeris or other calculation library is used in the app or unit tests. The fixed sample reference numbers were independently checked during development against Swiss Ephemeris 2.10.03 using Moshier, Lahiri and mean node (JD 2432412.2708333335). The largest sample longitude difference is about 0.053° (Moon). This single-chart check does not establish accuracy over the full supported date range.

| Sample placement | App sidereal longitude | Independent reference |
| --- | ---: | ---: |
| Lagna | 37.7311° | 37.7323° |
| Sun | 117.9961° | 117.9890° |
| Moon | 94.0360° | 93.9835° |
| Mercury | 103.6863° | 103.6743° |
| Venus | 112.5731° | 112.5612° |
| Mars | 67.4671° | 67.4562° |
| Jupiter | 205.8731° | 205.8777° |
| Saturn | 110.4366° | 110.4730° |
| Rahu (mean) | 35.0689° | 35.0697° |

## Roadmap

Future improvements: independently benchmark the complete supported date range against a professional ephemeris; refine near-station motion and boundary warnings; add opt-in encrypted local chart storage, richer accessibility for the chart, advanced Chara variants, localization and device instrumentation coverage. The current v1 is fully usable without those additions.


## Career charts, verification worksheet and text export

The Kundali tab has adjacent D1/D9/D10 selectors. Each divisional chart calculates its own Lagna, sign numbers, occupants and whole-sign houses, including in the accessible placement table and tapped-house explanation. D1 shows natal degrees; D9/D10 show sign placements rather than fabricated divisional degrees. D10 (Dashamsha) divides each natal sign into ten half-open 3° portions: odd-numbered signs start from themselves, even-numbered signs from the ninth, advancing zodiacally. D10 Lagna is birth-time sensitive. The career explainer starts from D1's tenth house/lord, seeks D10 support and dated-event confirmation in both dashas, and preserves Rao-inspired research and ethical cautions. Vargottama planets have a labeled badge on the Planets tab when D1 and D9 signs match.

The Dasha tab's **Past events check** accepts exactly three event descriptions and ISO dates (YYYY-MM-DD), from birth through today. It displays Vimshottari MD/AD, introductory Chara and the period lords' natal house ownership, occupation and dispositor connections. Date-only events use local noon in the birth-place time zone, clamped to the birth instant on the birth date. Same-day MD/AD/Chara changes are flagged with their local transition times: an exact event time may be needed. The implemented Chara table is one twelve-sign cycle; dates beyond it explicitly say unavailable, without inventing advanced or repeated-cycle rules. This worksheet teaches verification, including contradictions and misses, rather than claiming predictive proof or rectifying birth time from three events. Entries remain in Android saved UI state for the chart, with no database or network calls.

**Share text** opens Android's plain-text share chooser. **Save .txt** uses Android's document picker to write a UTF-8 summary to a user-selected destination without broad storage permissions. The summary includes personal birth details, D1/D9/D10 placements, Vargottama labels, current periods and accuracy conventions; worksheet events are excluded. Choose a local folder to save offline. Receiving apps and document providers control any later upload; Astro itself has no network permission.

## Lesson quizzes and device-local learning progress

Every lesson now includes **Chart of the lesson**, a mini D1 North Indian chart with highlighted topic houses and planets, a textual focus list, and a legend. It uses the same renderer as the Kundali charts. House circles, bold labels and asterisks identify focus without relying on colour alone. This reference chart stays D1 even when the lesson discusses divisional charts or transits; quiz feedback identifies those separate calculations.

Each of the fourteen lessons ends in five multiple-choice questions. Where appropriate they are generated from the current birth chart: house lords and their locations, signs, nakshatra/pada, dasha lords, aspects, dignity, karakas, D9/D10 and Vargottama. Concept questions reinforce technique and ethics. Choices are deterministic for a given lesson/chart, each first choice is final for that attempt, and feedback immediately gives the correct answer and explanation. Restart/Retake clears the attempt, allowing practice without erasing a better saved score.

Answering all five questions completes a lesson regardless of score. The lesson list shows completion, best score and an overall progress bar. SharedPreferences (`learning_progress_v1`, stable `lesson-1` … `lesson-14` keys) stores only completed-quiz best scores, globally across birth charts and app restarts. Birth details, questions and selected answers are not stored there. In-progress answers use Android saved UI state for recreation. There is no account, network call or sync, and application backup remains disabled. Clearing app data or uninstalling clears learning progress. The tests verify all 70 generated questions on different charts, independent sample answers, aspect counting, scoring, immutable best-score updates and chart-focus validity.

## Signed release builds (no committed credentials)

Use JDK 17 and the SDK setup above. A release build is non-debuggable and requires an explicit signing configuration; it never falls back to the debug key or silently emits an unsigned release. Debug builds/tests require no release credentials. Create and securely back up your own long-lived keystore outside the checkout (Android Studio's Generate Signed APK wizard or JDK `keytool` can do this). Keep the same signing identity for future updates. Losing it can prevent upgrades. The disposable keys used by CI are only for smoke testing and must never sign a distributed release.

Gradle reads these settings, with a nonblank environment value taking precedence over ignored root `local.properties`:

| Environment variable | `local.properties` key | Value |
| --- | --- | --- |
| `ASTRO_KEYSTORE_PATH` | `astro.keystorePath` | Absolute path, or path relative to repository root |
| `ASTRO_KEYSTORE_PASSWORD` | `astro.keystorePassword` | Keystore password |
| `ASTRO_KEY_ALIAS` | `astro.keyAlias` | Signing alias |
| `ASTRO_KEY_PASSWORD` | `astro.keyPassword` | Key password |
| `ASTRO_VERSION_NAME` | `astro.versionName` | Version name; default `1.0.0` |
| `ASTRO_VERSION_CODE` | `astro.versionCode` | Positive Android version code; default `1` |

Supply passwords through your local secret manager/environment or enter them into your private `local.properties`; do not paste them into commands recorded in shell history, logs, issues or Git. Java properties require escaping backslashes and line breaks; environment variables are preferable for values containing special characters. All four signing settings are required. `local.properties`, keystores, signing-properties files and `.env` files are ignored, but always review `git status` before committing. Never commit a real key, password or base64-encoded keystore.

For example, this command prompts for passwords rather than embedding them in shell history (choose a secure path outside the checkout):

```sh
keytool -genkeypair -keystore /secure/path/astro-release.jks -storetype JKS -alias astro -keyalg RSA -keysize 3072 -validity 10000
```

After configuring the four signing settings:

```sh
./gradlew testDebugUnitTest assembleRelease
# Signed output: app/build/outputs/apk/release/app-release.apk
# Verify it with Android SDK build-tools/34.0.0/apksigner verify --verbose <apk>
```

The version code must increase for upgrades. Tag-based releases derive it automatically as `major * 1000000 + minor * 1000 + patch`; each component is 0–999, with no leading zeros, and `v0.0.0` is rejected. Supported release tags are exactly `vMAJOR.MINOR.PATCH` (for example `v1.2.3` gives name `1.2.3`, code `1002003`); prerelease suffixes are not supported in this workflow. Manual builds must choose a compatible, increasing code. A release signed with your key cannot replace an installed debug-signed build with the same package ID: uninstall the debug build first, which clears app-private data.

## GitHub Releases on version tags

In repository **Settings → Secrets and variables → Actions**, configure these repository secrets before pushing a release tag:

- `ASTRO_KEYSTORE_BASE64`: the single-line base64 representation of your production keystore file (encode locally; the encoded value is also a secret).
- `ASTRO_KEYSTORE_PASSWORD`, `ASTRO_KEY_ALIAS`, `ASTRO_KEY_PASSWORD`: matching signing credentials.

The workflow is `.github/workflows/release.yml`. After merging this setup and validating the intended commit, create and push an intentional version tag. Pushing `v*` starts the workflow; invalid version formats fail before decoding credentials. Only push a release tag when you intend to publish that commit.

For example, after choosing an unused version and the intended commit:

```sh
git tag -a v1.2.3 -m "Astro 1.2.3"
git push origin v1.2.3
```

The build job uses JDK 17, runs tests, decodes the key into a temporary owner-only file, builds the signed release, verifies the APK signature, and prepares `astro-<version>.apk` plus its `.sha256` checksum. Signing material is removed in an always-run cleanup step and is never uploaded. A separate publish job has `contents: write` permission and attaches those files to a GitHub Release for the existing tag. It uses the built-in `GITHUB_TOKEN`, so no personal access token or app API key is required. An existing GitHub Release is not overwritten; choose a new version/tag for a new published binary. Releases are public if the repository is public. Adding this workflow does not publish a release; publication starts only when a supported tag is pushed.

PR CI also runs the tag/version unit tests and `.github/scripts/signing_smoke.py`. The latter tests missing-credential rejection and both environment/local-properties signing with a generated disposable key, verifies each APK, restores any existing `local.properties` and deletes the key. That smoke-test release APK is not uploaded; `astro-debug-apk` remains the installable test artifact. To run the smoke check locally, set `JAVA_HOME` to JDK 17 and `ANDROID_HOME` to your SDK, then run `python .github/scripts/signing_smoke.py`.

## Accuracy, privacy and accessibility

**About / Accuracy & Privacy** is available before birth entry and from chart results. It explains low-precision calculations, the educational nature of astrology, medical/financial/legal advice limits, on-device state and score storage, no analytics or telemetry, disabled backups and explicit cloud/device-transfer exclusion rules, deliberate sharing/export exceptions, and how to clear local data. Astro itself has no network or broad storage permission. A chosen sharing app or cloud document provider may transfer a summary; exported files and shared copies must be removed separately.

The shared North Indian chart has a spoken D1/D9/D10 description containing Lagna, all twelve house signs, full occupant names and lesson highlights. Interactive charts expose twelve screen-reader house actions and a text **Choose house** menu. Text placement cards replace the narrow table at larger system font sizes. Charts grow with system text size and pan horizontally; the containing screen scrolls vertically. Buttons/controls wrap instead of forcing crowded horizontal rows; large-text mode uses a scroll-content section picker instead of fixed-height tabs; expandable sections announce expanded/current state and quiz feedback is a polite live region. About headings are marked for screen-reader navigation.

Device review checklist: use Android's largest font size and display size on a narrow portrait screen; check birth date/time controls, the About dialog, all chart selectors/house menus, text placements, sharing controls, lessons and quizzes for clipping. With TalkBack, navigate About headings, read a chart description, invoke a house action or the house menu, and confirm quiz feedback is announced. Text/semantics generation and builds are tested in CI; TalkBack and visual large-font behavior still require device/emulator verification.
