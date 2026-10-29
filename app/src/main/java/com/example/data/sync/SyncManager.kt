package com.example.data.sync

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * SyncManager (Kudula F0.5+ Architecture).
 *
 * Controla e orquestra quando sincronizar de forma inteligente, eficiente e resiliente:
 * - Evita sincronização contínua ou desnecessária.
 * - Gatilhos inteligentes:
 *     1. Wi-Fi ou rede estável disponível
 *     2. Utilizador autenticado
 *     3. Abertura da aplicação
 *     4. Retorno de background
 *     5. Conclusão de upload ou nova sessão KLSE
 * - Resiliência com Mutex anti-duplicação e retry exponencial.
 * - Indicador discreto em StateFlow ("Sincronizando..."), garantindo que o utilizador nunca seja bloqueado.
 */
class SyncManager(
    private val context: Context,
    private val syncRepository: SyncRepository
) {

    companion object {
        private const val TAG = "SyncManager"
        private const val MAX_RETRIES = 3
        private const val INITIAL_BACKOFF_MS = 1000L
    }

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val syncMutex = Mutex()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _syncStatusMessage = MutableStateFlow<String?>(null)
    val syncStatusMessage: StateFlow<String?> = _syncStatusMessage.asStateFlow()

    private var retryCount = 0

    init {
        registerNetworkCallback()
    }

    private fun registerNetworkCallback() {
        try {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()

            connectivityManager?.registerNetworkCallback(
                request,
                object : ConnectivityManager.NetworkCallback() {
                    override fun onAvailable(network: Network) {
                        Log.d(TAG, "Network became available. Triggering sync.")
                        onNetworkAvailable()
                    }

                    override fun onLost(network: Network) {
                        Log.d(TAG, "Network lost.")
                    }
                }
            )
        } catch (e: Exception) {
            Log.w(TAG, "Could not register network callback: ${e.message}")
        }
    }

    /**
     * Gatilho: Rede disponível
     */
    fun onNetworkAvailable() {
        triggerSync("NETWORK_AVAILABLE")
    }

    /**
     * Gatilho: Utilizador autenticado
     */
    fun onUserAuthenticated() {
        triggerSync("USER_AUTHENTICATED")
    }

    /**
     * Gatilho: App aberto ou regressou de background
     */
    fun onAppForegrounded() {
        triggerSync("APP_FOREGROUNDED")
    }

    /**
     * Gatilho: Upload ou gravação concluída
     */
    fun onUploadCompleted() {
        triggerSync("UPLOAD_COMPLETED")
    }

    /**
     * Dispara sincronização em segundo plano, respeitando o Mutex para evitar duplicidades.
     */
    fun triggerSync(reason: String) {
        val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        if (currentUser == null || currentUser.isAnonymous) {
            Log.d(TAG, "Sync ignorado: nenhum utilizador Firebase autenticado ativo ($reason)")
            return
        }

        scope.launch {
            if (syncMutex.isLocked) {
                Log.d(TAG, "Sync already in progress. Skipping trigger: $reason")
                return@launch
            }

            syncMutex.withLock {
                _isSyncing.value = true
                _syncStatusMessage.value = "Sincronizando..."
                Log.d(TAG, "Starting sync cycle due to: $reason for UID: ${currentUser.uid}")

                var success = false
                var currentDelay = INITIAL_BACKOFF_MS

                while (!success && retryCount < MAX_RETRIES) {
                    val result = syncRepository.syncBidirectional()
                    if (result.isSuccess) {
                        success = true
                        retryCount = 0
                        Log.d(TAG, "Sync cycle completed successfully ($reason)")
                    } else {
                        val exception = result.exceptionOrNull()
                        val isPermissionDenied = (exception is com.google.firebase.firestore.FirebaseFirestoreException &&
                                exception.code == com.google.firebase.firestore.FirebaseFirestoreException.Code.PERMISSION_DENIED) ||
                                exception?.message?.contains("PERMISSION_DENIED", ignoreCase = true) == true

                        if (isPermissionDenied) {
                            Log.i(TAG, "Sincronização na nuvem pausada: Regras do Firestore no console limitam acesso. Modo Offline ativo no Room.")
                            retryCount = 0
                            break
                        }

                        retryCount++
                        Log.w(TAG, "Sync attempt $retryCount failed. Retrying in ${currentDelay}ms...")
                        delay(currentDelay)
                        currentDelay *= 2
                    }
                }

                _isSyncing.value = false
                _syncStatusMessage.value = null
            }
        }
    }
}
