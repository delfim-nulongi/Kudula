package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.KudulaDatabase
import com.example.data.local.KudulaDatabaseProvider
import com.example.data.local.entity.VoiceNoteEntity
import com.example.data.repository.VoiceNoteRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.random.Random

data class VoiceNotesUiState(
    val isRecording: Boolean = false,
    val recordingDuration: Int = 0,
    val isProcessing: Boolean = false,
    val activeVoiceNote: VoiceNoteEntity? = null,
    val historyNotes: List<VoiceNoteEntity> = emptyList(),
    val isPlayingAudio: Boolean = false,
    val waveAmplitudes: List<Float> = List(12) { 0.2f }
)

class VoiceNotesViewModel(application: Application) : AndroidViewModel(application) {

    private val voiceNoteDao = KudulaDatabaseProvider.getDatabase(application).voiceNoteDao()
    private val repository = VoiceNoteRepository(voiceNoteDao)

    private val _uiState = MutableStateFlow(VoiceNotesUiState())
    val uiState: StateFlow<VoiceNotesUiState> = _uiState.asStateFlow()

    private var recordingJob: Job? = null
    private var playbackJob: Job? = null

    init {
        viewModelScope.launch {
            repository.getAllVoiceNotes().collectLatest { notes ->
                _uiState.value = _uiState.value.copy(historyNotes = notes)
            }
        }
    }

    fun startRecording() {
        if (_uiState.value.isRecording) return

        recordingJob?.cancel()
        _uiState.value = _uiState.value.copy(
            isRecording = true,
            recordingDuration = 0,
            activeVoiceNote = null,
            waveAmplitudes = List(16) { 0.3f }
        )

        recordingJob = viewModelScope.launch {
            var seconds = 0
            while (_uiState.value.isRecording && seconds < 60) {
                delay(1000)
                seconds++
                val simulatedWaves = List(16) { 0.2f + Random.nextFloat() * 0.75f }
                _uiState.value = _uiState.value.copy(
                    recordingDuration = seconds,
                    waveAmplitudes = simulatedWaves
                )
            }
            if (_uiState.value.isRecording && seconds >= 60) {
                stopAndProcessRecording()
            }
        }
    }

    fun cancelRecording() {
        recordingJob?.cancel()
        _uiState.value = _uiState.value.copy(
            isRecording = false,
            recordingDuration = 0,
            waveAmplitudes = List(12) { 0.2f }
        )
    }

    fun stopAndProcessRecording() {
        if (!_uiState.value.isRecording) return

        recordingJob?.cancel()
        val duration = _uiState.value.recordingDuration.coerceAtLeast(1)

        _uiState.value = _uiState.value.copy(
            isRecording = false,
            isProcessing = true
        )

        viewModelScope.launch {
            try {
                val processed = repository.recordAndProcessVoiceNote(durationSeconds = duration)
                _uiState.value = _uiState.value.copy(
                    isProcessing = false,
                    activeVoiceNote = processed
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isProcessing = false)
            }
        }
    }

    fun playAudioResponse() {
        if (_uiState.value.isPlayingAudio) {
            playbackJob?.cancel()
            _uiState.value = _uiState.value.copy(isPlayingAudio = false)
            return
        }

        playbackJob?.cancel()
        _uiState.value = _uiState.value.copy(isPlayingAudio = true)

        playbackJob = viewModelScope.launch {
            // Simulação de reprodução de resposta com ondas de áudio
            repeat(8) {
                val waves = List(16) { 0.2f + Random.nextFloat() * 0.6f }
                _uiState.value = _uiState.value.copy(waveAmplitudes = waves)
                delay(500)
            }
            _uiState.value = _uiState.value.copy(
                isPlayingAudio = false,
                waveAmplitudes = List(12) { 0.2f }
            )
        }
    }

    fun deleteNote(id: Long) {
        viewModelScope.launch {
            repository.deleteById(id)
            if (_uiState.value.activeVoiceNote?.id == id) {
                _uiState.value = _uiState.value.copy(activeVoiceNote = null)
            }
        }
    }
}
