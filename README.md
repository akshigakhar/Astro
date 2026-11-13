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
