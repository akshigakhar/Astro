package com.astro.kundali.accounts

import com.astro.kundali.astro.*
import java.time.*

const val SAVE_LIMIT = 2
const val LIMIT_MESSAGE = "You can save 2 kundalis. Delete one to save another."

object SaveLimit {
    fun afterCreate(count: Int, admin: Boolean = false): Int {
        require(count >= 0 && count < Int.MAX_VALUE) { "Invalid saved count." }
        check(admin || count < SAVE_LIMIT) { LIMIT_MESSAGE }
        return count + 1
    }

    fun afterDelete(count: Int): Int {
        require(count > 0) { "Saved count must be positive before deletion." }
        return count - 1
    }
}

data class AccountSession(
    val uid: String,
    val email: String,
    val displayName: String,
    val admin: Boolean = false,
    val provider: String = "",
)

data class UserProfile(
    val uid: String,
    val email: String,
    val displayName: String,
    val kundaliCount: Int,
    val createdAt: Long,
)

data class SavedBirth(
    val personName: String,
    val birthDate: String,
    val birthTime: String,
    val placeName: String,
    val country: String,
    val latitude: Double,
    val longitude: Double,
    val timeZoneId: String,
) {
    fun toBirth(): Birth {
        require(
            personName.isNotBlank() &&
                personName.length <= 120 &&
                placeName.isNotBlank() &&
                placeName.length <= 120 &&
                country.isNotBlank() &&
                country.length <= 80
        ) {
            "Enter a name, place and country."
        }
        require(
            latitude.isFinite() &&
                latitude in -89.9..89.9 &&
                longitude.isFinite() &&
                longitude in -180.0..180.0
        ) {
            "Check latitude and longitude."
        }
        val date = LocalDate.parse(birthDate)
        require(date.year >= 1900 && !date.isAfter(LocalDate.now())) {
            "Birth date must be from 1900 through today."
        }
        require(birthTime.matches(Regex("[0-2][0-9]:[0-5][0-9]"))) { "Use HH:mm (24-hour time)." }
        val time = LocalTime.parse(birthTime)
        val zone = ZoneId.of(timeZoneId)
        require(zone.rules.getValidOffsets(LocalDateTime.of(date, time)).isNotEmpty()) {
            "That local birth time did not exist because clocks changed."
        }
        return Birth(
            personName.trim(),
            date,
            time,
            Place(placeName.trim(), latitude, longitude, timeZoneId),
        )
    }

    fun fields(): Map<String, Any> =
        mapOf(
            "personName" to personName.trim(),
            "birthDate" to birthDate,
            "birthTime" to birthTime,
            "placeName" to placeName.trim(),
            "country" to country.trim(),
            "latitude" to latitude,
            "longitude" to longitude,
            "timeZoneId" to timeZoneId,
        )

    companion object {
        fun from(birth: Birth, country: String) =
            SavedBirth(
                birth.name,
                birth.date.toString(),
                birth.time.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")),
                birth.place.name,
                country,
                birth.place.lat,
                birth.place.lon,
                birth.place.zone,
            )
    }
}

data class SavedKundali(
    val uid: String,
    val id: String,
    val birth: SavedBirth,
    val createdAt: Long,
    val updatedAt: Long,
)

fun defaultCountry(zone: String) =
    when (zone) {
        "Asia/Kolkata" -> "India"
        "America/Toronto",
        "America/Vancouver",
        "America/Edmonton" -> "Canada"
        "Europe/London" -> "United Kingdom"
        "Asia/Dubai" -> "United Arab Emirates"
        "Asia/Singapore" -> "Singapore"
        "Asia/Kathmandu" -> "Nepal"
        "Asia/Colombo" -> "Sri Lanka"
        "Asia/Dhaka" -> "Bangladesh"
        "Asia/Karachi" -> "Pakistan"
        "Australia/Sydney",
        "Australia/Melbourne",
        "Australia/Brisbane",
        "Australia/Perth" -> "Australia"
        "Pacific/Auckland" -> "New Zealand"
        "America/New_York",
        "America/Los_Angeles",
        "America/Chicago" -> "United States"
        else -> ""
    }
