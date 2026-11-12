package com.astro.kundali.astro

import java.time.*
import kotlin.math.*

val signs =
    listOf(
        "Aries",
        "Taurus",
        "Gemini",
        "Cancer",
        "Leo",
        "Virgo",
        "Libra",
        "Scorpio",
        "Sagittarius",
        "Capricorn",
        "Aquarius",
        "Pisces",
    )
val rulers =
    listOf(
        "Mars",
        "Venus",
        "Mercury",
        "Moon",
        "Sun",
        "Mercury",
        "Venus",
        "Mars",
        "Jupiter",
        "Saturn",
        "Saturn",
        "Jupiter",
    )
val names = listOf("Sun", "Moon", "Mercury", "Venus", "Mars", "Jupiter", "Saturn", "Rahu", "Ketu")
val nakshatras =
    "Ashwini,Bharani,Krittika,Rohini,Mrigashira,Ardra,Punarvasu,Pushya,Ashlesha,Magha,Purva Phalguni,Uttara Phalguni,Hasta,Chitra,Swati,Vishakha,Anuradha,Jyeshtha,Mula,Purva Ashadha,Uttara Ashadha,Shravana,Dhanishta,Shatabhisha,Purva Bhadrapada,Uttara Bhadrapada,Revati"
        .split(",")
val dashaLords =
    listOf("Ketu", "Venus", "Sun", "Moon", "Mars", "Rahu", "Jupiter", "Saturn", "Mercury")
val dashaYears = listOf(7, 20, 6, 10, 7, 18, 16, 19, 17)

fun norm(x: Double) = (x % 360 + 360) % 360

fun sinD(x: Double) = sin(Math.toRadians(x))

fun cosD(x: Double) = cos(Math.toRadians(x))

fun atanD(y: Double, x: Double) = Math.toDegrees(atan2(y, x))

fun separation(a: Double, b: Double) = abs((a - b + 540) % 360 - 180)

data class Place(val name: String, val lat: Double, val lon: Double, val zone: String)

data class Birth(val name: String, val date: LocalDate, val time: LocalTime, val place: Place) {
    val instant: Instant
        get() = LocalDateTime.of(date, time).atZone(ZoneId.of(place.zone)).toInstant()
}

data class Position(
    val name: String,
    val longitude: Double,
    val house: Int,
    val retro: Boolean = false,
    val combust: Boolean = false,
) {
    val sign
        get() = (norm(longitude) / 30).toInt()

    val degree
        get() = norm(longitude) % 30

    val nak
        get() = floor(norm(longitude) * 3 / 40).toInt().coerceIn(0, 26)

    val pada
        get() = (floor(norm(longitude) * 3 / 10).toInt() % 4) + 1

    val nakLord
        get() = dashaLords[nak % 9]

    val d9
        get() = navamsa(longitude)

    val dignity: String
        get() {
            val exalt =
                mapOf(
                    "Sun" to 0,
                    "Moon" to 1,
                    "Mars" to 9,
                    "Mercury" to 5,
                    "Jupiter" to 3,
                    "Venus" to 11,
                    "Saturn" to 6,
                )
            return when {
                exalt[name] == sign -> "Exalted"
                exalt[name]?.let { (it + 6) % 12 } == sign -> "Debilitated"
                rulers[sign] == name -> "Own sign"
                else -> "Ordinary sign placement"
            }
        }
}

fun navamsa(longitude: Double): Int {
    val s = (norm(longitude) / 30).toInt()
    val start =
        when (s % 3) {
            0 -> s
            1 -> s + 8
            else -> s + 4
        }
    return (start + floor(norm(longitude) % 30 / (10.0 / 3) + 1e-10).toInt()) % 12
}

data class Period(val lord: String, val start: Instant, val end: Instant) {
    fun active(now: Instant) = !now.isBefore(start) && now.isBefore(end)
}

data class Chart(
    val birth: Birth,
    val jd: Double,
    val ayanamsa: Double,
    val positions: List<Position>,
) {
    val asc
        get() = positions.last()

    fun p(name: String) = positions.first { it.name == name }

    val moon
        get() = p("Moon")

    val atmakaraka
        get() = positions.take(7).maxBy { it.degree }

    val tithi
        get() = (norm(p("Moon").longitude - p("Sun").longitude) / 12).toInt() + 1

    fun signOfHouse(h: Int) = (asc.sign + h - 1) % 12

    fun occupants(h: Int) = positions.take(9).filter { it.house == h }

    fun lord(h: Int) = p(rulers[signOfHouse(h)])
}

object Engine {
    fun julian(i: Instant) = i.epochSecond / 86400.0 + i.nano / 86400e9 + 2440587.5

    fun ayanamsa(d: Double) = 23.85708 + (d - 1) / 365.25 * (50.29 / 3600)

    private data class Orbit(
        val n: Double,
        val i: Double,
        val w: Double,
        val a: Double,
        val e: Double,
        val m: Double,
    )

    private fun orbit(p: String, d: Double): Orbit =
        when (p) {
            "Sun" ->
                Orbit(
                    0.0,
                    0.0,
                    282.9404 + 4.70935e-5 * d,
                    1.0,
                    .016709 - 1.151e-9 * d,
                    356.0470 + .9856002585 * d,
                )
            "Moon" ->
                Orbit(
                    125.1228 - .0529538083 * d,
                    5.1454,
                    318.0634 + .1643573223 * d,
                    60.2666,
                    .0549,
                    115.3654 + 13.0649929509 * d,
                )
            "Mercury" ->
                Orbit(
                    48.3313 + 3.24587e-5 * d,
                    7.0047 + 5e-8 * d,
                    29.1241 + 1.01444e-5 * d,
                    .387098,
                    .205635 + 5.59e-10 * d,
                    168.6562 + 4.0923344368 * d,
                )
            "Venus" ->
                Orbit(
                    76.6799 + 2.4659e-5 * d,
                    3.3946 + 2.75e-8 * d,
                    54.8910 + 1.38374e-5 * d,
                    .723330,
                    .006773 - 1.302e-9 * d,
                    48.0052 + 1.6021302244 * d,
                )
            "Mars" ->
                Orbit(
                    49.5574 + 2.11081e-5 * d,
                    1.8497 - 1.78e-8 * d,
                    286.5016 + 2.92961e-5 * d,
                    1.523688,
                    .093405 + 2.516e-9 * d,
                    18.6021 + .5240207766 * d,
                )
            "Jupiter" ->
                Orbit(
                    100.4542 + 2.76854e-5 * d,
                    1.3030 - 1.557e-7 * d,
                    273.8777 + 1.64505e-5 * d,
                    5.20256,
                    .048498 + 4.469e-9 * d,
                    19.8950 + .0830853001 * d,
                )
            else ->
                Orbit(
                    113.6634 + 2.38980e-5 * d,
                    2.4886 - 1.081e-7 * d,
                    339.3939 + 2.97661e-5 * d,
                    9.55475,
                    .055546 - 9.499e-9 * d,
                    316.9670 + .0334442282 * d,
                )
        }

    private fun xyz(o: Orbit): DoubleArray {
        val m = norm(o.m)
        var e = m + Math.toDegrees(o.e * sinD(m) * (1 + o.e * cosD(m)))
        repeat(12) { e -= (e - Math.toDegrees(o.e * sinD(e)) - m) / (1 - o.e * cosD(e)) }
        val x = o.a * (cosD(e) - o.e)
        val y = o.a * sqrt(1 - o.e * o.e) * sinD(e)
        val v = atanD(y, x)
        val r = hypot(x, y)
        val u = v + o.w
        return doubleArrayOf(
            r * (cosD(o.n) * cosD(u) - sinD(o.n) * sinD(u) * cosD(o.i)),
            r * (sinD(o.n) * cosD(u) + cosD(o.n) * sinD(u) * cosD(o.i)),
            r * sinD(u) * sinD(o.i),
        )
    }

    fun tropical(d: Double): Map<String, Double> {
        val sun = xyz(orbit("Sun", d))
        val so = orbit("Sun", d)
        val mo = orbit("Moon", d)
        val ls = so.m + so.w
        val lm = mo.m + mo.w + mo.n
        val dd = lm - ls
        val f = lm - mo.n
        val result =
            names
                .take(7)
                .associateWith { p ->
                    val v = xyz(orbit(p, d))
                    var lon =
                        if (p == "Sun" || p == "Moon") atanD(v[1], v[0])
                        else atanD(v[1] + sun[1], v[0] + sun[0])
                    if (p == "Moon")
                        lon +=
                            -1.274 * sinD(mo.m - 2 * dd) + .658 * sinD(2 * dd) -
                                .186 * sinD(so.m) -
                                .059 * sinD(2 * mo.m - 2 * dd) -
                                .057 * sinD(mo.m - 2 * dd + so.m) +
                                .053 * sinD(mo.m + 2 * dd) +
                                .046 * sinD(2 * dd - so.m) +
                                .041 * sinD(mo.m - so.m) -
                                .035 * sinD(dd) -
                                .031 * sinD(mo.m + so.m) -
                                .015 * sinD(2 * f - 2 * dd) + .011 * sinD(mo.m - 4 * dd)
                    // The dominant Jupiter/Saturn mutual perturbations in ecliptic longitude.
                    val j = orbit("Jupiter", d).m
                    val s = orbit("Saturn", d).m
                    if (p == "Jupiter")
                        lon +=
                            -.332 * sinD(2 * j - 5 * s - 67.6) - .056 * sinD(2 * j - 2 * s + 21) +
                                .042 * sinD(3 * j - 5 * s + 21) - .036 * sinD(j - 2 * s) +
                                .022 * cosD(j - s) +
                                .023 * sinD(2 * j - 3 * s + 52) - .016 * sinD(j - 5 * s - 69)
                    if (p == "Saturn")
                        lon +=
                            .812 * sinD(2 * j - 5 * s - 67.6) - .229 * cosD(2 * j - 4 * s - 2) +
                                .119 * sinD(j - 2 * s - 3) +
                                .046 * sinD(2 * j - 6 * s - 69) +
                                .014 * sinD(j - 3 * s + 32)
                    norm(lon)
                }
                .toMutableMap()
        result["Rahu"] = norm(mo.n)
        result["Ketu"] = norm(mo.n + 180)
        return result
    }

    fun calculate(b: Birth): Chart {
        val jd = julian(b.instant)
        val d = jd - 2451543.5
        val ay = ayanamsa(d)
        val t = tropical(d)
        val next = tropical(d + 1)
        val lst = norm(280.46061837 + 360.98564736629 * (jd - 2451545) + b.place.lon)
        val ob = 23.4393 - 3.563e-7 * d
        val asc =
            norm(
                atanD(
                    -cosD(lst),
                    sinD(lst) * cosD(ob) + tan(Math.toRadians(b.place.lat)) * sinD(ob),
                ) + 180 - ay
            )
        val a = (asc / 30).toInt()
        val limits =
            mapOf(
                "Moon" to 12,
                "Mars" to 17,
                "Mercury" to 14,
                "Venus" to 10,
                "Jupiter" to 11,
                "Saturn" to 15,
            )
        val ps =
            names.map { n ->
                val lon = norm(t.getValue(n) - ay)
                Position(
                    n,
                    lon,
                    ((lon / 30).toInt() - a + 12) % 12 + 1,
                    ((next.getValue(n) - t.getValue(n) + 540) % 360 - 180) < 0,
                    limits[n]?.let { separation(t.getValue(n), t.getValue("Sun")) < it } ?: false,
                )
            }
        return Chart(b, jd, ay, ps + Position("Lagna", asc, 1))
    }

    fun vimshottari(c: Chart): List<Period> {
        val index = c.moon.nak % 9
        val elapsed = (c.moon.longitude % (40.0 / 3)) / (40.0 / 3)
        var start =
            c.birth.instant.minusSeconds((elapsed * dashaYears[index] * 365.25 * 86400).toLong())
        return (0..17).map {
            val k = (index + it) % 9
            val end = start.plusSeconds((dashaYears[k] * 365.25 * 86400).toLong())
            Period(dashaLords[k], start, end).also { start = end }
        }
    }

    fun antardashas(p: Period): List<Period> {
        val k = dashaLords.indexOf(p.lord)
        var start = p.start
        return (0..8).map {
            val i = (k + it) % 9
            val end =
                if (it == 8) p.end
                else
                    start.plusSeconds(
                        ((p.end.epochSecond - p.start.epochSecond) * dashaYears[i] / 120.0).toLong()
                    )
            Period(dashaLords[i], start, end).also { start = end }
        }
    }

    fun chara(c: Chart): List<Period> {
        var start = c.birth.instant
        val direction = if (c.asc.sign % 2 == 0) 1 else -1
        return (0..11).map {
            val s = (c.asc.sign + direction * it + 120) % 12
            val distance = (direction * (c.p(rulers[s]).sign - s) + 120) % 12
            val years = if (distance == 0) 12 else distance
            val end = start.plusSeconds((years * 365.25 * 86400).toLong())
            Period(signs[s], start, end).also { start = end }
        }
    }
}

fun aspects(p: Position, targetHouse: Int): Boolean {
    val distance = (targetHouse - p.house + 12) % 12 + 1
    return distance in
        when (p.name) {
            "Mars" -> listOf(1, 4, 7, 8)
            "Jupiter" -> listOf(1, 5, 7, 9)
            "Saturn" -> listOf(1, 3, 7, 10)
            else -> listOf(1, 7)
        }
}
