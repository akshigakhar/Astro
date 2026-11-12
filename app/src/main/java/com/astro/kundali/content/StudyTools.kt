package com.astro.kundali.content

import com.astro.kundali.astro.*
import com.astro.kundali.astro.Period
import java.time.*

const val d10Guide =
    "Read career from D1 first: the tenth house, its lord, occupants and natural significators. In the research-minded approach inspired by K. N. Rao, use Dashamsha (D10) to refine that promise, not to invent a profession from one placement. Inspect D10 Lagna and its lord, then its tenth house, lord, occupants and associations; compare repeated links with D1 and D9. Judge the relevant planets' strength in context and test dated work events in Vimshottari AND Chara before using transits as corroboration. Desha-Kala-Patra, education, opportunity and choices matter. D10 Lagna is sensitive to birth-time error: verify the record and nearby times. This introductory guide does not claim Rao's endorsement or promise a career outcome."

fun vargaHouseReading(c: Chart, division: Int, house: Int): String {
    if (division == 1) return houseReading(c, house)
    val v = c.varga(division)
    val sign = v.signOfHouse(house)
    val lord = rulers[sign]
    val lordPosition = v.positions.first { it.name == lord }
    return "D$division house $house: ${houseTitles[house-1]}. Sign ${signs[sign]}, lord $lord in D$division house ${lordPosition.house} (${signs[lordPosition.sign]}). Occupants: ${v.occupants(house).joinToString { it.name }.ifEmpty { "none" }}. These are divisional houses counted from D$division Lagna, not natal houses. ${if (division == 10) "Compare these career-related links with D1's tenth house and its lord before judging timing." else "Use D9 as supporting testimony to D1; do not interpret a divisional placement alone."}"
}

fun lordConnections(c: Chart, name: String): String {
    val p = c.p(name)
    val owned = (1..12).filter { rulers[c.signOfHouse(it)] == name }
    val dispositor = c.p(rulers[p.sign])
    return "$name: owns natal houses ${owned.joinToString().ifEmpty { "none (node)" }}; occupies ${p.house} (${houseTitles[p.house-1]}), ${signs[p.sign]}. Dispositor ${dispositor.name} occupies house ${dispositor.house}."
}

data class EventCheck(
    val date: LocalDate,
    val instant: Instant,
    val md: Period?,
    val ad: Period?,
    val chara: Period?,
    val transitions: List<String>,
)

/** Date-only events use birth-place local noon, clamped to birth on the birth date. */
fun checkEvent(
    c: Chart,
    date: LocalDate,
    today: LocalDate = LocalDate.now(ZoneId.of(c.birth.place.zone)),
): EventCheck {
    require(!date.isBefore(c.birth.date)) { "Event date must be on or after birth." }
    require(!date.isAfter(today)) { "Use a past date or today." }
    val zone = ZoneId.of(c.birth.place.zone)
    val instant = maxOf(date.atTime(LocalTime.NOON).atZone(zone).toInstant(), c.birth.instant)
    val start = maxOf(date.atStartOfDay(zone).toInstant(), c.birth.instant)
    val end = date.plusDays(1).atStartOfDay(zone).toInstant()
    val majors = Engine.vimshottari(c)
    val minors = majors.flatMap { Engine.antardashas(it) }
    val charas = Engine.chara(c)
    fun transitions(label: String, periods: List<Period>) =
        periods
            .filter { it.start.isAfter(start) && it.start.isBefore(end) }
            .map { "$label → ${it.lord} at ${it.start.atZone(zone)}" }
    return EventCheck(
        date,
        instant,
        majors.firstOrNull { it.active(instant) },
        minors.firstOrNull { it.active(instant) },
        charas.firstOrNull { it.active(instant) },
        transitions("MD", majors) + transitions("AD", minors) + transitions("Chara", charas),
    )
}

fun eventReading(c: Chart, event: EventCheck): String = buildString {
    appendLine(
        "Reference: ${event.instant.atZone(ZoneId.of(c.birth.place.zone))}. Date only: local noon, or birth instant if later on the birth date. An event elsewhere may need a different civil date; exact event time is not recorded here."
    )
    appendLine(
        "Vimshottari: ${event.md?.lord ?: "outside displayed range"} / ${event.ad?.lord ?: "unavailable"}."
    )
    listOfNotNull(event.md?.lord, event.ad?.lord).distinct().forEach {
        appendLine(lordConnections(c, it))
    }
    val chara = event.chara
    if (chara == null)
        appendLine(
            "Chara: outside the introductory twelve-sign cycle. No repeated cycle is inferred."
        )
    else {
        val sign = signs.indexOf(chara.lord)
        appendLine(
            "Chara: ${chara.lord}, natal house ${(sign-c.asc.sign+12)%12+1}; ${lordConnections(c, rulers[sign])}"
        )
    }
    if (event.transitions.isNotEmpty())
        appendLine(
            "Period changes on this local date — check the actual event time before assigning one period:\n${event.transitions.joinToString("\n")}"
        )
    append(
        "Verification prompt: which owned/occupied houses relate to this event in BOTH systems? Record agreements, contradictions and misses. A match is not proof of prediction; do not adjust birth time to fit three events."
    )
}

fun kundaliSummary(c: Chart, now: Instant = Instant.now()): String = buildString {
    appendLine("Astro — Kundali & Learn Jyotish")
    appendLine("${c.birth.name} | ${c.birth.date} ${c.birth.time} | ${c.birth.place.name}")
    appendLine(
        "${c.birth.place.lat}, ${c.birth.place.lon} | ${c.birth.place.zone} | UTC ${c.birth.instant}"
    )
    appendLine("Lahiri ${c.ayanamsa.fmt()}°; whole-sign houses; mean node. As of $now.")
    appendLine(
        "Lagna ${signs[c.asc.sign]}; Moon ${signs[c.moon.sign]}, ${nakshatras[c.moon.nak]} pada ${c.moon.pada}; Sun ${signs[c.p("Sun").sign]}; Tithi ${c.tithi}; Atmakaraka ${c.atmakaraka.name}."
    )
    appendLine("Running Vimshottari: ${running(c, now)}.")
    val chara = Engine.chara(c).firstOrNull { it.active(now) }
    appendLine("Introductory Chara: ${chara?.lord ?: "outside twelve-sign cycle"}.")
    c.positions.forEach { p ->
        val d9 = c.varga(9).positions.first { it.name == p.name }
        val d10 = c.varga(10).positions.first { it.name == p.name }
        appendLine(
            "${p.name}: D1 ${signs[p.sign]} ${p.degree.fmt()}°, H${p.house}; D9 ${signs[d9.sign]}, H${d9.house}; D10 ${signs[d10.sign]}, H${d10.house}${if (p.name != "Lagna" && p.vargottama) "; Vargottama" else ""}."
        )
    }
    appendLine("D1 tenth lord: ${lordConnections(c, c.lord(10).name)}")
    appendLine(
        "Vargas refine natal testimony; verify birth time and both dashas. No outcome is guaranteed."
    )
    append(accuracy)
}
