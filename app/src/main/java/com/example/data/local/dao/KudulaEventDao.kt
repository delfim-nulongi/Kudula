package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.KudulaEvent
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object para o Kudula Event Log.
 */
@Dao
interface KudulaEventDao {

    @Query("SELECT * FROM kudula_events ORDER BY timestamp DESC")
    fun getAllEvents(): Flow<List<KudulaEvent>>

    @Query("SELECT * FROM kudula_events ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentEvents(limit: Int = 50): Flow<List<KudulaEvent>>

    @Query("SELECT * FROM kudula_events WHERE eventType = :eventType ORDER BY timestamp DESC")
    fun getEventsByType(eventType: String): Flow<List<KudulaEvent>>

    @Query("SELECT * FROM kudula_events WHERE synced = 0 ORDER BY timestamp ASC")
    fun getUnsyncedEvents(): Flow<List<KudulaEvent>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: KudulaEvent)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<KudulaEvent>)

    @Delete
    suspend fun deleteEvent(event: KudulaEvent)

    @Query("UPDATE kudula_events SET synced = 1 WHERE uuid = :uuid")
    suspend fun markAsSynced(uuid: String)

    @Query("UPDATE kudula_events SET synced = 1")
    suspend fun markAllAsSynced()

    @Query("DELETE FROM kudula_events")
    suspend fun clearAll()
}
