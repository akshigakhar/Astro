package com.astro.kundali.accounts

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "kundalis", primaryKeys = ["uid", "id"])
data class CachedKundali(
    val uid: String,
    val id: String,
    val personName: String,
    val birthDate: String,
    val birthTime: String,
    val placeName: String,
    val country: String,
    val latitude: Double,
    val longitude: Double,
    val timeZoneId: String,
    val createdAt: Long,
    val updatedAt: Long,
) {
    fun saved() =
        SavedKundali(
            uid,
            id,
            SavedBirth(
                personName,
                birthDate,
                birthTime,
                placeName,
                country,
                latitude,
                longitude,
                timeZoneId,
            ),
            createdAt,
            updatedAt,
        )

    companion object {
        fun from(k: SavedKundali) =
            with(k.birth) {
                CachedKundali(
                    k.uid,
                    k.id,
                    personName,
                    birthDate,
                    birthTime,
                    placeName,
                    country,
                    latitude,
                    longitude,
                    timeZoneId,
                    k.createdAt,
                    k.updatedAt,
                )
            }
    }
}

@Dao
abstract class KundaliDao {
    @Query("SELECT * FROM kundalis WHERE uid = :uid ORDER BY updatedAt DESC")
    abstract fun observe(uid: String): Flow<List<CachedKundali>>

    @Query("DELETE FROM kundalis") abstract suspend fun clear()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insert(rows: List<CachedKundali>)

    @Transaction
    open suspend fun replace(uid: String, rows: List<CachedKundali>) {
        require(rows.all { it.uid == uid })
        clear()
        insert(rows)
    }
}

@Database(entities = [CachedKundali::class], version = 1, exportSchema = true)
abstract class KundaliCache : RoomDatabase() {
    abstract fun kundalis(): KundaliDao
}
