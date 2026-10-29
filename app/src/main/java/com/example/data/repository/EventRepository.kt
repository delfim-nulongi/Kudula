package com.example.data.repository

import com.example.data.local.dao.KudulaEventDao
import com.example.data.local.entity.KudulaEvent
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/**
 * Repositório para o Kudula Event Log.
 */
class EventRepository(private val dao: KudulaEventDao) {

    val allEvents: Flow<List<KudulaEvent>> = dao.getAllEvents()
    val unsyncedEvents: Flow<List<KudulaEvent>> = dao.getUnsyncedEvents()

    fun getRecentEvents(limit: Int = 50): Flow<List<KudulaEvent>> = dao.getRecentEvents(limit)

    fun getEventsByType(type: String): Flow<List<KudulaEvent>> = dao.getEventsByType(type)

    suspend fun logEvent(
        eventType: String,
        details: String,
        source: String = "LOCAL",
        metadataJson: String? = null
    ): KudulaEvent {
        val event = KudulaEvent(
            uuid = UUID.randomUUID().toString(),
            eventType = eventType,
            details = details,
            source = source,
            timestamp = System.currentTimeMillis(),
            synced = false,
            metadataJson = metadataJson
        )
        dao.insertEvent(event)
        return event
    }

    suspend fun markAsSynced(uuid: String) {
        dao.markAsSynced(uuid)
    }

    suspend fun markAllAsSynced() {
        dao.markAllAsSynced()
    }

    suspend fun seedInitialEventsIfEmpty() {
        // Reservado estritamente para testes; contas de produção começam 100% limpas
    }
}
