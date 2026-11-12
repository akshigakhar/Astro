package com.astro.kundali

import com.astro.kundali.astro.*
import com.astro.kundali.content.*
import java.time.*
import org.junit.Assert.*
import org.junit.Test

class VargaStudyTest {
    private val chart = Engine.calculate(Cities.sample)

    @Test
    fun d10StartsAndLastPortionsForEverySign() {
        val starts = listOf(0, 9, 2, 11, 4, 1, 6, 3, 8, 5, 10, 7)
        val ends = listOf(9, 6, 11, 8, 1, 10, 3, 0, 5, 2, 7, 4)
        for (s in 0..11) {
            assertEquals("start of ${signs[s]}", starts[s], dashamsha(s * 30.0))
            assertEquals("last portion of ${signs[s]}", ends[s], dashamsha(s * 30.0 + 29.999999))
        }
    }

    @Test
    fun d10EveryThreeDegreeBoundaryIsHalfOpen() {
        val aries = listOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9)
        val taurus = listOf(9, 10, 11, 0, 1, 2, 3, 4, 5, 6)
        listOf(0.0 to aries, 30.0 to taurus).forEach { (start, expected) ->
            for (i in 1..9) {
                val boundary = start + i * 3
                assertEquals(expected[i - 1], dashamsha(boundary - 1e-7))
                assertEquals(expected[i], dashamsha(boundary))
                assertEquals(expected[i], dashamsha(boundary + 1e-7))
            }
        }
        assertEquals(9, dashamsha(30.0))
        assertEquals(2, dashamsha(60.0))
    }

    @Test
    fun d10WrapsAndRejectsNonFiniteInput() {
        assertEquals(0, dashamsha(360.0))
        assertEquals(0, dashamsha(720.0))
        assertEquals(4, dashamsha(-0.0001))
        assertEquals(dashamsha(35.0), dashamsha(395.0))
        listOf(Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY).forEach {
            assertThrows(IllegalArgumentException::class.java) { dashamsha(it) }
        }
    }

    @Test
    fun d10UsesItsOwnLagnaAndHouses() {
        val v = chart.varga(10)
        assertEquals(11, v.ascSign) // Taurus 7°43' -> Pisces.
        assertEquals(1, v.positions.first { it.name == "Lagna" }.house)
        assertEquals(0, v.positions.first { it.name == "Moon" }.sign)
        assertEquals(2, v.positions.first { it.name == "Moon" }.house)
        assertEquals(10, v.positions.first { it.name == "Sun" }.house)
        assertEquals(9, v.positions.count { it.name != "Lagna" })
        assertEquals(chart.asc.sign, chart.varga(1).ascSign)
        assertEquals(chart.asc.d9, chart.varga(9).ascSign)
        assertThrows(IllegalStateException::class.java) { chart.varga(7) }
    }

    @Test
    fun vargottamaDependsOnSignNotDegreeOrHouse() {
        assertTrue(Position("Sun", 1.0, 10).vargottama)
        assertFalse(Position("Sun", 4.0, 1).vargottama)
        assertTrue(Position("Moon", 359.0, 3).vargottama)
    }

    @Test
    fun worksheetChecksBirthAndRejectsInvalidDates() {
        val e = checkEvent(chart, chart.birth.date, LocalDate.of(2026, 11, 12))
        assertEquals("Saturn", e.md!!.lord)
        assertNotNull(e.ad)
        assertEquals("Taurus", e.chara!!.lord)
        assertTrue(eventReading(chart, e).contains("owns natal houses"))
        assertThrows(IllegalArgumentException::class.java) {
            checkEvent(chart, LocalDate.of(1947, 8, 14))
        }
        assertThrows(IllegalArgumentException::class.java) {
            checkEvent(chart, LocalDate.of(2027, 1, 1), LocalDate.of(2026, 11, 12))
        }
    }

    @Test
    fun worksheetFlagsDateOfMajorPeriodChange() {
        val transition = Engine.vimshottari(chart)[1]
        val day = transition.start.atZone(ZoneId.of(chart.birth.place.zone)).toLocalDate()
        val e = checkEvent(chart, day, LocalDate.of(2026, 11, 12))
        assertTrue(e.transitions.any { it.startsWith("MD → ${transition.lord}") })
        assertTrue(eventReading(chart, e).contains("check the actual event time"))
        assertFalse(Engine.vimshottari(chart).first().active(transition.start))
        assertTrue(transition.active(transition.start))
    }

    @Test
    fun worksheetDoesNotInventCharaBeyondItsImplementedCycle() {
        val end =
            Engine.chara(chart)
                .last()
                .end
                .atZone(ZoneId.of(chart.birth.place.zone))
                .toLocalDate()
                .plusDays(1)
        val e = checkEvent(chart, end, end)
        assertNull(e.chara)
        assertTrue(eventReading(chart, e).contains("outside the introductory twelve-sign cycle"))
    }

    @Test
    fun summaryIncludesVargasAndConventionsWithoutWorksheetEvents() {
        val text = kundaliSummary(chart, Instant.parse("2000-01-01T00:00:00Z"))
        listOf(
                "India",
                "1947-08-15",
                "Asia/Kolkata",
                "D1",
                "D9",
                "D10",
                "Running Vimshottari",
                "Lahiri",
                "mean node",
                "cross-check",
            )
            .forEach { assertTrue(it, text.contains(it)) }
        assertTrue(text.contains("As of 2000-01-01T00:00:00Z"))
        assertEquals(10, text.lines().count { it.contains(Regex("D1 .*H[0-9]+; D9")) })
    }
}
