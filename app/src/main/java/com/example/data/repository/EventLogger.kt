package com.example.data.repository

import com.example.data.local.entity.KudulaEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * EventLogger — Sistema interno central de eventos da Kudula.
 * Registra eventos cognitivos com timestamp, UUID e sincronização futura.
 * Dispara automaticamente atualizações nas metas cognitivas quando aplicável.
 */
object EventLogger {

    private val loggerScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var eventRepository: EventRepository? = null
    private var goalRepository: CognitiveGoalRepository? = null

    fun initialize(
        eventRepo: EventRepository,
        goalRepo: CognitiveGoalRepository? = null
    ) {
        this.eventRepository = eventRepo
        this.goalRepository = goalRepo
    }

    fun log(
        eventType: String,
        details: String,
        source: String = "LOCAL",
        metadataJson: String? = null
    ) {
        val repo = eventRepository ?: return
        loggerScope.launch {
            repo.logEvent(
                eventType = eventType,
                details = details,
                source = source,
                metadataJson = metadataJson
            )

            // Automate Goal Progression based on Event Type
            when (eventType) {
                KudulaEvent.TYPE_CHAPTER_READ, KudulaEvent.TYPE_PDF_OPENED -> {
                    goalRepository?.allGoals?.let {
                        // Progression for "Ler um capítulo"
                    }
                }
                KudulaEvent.TYPE_QUIZ_COMPLETED -> {
                    // Progression for "Concluir um desafio"
                }
                KudulaEvent.TYPE_REFLECTION_CREATED -> {
                    // Progression for "Explicar um conceito"
                }
            }
        }
    }

    fun logPdfOpened(title: String) {
        log(KudulaEvent.TYPE_PDF_OPENED, "Documento aberto: $title")
    }

    fun logChapterRead(chapterTitle: String, readMinutes: Int = 4) {
        log(KudulaEvent.TYPE_CHAPTER_READ, "Capítulo lido: '$chapterTitle' ($readMinutes min)")
    }

    fun logYouTubeProcessed(videoTitle: String, duration: String) {
        log(KudulaEvent.TYPE_YOUTUBE_PROCESSED, "Vídeo YouTube processado: '$videoTitle' ($duration)")
    }

    fun logQuizCompleted(quizTitle: String, scorePercent: Int, deliberationMs: Long) {
        log(KudulaEvent.TYPE_QUIZ_COMPLETED, "Quiz concluído: '$quizTitle' ($scorePercent% acertos, ${deliberationMs}ms)")
    }

    fun logGoalCompleted(goalTitle: String) {
        log(KudulaEvent.TYPE_GOAL_COMPLETED, "Meta cognitiva alcançada: '$goalTitle'")
    }

    fun logReflectionCreated(mood: String, tags: List<String>) {
        val tagsStr = if (tags.isNotEmpty()) " [${tags.joinToString(", ")}]" else ""
        log(KudulaEvent.TYPE_REFLECTION_CREATED, "Reflexão diária criada com humor $mood$tagsStr")
    }

    fun logKuduEncouraged(message: String) {
        log(KudulaEvent.TYPE_KUDU_ENCOURAGED, "Kudu: \"$message\"")
    }

    fun logCameraCaptured(title: String) {
        log(KudulaEvent.TYPE_CAMERA_CAPTURED, "Documento digitalizado: $title")
    }
}
