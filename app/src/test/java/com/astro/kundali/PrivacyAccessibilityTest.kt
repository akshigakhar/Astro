package com.astro.kundali

import com.astro.kundali.astro.*
import com.astro.kundali.content.*
import org.junit.Assert.*
import org.junit.Test

class PrivacyAccessibilityTest {
    private val chart = Engine.calculate(Cities.sample)

    @Test
    fun chartDescriptionIncludesEveryHouseAndFullPlanetNamesInAllVargas() {
        listOf(1, 9, 10).forEach { division ->
            val view = chart.varga(division)
            val text = chartDescription(view)
            assertTrue(
                text.startsWith("North Indian D$division chart. Lagna ${signs[view.ascSign]}")
            )
            (1..12).forEach { house ->
                assertTrue(text.contains("House $house,"))
                assertTrue(
                    chartHouseDescription(view, house).contains(signs[view.signOfHouse(house)])
                )
            }
            names.forEach { assertTrue(it, text.contains(it)) }
            assertTrue(text.contains("no planets"))
        }
    }

    @Test
    fun selectedHouseDescriptionReportsHighlightsAndActualOccupants() {
        val view = chart.varga(1)
        val text = chartHouseDescription(view, 3, setOf(3), setOf("Moon"))
        assertTrue(text.startsWith("House 3, Cancer, highlighted"))
        assertTrue(text.contains("Moon, highlighted"))
        assertTrue(text.contains("Saturn"))
        assertThrows(IllegalArgumentException::class.java) { chartHouseDescription(view, 0) }
    }

    @Test
    fun privacyExplainsLocalStorageExportExceptionsAndAdviceLimits() {
        val text = aboutSections.values.joinToString(" ")
        listOf(
                "Firebase Auth",
                "analytics",
                "network access",
                "SharedPreferences",
                "Clear storage",
                "medical",
                "financial",
                "educational",
                "subsequent transfer",
                "backup is disabled",
                "owner/admin",
                "birth details only",
            )
            .forEach { assertTrue(it, text.contains(it, ignoreCase = true)) }
        assertTrue(text.contains(accuracy))
    }
}
