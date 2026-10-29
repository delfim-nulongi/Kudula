package com.example.data.repository

import com.example.data.local.dao.VoiceNoteDao
import com.example.data.local.entity.VoiceNoteEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * Repositório para o módulo Kudu Voice Notes (F0.5+).
 * Gere a gravação local, persistência em Room e processamento inteligente com o KDAP.
 */
class VoiceNoteRepository(
    private val voiceNoteDao: VoiceNoteDao
) {

    fun getAllVoiceNotes(): Flow<List<VoiceNoteEntity>> = voiceNoteDao.getAll()

    suspend fun getById(id: Long): VoiceNoteEntity? = withContext(Dispatchers.IO) {
        voiceNoteDao.getById(id)
    }

    /**
     * Guarda uma nova gravação no Room e simula o processamento do KDAP para obter resposta socrática.
     */
    suspend fun recordAndProcessVoiceNote(
        durationSeconds: Int,
        audioPath: String = ""
    ): VoiceNoteEntity = withContext(Dispatchers.IO) {
        // Simulação do pipeline de transcrição e geração de resposta do tutor Kudu
        delay(600)

        val sampleTranscripts = listOf(
            "Kudu, podes explicar-me como conectar este conceito com a minha meta diária?",
            "Como é que posso aplicar este tema no meu projeto prático?",
            "Qual é a melhor técnica de revisão para reter esta fórmula a longo prazo?",
            "Estou a ter dificuldades em concentrar-me neste módulo. Que estratégia recomendas?",
            "Kudu, qual é a síntese dos pontos essenciais deste capítulo?"
        )

        val sampleResponses = listOf(
            "Excelente reflexão oral! Com base no teu perfil cognitivo e no teu IPA atual, focar 15 minutos em síntese ativa antes de rever consolida a memória a longo prazo com menor esforço.",
            "Boa pergunta! Sugiro transformar este conceito num mapa mental com três ramificações práticas conectadas aos teus objetivos profissionais.",
            "Para fixação ótima com o teu ritmo de aprendizagem, aplica o método da Recordação Ativa: fecha o material e explica em voz alta como se ensinasses a um colega.",
            "O teu foco beneficia de pausas estratégicas. Experimenta o ritmo ultradiano: 25 minutos de estudo imersivo seguidos de 5 minutos de respiração consciente.",
            "Os pontos-chave são: 1. Compreensão do princípio fundamental; 2. Prática deliberada; 3. Auto-avaliação reflexiva. O teu IKC vai refletir este avanço!"
        )

        val index = (0 until sampleTranscripts.size).random()
        val transcript = sampleTranscripts[index]
        val answer = sampleResponses[index]

        val entity = VoiceNoteEntity(
            uuid = UUID.randomUUID().toString(),
            durationSeconds = durationSeconds.coerceIn(1, 60),
            timestamp = System.currentTimeMillis(),
            audioPath = audioPath,
            questionTranscript = transcript,
            aiResponseText = answer,
            status = VoiceNoteEntity.STATUS_ANSWERED,
            synced = false
        )

        val id = voiceNoteDao.insert(entity)
        entity.copy(id = id)
    }

    suspend fun deleteVoiceNote(voiceNote: VoiceNoteEntity) = withContext(Dispatchers.IO) {
        voiceNoteDao.delete(voiceNote)
    }

    suspend fun deleteById(id: Long) = withContext(Dispatchers.IO) {
        voiceNoteDao.deleteById(id)
    }
}
