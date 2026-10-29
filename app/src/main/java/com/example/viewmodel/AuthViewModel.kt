package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.auth.AuthRepository
import com.example.data.auth.AuthState
import com.example.data.local.KudulaDatabaseProvider
import com.example.data.local.entity.AnonymousSession
import com.example.data.repository.AnonymousSessionRepository
import com.example.data.repository.EventLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val authState: AuthState = AuthState.Anonymous(sessionId = "init", deviceId = "local"),
    val anonymousSession: AnonymousSession? = null,
    val showProgressiveLoginPrompt: Boolean = false,
    val progressivePromptReason: String? = null,
    val hasDeclinedLoginRecently: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val rememberCredentials: Boolean = true
)

/**
 * AuthViewModel (F0.6)
 * Gere Sessão Anónima, Firebase Authentication e Login Progressivo Invisível.
 */
class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val db = KudulaDatabaseProvider.getDatabase(application)
    private val anonymousRepo = AnonymousSessionRepository(db.anonymousSessionDao())
    private val authRepo = AuthRepository()

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val session = anonymousRepo.getOrCreateSession()
            val currentAuth = authRepo.getCurrentUser()
            _uiState.value = _uiState.value.copy(
                anonymousSession = session,
                authState = currentAuth
            )
        }
    }

    /**
     * Gatilhos de Login Progressivo:
     * - Concluir primeiro quiz
     * - Adicionar primeiro PDF
     * - Processar primeiro YouTube
     * - Criar primeira reflexão
     * - Atingir primeira conquista (First Win)
     * - Desbloquear primeira revisão
     */
    fun triggerProgressiveLogin(reason: String) {
        if (_uiState.value.authState is AuthState.Authenticated) return
        if (_uiState.value.hasDeclinedLoginRecently) return

        _uiState.value = _uiState.value.copy(
            showProgressiveLoginPrompt = true,
            progressivePromptReason = reason
        )
        EventLogger.log("PROGRESSIVE_LOGIN_TRIGGERED", "Gatilho acionado: $reason")
    }

    fun dismissProgressiveLogin() {
        _uiState.value = _uiState.value.copy(
            showProgressiveLoginPrompt = false,
            hasDeclinedLoginRecently = true
        )
        EventLogger.log("PROGRESSIVE_LOGIN_DISMISSED", "Utilizador escolheu 'Agora não'")
    }

    fun continueWithGoogle(activityContext: android.content.Context? = null, onSuccess: () -> Unit = {}) {
        val targetContext = activityContext ?: getApplication<Application>()
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, successMessage = null)
            val result = authRepo.signInWithGoogle(targetContext)
            result.onSuccess { authenticated ->
                _uiState.value = _uiState.value.copy(
                    authState = authenticated,
                    showProgressiveLoginPrompt = false,
                    isLoading = false,
                    successMessage = "Sessão iniciada com sucesso via Google!"
                )
                _uiState.value.anonymousSession?.let { sess ->
                    anonymousRepo.linkAccount(sess.sessionId, authenticated.uid, authenticated.email)
                }
                EventLogger.log("LOGIN_GOOGLE_SUCCESS", "Conta vinculada via Google: ${authenticated.email}")
                onSuccess()
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = err.message ?: "Falha ao iniciar sessão com Google"
                )
            }
        }
    }

    fun continueWithEmail(email: String, pass: String, onSuccess: () -> Unit = {}) {
        if (email.isBlank() || pass.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Por favor, preenche todos os campos.")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, successMessage = null)
            val result = authRepo.signInWithEmail(email, pass)
            result.onSuccess { authenticated ->
                _uiState.value = _uiState.value.copy(
                    authState = authenticated,
                    showProgressiveLoginPrompt = false,
                    isLoading = false,
                    successMessage = "Bem-vindo de volta!"
                )
                _uiState.value.anonymousSession?.let { sess ->
                    anonymousRepo.linkAccount(sess.sessionId, authenticated.uid, authenticated.email)
                }
                EventLogger.log("LOGIN_EMAIL_SUCCESS", "Conta vinculada via Email: ${authenticated.email}")
                onSuccess()
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = err.message ?: "Credenciais incorretas."
                )
            }
        }
    }

    fun signUpWithEmail(name: String, email: String, pass: String, onSuccess: () -> Unit = {}) {
        if (name.isBlank() || email.isBlank() || pass.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Por favor, preenche todos os campos.")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, successMessage = null)
            val result = authRepo.signUpWithEmail(name, email, pass)
            result.onSuccess { created ->
                _uiState.value = _uiState.value.copy(
                    authState = created,
                    showProgressiveLoginPrompt = false,
                    isLoading = false,
                    successMessage = "Conta criada com sucesso!"
                )
                _uiState.value.anonymousSession?.let { sess ->
                    anonymousRepo.linkAccount(sess.sessionId, created.uid, created.email)
                }
                EventLogger.log("SIGNUP_EMAIL_SUCCESS", "Nova conta criada: ${created.email} ($name)")
                onSuccess()
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = err.message ?: "Não foi possível criar a conta."
                )
            }
        }
    }

    fun sendPasswordReset(email: String) {
        if (email.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Insere o teu email para recuperar a senha.")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, successMessage = null)
            val result = authRepo.sendPasswordReset(email)
            result.onSuccess {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    successMessage = "Instruções de recuperação enviadas para $email"
                )
                EventLogger.log("PASSWORD_RESET_SENT", "Recuperação solicitada para $email")
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = err.message ?: "Erro ao solicitar recuperação."
                )
            }
        }
    }

    fun updateDisplayName(newName: String, onComplete: () -> Unit = {}) {
        if (newName.isBlank()) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            authRepo.updateDisplayName(newName)
            val current = _uiState.value.authState
            if (current is AuthState.Authenticated) {
                _uiState.value = _uiState.value.copy(
                    authState = current.copy(displayName = newName),
                    isLoading = false,
                    successMessage = "Nome atualizado para $newName"
                )
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
            onComplete()
        }
    }

    fun setRememberCredentials(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(rememberCredentials = enabled)
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = null, successMessage = null)
    }

    fun signOut() {
        authRepo.signOut()
        viewModelScope.launch {
            val session = anonymousRepo.getOrCreateSession()
            _uiState.value = _uiState.value.copy(
                authState = AuthState.Anonymous(session.sessionId, session.deviceId),
                hasDeclinedLoginRecently = false,
                successMessage = "Sessão terminada."
            )
        }
    }
}
