package com.astro.kundali.content

import com.astro.kundali.astro.*
import java.time.Instant
import java.util.Locale

fun Double.fmt() = String.format(Locale.ROOT, "%.2f", this)

const val accuracy =
    "Learning calculation: low-precision Schlyter/Meeus algorithms, not a professional ephemeris. Typical errors are a few arc-minutes for Sun/Moon and under about 1° for slow planets; these are estimates, not guarantees over 1900–today. Within about 1° of a sign or nakshatra boundary, cross-check in Jagannatha Hora. Birth-time accuracy matters, especially for Lagna and vargas."
val houseTitles =
    listOf(
        "Self & vitality",
        "Resources & speech",
        "Initiative & siblings",
        "Home & foundations",
        "Learning & children",
        "Service & challenges",
        "Partnerships",
        "Change & shared resources",
        "Meaning & teachers",
        "Work & public role",
        "Gains & networks",
        "Retreat & release",
    )
val karakas =
    mapOf(
        "Sun" to "identity, responsibility, authority and father",
        "Moon" to "mind, care, rhythms and mother",
        "Mars" to "initiative, engineering, courage and conflict",
        "Mercury" to "language, analysis, trade and study",
        "Jupiter" to "wisdom, teachers, children and growth",
        "Venus" to "relationships, art, agreements and enjoyment",
        "Saturn" to "work, patience, limits and longevity of effort",
        "Rahu" to "unfamiliar settings, ambition and unconventional experience",
        "Ketu" to "detachment, investigation and spiritual inquiry",
        "Lagna" to "embodied perspective and the starting point of houses",
    )

fun planetReading(c: Chart, p: Position): String {
    val owned = (1..12).filter { rulers[c.signOfHouse(it)] == p.name }.joinToString()
    val nakRemainder = norm(p.longitude) % (40.0 / 3)
    val nearBoundary = minOf(p.degree, 30 - p.degree, nakRemainder, 40.0 / 3 - nakRemainder) < 1
    val caution =
        if (nearBoundary)
            " Boundary caution: this placement is within about 1° of a sign or nakshatra boundary; cross-check in professional software before relying on its label."
        else ""
    return "${p.name} is at ${p.degree.fmt()}° ${signs[p.sign]}, whole-sign house ${p.house}: ${houseTitles[p.house-1]}. ${nakshatras[p.nak]}, pada ${p.pada}, is ruled by ${p.nakLord}. ${p.dignity}. ${if(p.retro)"Retrograde: apparent backward motion invites careful review, not automatic weakness." else "Direct motion."} ${if(p.combust)"Within the chosen combustion limit of the Sun; assess visibility and independent expression." else "Outside the chosen combustion limit (or not applicable)."} Its natural themes are ${karakas[p.name]}. ${if(owned.isNotEmpty())"It rules houses $owned, bringing their topics to house ${p.house}." else "No classical sign ownership is assigned here."} D9: ${signs[p.d9]}${if(p.d9==p.sign)" — Vargottama, the same sign in D1 and D9" else ""}. Combine ownership, occupation and strength before any conclusion.$caution"
}

fun houseReading(c: Chart, h: Int): String {
    val lord = c.lord(h)
    val occ = c.occupants(h).joinToString { it.name }.ifEmpty { "none" }
    val karaka =
        listOf(
            "Sun",
            "Jupiter",
            "Mars",
            "Moon",
            "Jupiter",
            "Mars",
            "Venus",
            "Saturn",
            "Jupiter",
            "Sun",
            "Jupiter",
            "Ketu",
        )[h - 1]
    return "1. House $h concerns ${houseTitles[h-1].lowercase()}; its sign is ${signs[c.signOfHouse(h)]}.\n2. Occupants: $occ. Their natural meanings add themes, not guaranteed events.\n3. Lord ${lord.name} sits in house ${lord.house} (${signs[lord.sign]}), linking this topic with ${houseTitles[lord.house-1].lowercase()}; ${lord.dignity.lowercase()}.\n4. Karaka $karaka is in house ${c.p(karaka).house}. Compare that testimony with the lord, aspects and relevant varga. This is a framework for observation, not a fixed forecast."
}

data class Yoga(
    val name: String,
    val present: Boolean,
    val explanation: String,
    val verify: String,
)

fun yogas(c: Chart): List<Yoga> {
    fun linked(a: Position, b: Position) = a.sign == b.sign || (a.sign - b.sign + 12) % 12 == 6
    val moon = c.moon
    val j = c.p("Jupiter")
    val m = c.p("Mars")
    val raja =
        (listOf(1, 4, 7, 10).flatMap { a -> listOf(5, 9).map { b -> a to b } }).filter { (a, b) ->
            c.lord(a).name != c.lord(b).name && linked(c.lord(a), c.lord(b))
        }
    val wealth = listOf(2, 5, 9, 11)
    val dhana =
        wealth
            .flatMap { a -> wealth.filter { it > a }.map { b -> a to b } }
            .filter { (a, b) -> c.lord(a).name != c.lord(b).name && linked(c.lord(a), c.lord(b)) }
    val emptySides =
        c.positions
            .take(7)
            .filter { it.name !in listOf("Sun", "Moon") }
            .none { (it.sign - moon.sign + 12) % 12 in listOf(1, 11) }
    val cancellation =
        c.positions
            .take(7)
            .filter { it.name !in listOf("Sun", "Moon") }
            .any {
                (it.sign - moon.sign + 12) % 12 in listOf(0, 3, 6, 9) ||
                    it.house in listOf(1, 4, 7, 10)
            } || moon.house in listOf(1, 4, 7, 10)
    return listOf(
        Yoga(
            "Gajakesari",
            (j.sign - moon.sign + 12) % 12 in listOf(0, 3, 6, 9),
            "Jupiter in a kendra from Moon connects judgment and mental life. Strength and affliction still matter.",
            "Count Moon's sign as 1; Jupiter must be in 1, 4, 7 or 10. Here the count is ${(j.sign-moon.sign+12)%12+1}.",
        ),
        Yoga(
            "Budha-Aditya",
            c.p("Sun").sign == c.p("Mercury").sign,
            "Sun and Mercury share a sign. This common combination is not by itself a promise of brilliance; assess combustion and lordship.",
            "Compare Sun and Mercury sign names; inspect separation and houses owned.",
        ),
        Yoga(
            "Chandra-Mangala",
            linked(moon, m),
            "Moon and Mars conjoin or mutually oppose by seventh aspect. Initiative and emotional urgency need context.",
            "Count Moon to Mars: ${(m.sign-moon.sign+12)%12+1}; 1 or 7 meets this definition.",
        ),
        Yoga(
            "Raja yoga",
            raja.isNotEmpty(),
            "Distinct kendra and 5th/9th lords connect. Matching house pairs: ${raja.joinToString().ifEmpty{"none"}}.",
            "List lords of 1/4/7/10 and 5/9. Check same sign or mutual seventh aspect. A single planet owning both is a separate yogakaraka consideration.",
        ),
        Yoga(
            "Dhana yoga",
            dhana.isNotEmpty(),
            "Distinct lords among 2/5/9/11 connect; pairs: ${dhana.joinToString().ifEmpty{"none"}}. This indicates a theme to investigate, not guaranteed wealth.",
            "Write the four house lords and compare conjunctions and mutual seventh aspects.",
        ),
        Yoga(
            "Kemadruma check",
            emptySides && !cancellation,
            "Adjacent signs empty of classical planets other than Sun/Moon: $emptySides. Cancellation found: $cancellation. This introductory check includes planets in kendras from Moon or Lagna and Moon in a Lagna kendra; traditions give further cancellations. Never infer isolation or hardship from this alone.",
            "Check 2nd/12th from Moon, excluding Sun and nodes, then inspect kendras and wider chart support.",
        ),
        Yoga(
            "Amala",
            listOf("Jupiter", "Venus", "Mercury").any {
                val p = c.p(it)
                p.house == 10 || (p.sign - moon.sign + 12) % 12 == 9
            },
            "A natural benefic in tenth from Lagna or Moon can support constructive public activity. Mercury's associations and the Moon's waxing status require further judgment.",
            "Count the tenth from Lagna and Moon; locate Jupiter, Venus and Mercury. This check uses these three benefics.",
        ),
    )
}

fun running(c: Chart, now: Instant = Instant.now()): String {
    val md =
        Engine.vimshottari(c).firstOrNull { it.active(now) }
            ?: return "outside the displayed 240-year Vimshottari window"
    val ad = Engine.antardashas(md).first { it.active(now) }
    return "${md.lord} / ${ad.lord}"
}

data class Topic(
    val title: String,
    val houses: List<Int>,
    val indicators: List<String>,
    val note: String,
)

val topics =
    listOf(
        Topic(
            "Career & Status",
            listOf(10),
            listOf("Sun", "Saturn"),
            "Read the tenth, its lord and relevant skills; employment depends on education, opportunity and choices.",
        ),
        Topic(
            "Wealth & Income",
            listOf(2, 11, 5, 9),
            listOf("Jupiter"),
            "Distinguish resources, gains, judgment and opportunity; this is not investment advice.",
        ),
        Topic(
            "Marriage & Partnerships",
            listOf(7),
            listOf("Venus"),
            "Compare the seventh and Venus with D9. Respect autonomy and avoid declaring a person's relationship fate.",
        ),
        Topic(
            "Health & Vitality",
            listOf(1, 6, 8),
            listOf("Moon"),
            "Educational only, never a diagnosis. See qualified clinicians for symptoms; chart patterns cannot establish disease.",
        ),
        Topic(
            "Education & Children",
            listOf(4, 5, 9),
            listOf("Mercury", "Jupiter"),
            "Compare learning foundations, creativity and higher study. Do not infer fertility or a child's worth.",
        ),
        Topic(
            "Home / Property / Foreign",
            listOf(4, 9, 12),
            listOf("Rahu"),
            "Separate home, long journeys and living away; migration also depends on law and practical opportunity.",
        ),
        Topic(
            "Spirituality",
            listOf(9, 12, 5),
            listOf("Ketu"),
            "Include the Atmakaraka as a study lens; no placement establishes moral or spiritual superiority.",
        ),
    )

fun topicReading(c: Chart, t: Topic) =
    t.houses.joinToString("\n\n") { houseReading(c, it) } +
        "\n\n" +
        t.indicators.joinToString("\n") {
            "$it: house ${c.p(it).house}, ${c.p(it).dignity}; D9 ${signs[c.p(it).d9]}."
        } +
        "\nAtmakaraka: ${c.atmakaraka.name}.\n\nHow this is read: ${t.note} Require repeated testimony and Desha-Kala-Patra.\nTiming link: running ${running(c)}. Judge those lords' owned and occupied houses; confirm in Vimshottari AND Chara before considering timing."

fun transits(c: Chart, now: Instant): List<Pair<String, String>> {
    val today =
        Engine.calculate(
            c.birth.copy(
                date = now.atZone(java.time.ZoneOffset.UTC).toLocalDate(),
                time = now.atZone(java.time.ZoneOffset.UTC).toLocalTime(),
                place = c.birth.place.copy(zone = "UTC"),
            )
        )
    val out =
        mutableListOf(
            "Sidereal sky • ${now.atZone(java.time.ZoneOffset.UTC).toLocalDate()} UTC" to
                today.positions.take(9).joinToString("\n") {
                    "${it.name}: ${signs[it.sign]} ${it.degree.fmt()}°"
                }
        )
    for (n in listOf("Saturn", "Jupiter", "Rahu")) {
        val p = today.p(n)
        out +=
            n to
                "${(p.sign-c.moon.sign+12)%12+1} from natal Moon; ${(p.sign-c.asc.sign+12)%12+1} from natal Lagna. Count inclusively from each natal sign."
    }
    val sat = (today.p("Saturn").sign - c.moon.sign + 12) % 12
    out +=
        "Sade Sati" to
            "${when(sat){11->"First phase: Saturn in 12th from Moon"
0->"Middle phase: Saturn in Moon sign"
1->"Final phase: Saturn in 2nd from Moon"
else->"Not currently in Sade Sati"}}. This is a broad transit cycle, not a threat or guarantee of distress. In Rao's non-fearful approach, judge natal promise, both dashas, resources and real circumstances. No paid remedies are needed."
    val s = today.p("Saturn").copy(house = (today.p("Saturn").sign - c.asc.sign + 12) % 12 + 1)
    val j = today.p("Jupiter").copy(house = (today.p("Jupiter").sign - c.asc.sign + 12) % 12 + 1)
    val houses = (1..12).filter { aspects(s, it) && aspects(j, it) }
    out +=
        "Double transit" to
            "Saturn AND Jupiter occupy/aspect houses ${houses.joinToString().ifEmpty{"none"}}. ${houses.joinToString { "$it: ${houseTitles[it-1]}" }}. Use seventh aspects plus Saturn 3/10 and Jupiter 5/9, counted inclusively. A highlighted house is only a candidate: confirm natal support and both dashas, never predict an event from transit alone."
    return out
}
