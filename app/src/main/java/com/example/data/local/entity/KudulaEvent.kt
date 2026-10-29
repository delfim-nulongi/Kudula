package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Kudula Event Log
 * Sistema interno de eventos para rastreamento de marcos e telemetria cognitiva.
 * Preparado para futura sincronização com Firebase/PostgreSQL.
 */
@Entity(tableName = "kudula_events")
data class KudulaEvent(
    @PrimaryKey
    val uuid: String = UUID.randomUUID().toString(),
    val eventType: String,
    val details: String,
    val source: String = "LOCAL",
    val timestamp: Long = System.currentTimeMillis(),
    val synced: Boolean = false,
    val metadataJson: String? = null
) {
    companion object {
        const val TYPE_PDF_OPENED = "PDF_OPENED"
        const val TYPE_YOUTUBE_PROCESSED = "YOUTUBE_PROCESSED"
        const val TYPE_QUIZ_COMPLETED = "QUIZ_COMPLETED"
        const val TYPE_CHAPTER_READ = "CHAPTER_READ"
        const val TYPE_GOAL_COMPLETED = "GOAL_COMPLETED"
        const val TYPE_REFLECTION_CREATED = "REFLECTION_CREATED"
        const val TYPE_KUDU_ENCOURAGED = "KUDU_ENCOURAGED"
        const val TYPE_CAMERA_CAPTURED = "CAMERA_CAPTURED"
    }
}
