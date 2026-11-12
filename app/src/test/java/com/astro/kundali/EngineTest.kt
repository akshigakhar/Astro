package com.astro.kundali

import com.astro.kundali.astro.*
import com.astro.kundali.content.*
import java.time.*
import org.junit.Assert.*
import org.junit.Test

class EngineTest {
    private val chart = Engine.calculate(Cities.sample)

    @Test
    fun independenceAcceptance() {
        // Independent Swiss Ephemeris 2.10.03, Moshier, Lahiri, mean node.
        // UTC 1947-08-14 18:30, JD 2432412.2708333335; no ephemeris dependency in app/tests.
        val expected =
            mapOf(
                "Lagna" to 37.7323,
                "Sun" to 117.9890,
                "Moon" to 93.9835,
                "Mercury" to 103.6743,
                "Venus" to 112.5612,
                "Saturn" to 110.4730,
                "Mars" to 67.4562,
                "Jupiter" to 205.8777,
                "Rahu" to 35.0697,
            )
        val expectedSigns =
            mapOf(
                "Lagna" to 1,
                "Sun" to 3,
                "Moon" to 3,
                "Mercury" to 3,
                "Venus" to 3,
                "Saturn" to 3,
                "Mars" to 2,
                "Jupiter" to 6,
                "Rahu" to 1,
            )
        for ((name, lon) in expected) {
            val p = chart.p(name)
            assertEquals("$name longitude ${p.longitude}", lon, p.longitude, 2.0)
            assertEquals(name, expectedSigns.getValue(name), p.sign)
        }
        assertEquals("Pushya", nakshatras[chart.moon.nak])
        assertEquals("Saturn", Engine.vimshottari(chart).first().lord)
    }

    @Test
    fun utcAndEpoch() {
        assertEquals(Instant.parse("1947-08-14T18:30:00Z"), Cities.sample.instant)
        assertEquals(2451544.5, Engine.julian(Instant.parse("2000-01-01T00:00:00Z")), 1e-9)
        assertEquals(23.85708, Engine.ayanamsa(1.0), 1e-6)
    }

    @Test
    fun nakshatraPada() {
        val p = Position("Moon", 100.0, 1)
        assertEquals(7, p.nak)
        assertEquals(3, p.pada)
        assertEquals("Saturn", p.nakLord)
        assertEquals(0, Position("Moon", 0.0, 1).nak)
        assertEquals(26, Position("Moon", 359.99, 1).nak)
        assertEquals(4, Position("Moon", 359.99, 1).pada)
    }

    @Test
    fun navamsaRules() {
        assertEquals(0, navamsa(0.0))
        assertEquals(8, navamsa(29.99))
        assertEquals(9, navamsa(30.0))
        assertEquals(6, navamsa(60.0))
        assertEquals(3, navamsa(90.0))
        assertEquals(11, navamsa(359.99))
    }

    @Test
    fun dashaContinuityAndBalance() {
        val ps = Engine.vimshottari(chart)
        assertTrue(ps.first().start.isBefore(chart.birth.instant))
        assertTrue(ps.first().end.isAfter(chart.birth.instant))
        ps.zipWithNext().forEach { (a, b) -> assertEquals(a.end, b.start) }
        ps.forEach { p ->
            val subs = Engine.antardashas(p)
            assertEquals(p.start, subs.first().start)
            assertEquals(p.end, subs.last().end)
            subs.zipWithNext().forEach { (a, b) -> assertEquals(a.end, b.start) }
        }
    }

    @Test
    fun anglesAndNodes() {
        for (i in listOf(-720.0, -1.0, 0.0, 359.0, 721.0)) assertTrue(norm(i) in 0.0..<360.0)
        assertEquals(2.0, separation(359.0, 1.0), 1e-9)
        assertEquals(180.0, separation(chart.p("Rahu").longitude, chart.p("Ketu").longitude), 1e-8)
        assertEquals(12, Engine.chara(chart).size)
    }

    @Test
    fun aspectsCountInclusively() {
        val mars = Position("Mars", 0.0, 2)
        assertTrue(aspects(mars, 5))
        assertTrue(aspects(mars, 9))
        assertFalse(aspects(mars, 6))
        assertTrue(aspects(Position("Jupiter", 0.0, 2), 10))
        assertTrue(aspects(Position("Saturn", 0.0, 2), 11))
    }

    @Test
    fun citiesAndCurriculumComplete() {
        assertTrue(Cities.all.size >= 60)
        Cities.all.forEach {
            ZoneId.of(it.zone)
            assertTrue(it.lat in -90.0..90.0)
            assertTrue(it.lon in -180.0..180.0)
        }
        assertEquals(14, lessons.size)
        lessons.forEach { assertTrue(it.text.split(Regex("\\s+")).size in 250..450) }
        assertEquals(16, glossary.size)
    }
}
