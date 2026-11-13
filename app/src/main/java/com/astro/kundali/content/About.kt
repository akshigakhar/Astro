package com.astro.kundali.content

val aboutSections =
    linkedMapOf(
        "About Astro" to
            "Astro — Kundali & Learn Jyotish is an independent, offline learning app inspired by K. N. Rao's research-minded and non-fearful approach. No endorsement is claimed. It teaches chart verification, Parashari and Jaimini together, birth-time accuracy and respect for individual circumstances.",
        "Accuracy & educational limits" to
            (accuracy +
                " Astrology is a traditional interpretive framework, not a scientifically established predictor. The app is educational; it does not provide medical, financial, investment or legal advice, diagnose illness, predict death or guarantee outcomes. Use qualified professionals for health and financial decisions. No paid remedies or gemstones are sold."),
        "On-device data & no analytics" to
            "Astro has no account, backend, ads, analytics, crash-reporting service or network permission. Chart inputs, worksheet entries and in-progress quiz answers stay in Android's saved UI state on this device; they are not uploaded by Astro. Only completed-lesson best scores are saved in private SharedPreferences across app restarts. Application backup is disabled. The app does not collect your location: you choose a city or enter coordinates manually.",
        "Sharing is your choice" to
            "Nothing is sent automatically. Share text and Save .txt include personal birth details, chart placements and current periods; worksheet events are excluded. The Android app or document provider you choose controls any subsequent transfer or cloud upload. Choose a local folder to export offline. Treat shared summaries and screenshots as personal information.",
        "Remove your data" to
            "Use Android Settings → Apps → Astro → Storage → Clear storage (wording varies by device) to remove local progress and app data. Uninstalling also removes app-private data. Files you exported and copies you shared remain in their chosen destinations; delete those separately. Returning to birth details does not clear saved lesson scores.",
        "Conventions & accessibility" to
            "Calculations use Lahiri ayanamsa, whole-sign houses, mean lunar node and the documented introductory Chara scheme. D9/D10 depend on birth-time accuracy. Charts include spoken house/planet descriptions, screen-reader house actions and a text placement alternative. System text size is respected; charts enlarge and can be panned horizontally, and controls wrap. A chart's graphic is a reference, not a substitute for its explanations.",
    )
