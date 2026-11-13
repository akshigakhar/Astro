package com.astro.kundali

import com.astro.kundali.accounts.*
import com.astro.kundali.astro.*
import org.junit.Assert.*
import org.junit.Test

class SaveLimitTest {
    @Test
    fun zeroOneTwoSavesAllowedThirdRejected() {
        assertEquals(1, SaveLimit.afterCreate(0))
        assertEquals(2, SaveLimit.afterCreate(1))
        val error = assertThrows(IllegalStateException::class.java) { SaveLimit.afterCreate(2) }
        assertEquals(LIMIT_MESSAGE, error.message)
    }

    @Test
    fun deleteFreesExactlyOneSlotAndNeverUnderflows() {
        val afterDelete = SaveLimit.afterDelete(2)
        assertEquals(1, afterDelete)
        assertEquals(2, SaveLimit.afterCreate(afterDelete))
        assertEquals(0, SaveLimit.afterDelete(1))
        assertThrows(IllegalArgumentException::class.java) { SaveLimit.afterDelete(0) }
        assertThrows(IllegalArgumentException::class.java) { SaveLimit.afterCreate(-1) }
    }

    @Test
    fun ownerOwnSaveCountIsUncappedButStillCounted() {
        assertEquals(3, SaveLimit.afterCreate(2, true))
        assertEquals(101, SaveLimit.afterCreate(100, true))
        assertEquals(100, SaveLimit.afterDelete(101))
    }

    @Test
    fun storedBirthDetailsRoundTripAndDoNotContainCalculatedTables() {
        val saved = SavedBirth.from(Cities.sample, "India")
        assertEquals(Cities.sample, saved.toBirth())
        assertEquals("00:00", saved.birthTime)
        assertEquals(
            setOf(
                "personName",
                "birthDate",
                "birthTime",
                "placeName",
                "country",
                "latitude",
                "longitude",
                "timeZoneId",
            ),
            saved.fields().keys,
        )
        val before = Engine.calculate(Cities.sample)
        val after = Engine.calculate(saved.toBirth())
        assertEquals(before.positions, after.positions)
    }

    @Test
    fun storedBirthDetailsRejectInvalidCoordinatesDatesAndZones() {
        val base = SavedBirth.from(Cities.sample, "India")
        listOf(
                base.copy(latitude = 91.0),
                base.copy(longitude = Double.NaN),
                base.copy(timeZoneId = "No/SuchZone"),
                base.copy(birthDate = "1947-02-31"),
                base.copy(birthTime = "27:00"),
                base.copy(country = ""),
            )
            .forEach { value -> assertThrows(Exception::class.java) { value.toBirth() } }
    }

    @Test
    fun cacheRowsAreScopedByUserAndPreserveBirthDetailsOnly() {
        val saved =
            SavedKundali("user-a", "chart-a", SavedBirth.from(Cities.sample, "India"), 100, 200)
        assertEquals(saved, CachedKundali.from(saved).saved())
        assertNotEquals(CachedKundali.from(saved), CachedKundali.from(saved.copy(uid = "user-b")))
        val other = saved.copy(uid = "user-b")
        assertEquals(listOf(saved), visibleSavedKundalis("user-a", listOf(saved, other)))
        assertEquals(listOf(other), visibleSavedKundalis("user-b", listOf(saved, other)))
        assertTrue(visibleSavedKundalis(null, listOf(saved, other)).isEmpty())
    }
}
