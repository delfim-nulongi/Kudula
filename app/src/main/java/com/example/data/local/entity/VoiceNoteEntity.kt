package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Entidade para o módulo Kudu Voice Notes (F0.5+).
 * Regista perguntas orais curtas de até 60 segundos com processamento KDAP e resposta inteligente.
 */
@Entity(tableName = "voice_notes")
data class VoiceNoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val uuid: String = UUID.randomUUID().toString(),
    val durationSeconds: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val audioPath: String = "",
    val questionTranscript: String = "",
    val aiResponseText: String = "",
    val status: String = STATUS_ANSWERED, // RECORDED, PROCESSING, ANSWERED
    val synced: Boolean = false
) {
    companion object {
        const val STATUS_RECORDED = "RECORDED"
        const val STATUS_PROCESSING = "PROCESSING"
        const val STATUS_ANSWERED = "ANSWERED"
    }
}
