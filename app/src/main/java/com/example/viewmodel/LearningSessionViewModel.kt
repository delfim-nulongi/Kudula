package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.KudulaDatabaseProvider
import com.example.data.local.entity.CognitiveGoal
import com.example.data.local.entity.KudulaEvent
import com.example.data.local.entity.LearningSession
import com.example.data.local.entity.ReflectionJournal
import com.example.data.repository.CognitiveGoalRepository
import com.example.data.repository.EventLogger
import com.example.data.repository.EventRepository
import com.example.data.repository.LearningSessionRepository
import com.example.data.repository.ReflectionRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class LearningSessionUiState(
    val sessions: List<LearningSession> = emptyList(),
    val events: List<KudulaEvent> = emptyList(),
    val goals: List<CognitiveGoal> = emptyList(),
    val reflections: List<ReflectionJournal> = emptyList(),
    val totalMinutesStudied: Int = 0,
    val isSyncing: Boolean = false,
    val lastSyncMessage: String? = null,
    val syncStatusDescription: String = "Pronto para sincronização com Firebase/PostgreSQL"
)

/**
 * Kudula Local Session Engine (KLSE) ViewModel
 * Coordena as sessões cognitivas locais, metas diárias, eventos e reflexões.
 */
class LearningSessionViewModel(application: Application) : AndroidViewModel(application) {

    private val db = KudulaDatabaseProvider.getDatabase(application)
    private val sessionRepo = LearningSessionRepository(db.learningSessionDao())
    private val eventRepo = EventRepository(db.kudulaEventDao())
    private val goalRepo = CognitiveGoalRepository(db.cognitiveGoalDao())
    private val reflectionRepo = ReflectionRepository(db.reflectionJournalDao())

    private val _isSyncing = MutableStateFlow(false)
    private val _lastSyncMessage = MutableStateFlow<String?>(null)

    private val syncStateFlow = combine(_isSyncing, _lastSyncMessage) { syncing, msg ->
        syncing to msg
    }

    val uiState: StateFlow<LearningSessionUiState> = combine(
        sessionRepo.allSessions,
        eventRepo.allEvents,
        goalRepo.allGoals,
        reflectionRepo.allReflections,
        syncStateFlow
    ) { sessions, events, goals, reflections, (isSyncing, syncMsg) ->
        val totalMinutes = sessions.sumOf { it.studyMinutes }
        LearningSessionUiState(
            sessions = sessions,
            events = events,
            goals = goals,
            reflections = reflections,
            totalMinutesStudied = totalMinutes,
            isSyncing = isSyncing,
            lastSyncMessage = syncMsg
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = LearningSessionUiState()
    )

    init {
        // Initialize central EventLogger
        EventLogger.initialize(eventRepo, goalRepo)
        // Note: Production accounts start clean with 0 sessions, 0 events, 0 goals, 0 reflections.
        // Seeding is reserved strictly for test fixtures.
    }

    fun recordLearningSession(
        concept: String,
        sourceType: String,
        studyMinutes: Int,
        ikcPartial: Int,
        ipaPartial: Int,
        notes: String = ""
    ) {
        viewModelScope.launch {
            val session = sessionRepo.recordSession(
                concept = concept,
                sourceType = sourceType,
                studyMinutes = studyMinutes,
                ikcPartial = ikcPartial,
                ipaPartial = ipaPartial,
                notes = notes
            )
            // Log event in EventLog
            EventLogger.log(
                eventType = KudulaEvent.TYPE_CHAPTER_READ,
                details = "Sessão concluída: $concept ($studyMinutes min, IKC: $ikcPartial, IPA: $ipaPartial)",
                metadataJson = """{"sessionId":"${session.uuid}","minutes":$studyMinutes}"""
            )
        }
    }

    fun saveReflection(
        text: String,
        mood: String,
        energy: Int,
        confidence: Int,
        tags: List<String>,
        promptQuestion: String,
        sessionId: String? = null
    ) {
        viewModelScope.launch {
            val reflection = reflectionRepo.saveReflection(
                text = text,
                mood = mood,
                energy = energy,
                confidence = confidence,
                tags = tags,
                promptQuestion = promptQuestion,
                sessionId = sessionId
            )
            EventLogger.logReflectionCreated(mood, tags)
            // Progress goal "explicar um conceito"
            val todayGoals = uiState.value.goals
            todayGoals.find { it.title.contains("explicar", ignoreCase = true) }?.let { goal ->
                goalRepo.incrementProgress(goal.uuid, 1) { completedGoal ->
                    EventLogger.logGoalCompleted(completedGoal.title)
                }
            }
        }
    }

    fun incrementGoalProgress(goalUuid: String, amount: Int = 1) {
        viewModelScope.launch {
            goalRepo.incrementProgress(goalUuid, amount) { completedGoal ->
                EventLogger.logGoalCompleted(completedGoal.title)
            }
        }
    }

    fun syncOfflineData() {
        viewModelScope.launch {
            _isSyncing.value = true
            _lastSyncMessage.value = "A preparar pacotes de sincronização com PostgreSQL/Firebase..."
            delay(1200)
            sessionRepo.markAllAsSynced()
            eventRepo.markAllAsSynced()
            _isSyncing.value = false
            _lastSyncMessage.value = "Sessões e telemetria offline preparadas para replicação na nuvem!"
            EventLogger.log(
                eventType = "SYNC_SIMULATED",
                details = "Todas as sessões locais marcadas como prontas para upload seguro"
            )
        }
    }
}
