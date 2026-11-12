package com.astro.kundali

import com.astro.kundali.astro.*
import com.astro.kundali.content.*
import java.time.*
import org.junit.Assert.*
import org.junit.Test

class InterpretationTest {
    private fun fixture(jupiter: Double = 95.0): Chart {
        val longitudes = listOf(125.0, 5.0, 128.0, 35.0, 185.0, jupiter, 305.0, 65.0, 245.0)
        return Chart(
            Cities.sample,
            0.0,
            23.0,
            names.zip(longitudes).map { (name, lon) ->
                Position(name, lon, (lon / 30).toInt() + 1)
            } + Position("Lagna", 5.0, 1),
        )
    }

    @Test
    fun explicitYogaDefinitionsAndCancellation() {
        val found = yogas(fixture()).associateBy { it.name }
        assertTrue(found.getValue("Gajakesari").present)
        assertTrue(found.getValue("Budha-Aditya").present)
        assertTrue(found.getValue("Chandra-Mangala").present)
        assertFalse(found.getValue("Kemadruma check").present)
        assertFalse(yogas(fixture(65.0)).first { it.name == "Gajakesari" }.present)
        assertTrue(yogas(fixture(275.0)).first { it.name == "Amala" }.present)
        assertTrue(found.values.all { it.verify.isNotBlank() && it.explanation.isNotBlank() })
    }

    @Test
    fun dignityAndSevenKaraka() {
        assertEquals("Exalted", Position("Moon", 35.0, 1).dignity)
        assertEquals("Debilitated", Position("Moon", 215.0, 1).dignity)
        assertEquals("Own sign", Position("Sun", 125.0, 1).dignity)
        assertEquals("Mercury", fixture().atmakaraka.name)
    }

    @Test
    fun historicalZoneAndDaylightSaving() {
        val toronto = Cities.all.first { it.name == "Toronto" }
        assertEquals(
            Instant.parse("2000-07-01T16:00:00Z"),
            Birth("Summer", LocalDate.of(2000, 7, 1), LocalTime.NOON, toronto).instant,
        )
        assertEquals(
            Instant.parse("2000-01-01T17:00:00Z"),
            Birth("Winter", LocalDate.of(2000, 1, 1), LocalTime.NOON, toronto).instant,
        )
    }

    @Test
    fun charaDirectionAndOwnSignDuration() {
        val sample = Engine.calculate(Cities.sample)
        assertEquals("Taurus", Engine.chara(sample).first().lord)
        assertEquals("Aries", Engine.chara(sample)[1].lord)
        val own =
            fixture().let { c ->
                c.copy(
                    positions =
                        c.positions.map { if (it.name == "Mars") Position("Mars", 5.0, 1) else it }
                )
            }
        val first = Engine.chara(own).first()
        assertEquals(
            (12 * 365.25 * 86400).toLong(),
            first.end.epochSecond - first.start.epochSecond,
        )
    }

    @Test
    fun transitCycleIsContextualized() {
        val entries = transits(fixture(), Instant.parse("2026-11-12T12:00:00Z")).toMap()
        assertTrue(entries.getValue("Sade Sati").contains("First phase"))
        assertTrue(entries.getValue("Double transit").contains("Saturn AND Jupiter"))
        assertTrue(entries.getValue("Sade Sati").contains("not a threat"))
    }
}
