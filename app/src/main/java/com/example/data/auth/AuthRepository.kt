package com.example.data.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.example.data.remote.FirestoreRepository
import com.example.data.security.SecureLogger
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthActionCodeException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Repositório Real de Autenticação Firebase (F0.7 Real Firebase Implementation).
 *
 * Princípios de segurança rigorosos:
 * - Sem criação de UIDs artificiais ou fictícios (nenhum sandbox-* ou offline-user-*).
 * - Sem mascaramento de falhas de credenciais como sucessos.
 * - Integração nativa com o Firebase Authentication real.
 * - Suporte a Google Sign-In real através do Credential Manager.
 * - Inicialização do documento users/{uid} no Firestore após registo.
 */
class AuthRepository(
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestoreRepository: FirestoreRepository = FirestoreRepository()
) {

    companion object {
        private const val TAG = "AuthRepository"
    }

    /**
     * Retorna se o utilizador está autenticado no Firebase Authentication.
     */
    fun isUserLoggedIn(): Boolean {
        val user = firebaseAuth.currentUser
        return user != null && !user.isAnonymous
    }

    /**
     * Devolve o estado de autenticação real atual.
     * Nunca fabrica uma conta autenticada se o Firebase não possuir utilizador ativo.
     */
    fun getCurrentUser(): AuthState {
        val user = firebaseAuth.currentUser
        return if (user != null && !user.isAnonymous) {
            AuthState.Authenticated(
                uid = user.uid,
                email = user.email,
                displayName = user.displayName ?: user.email?.substringBefore('@'),
                isAnonymous = false,
                photoUrl = user.photoUrl?.toString()
            )
        } else {
            AuthState.Anonymous(
                sessionId = "anon-${user?.uid ?: "local"}",
                deviceId = "device-kudula"
            )
        }
    }

    /**
     * Início de sessão real com E-mail e Senha no Firebase Authentication.
     */
    suspend fun signInWithEmail(email: String, pass: String): Result<AuthState.Authenticated> = withContext(Dispatchers.IO) {
        val trimmedEmail = email.trim()
        if (trimmedEmail.isBlank() || pass.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("E-mail e senha são de preenchimento obrigatório."))
        }

        try {
            val result = firebaseAuth.signInWithEmailAndPassword(trimmedEmail, pass).await()
            val user = result.user ?: return@withContext Result.failure(
                IllegalStateException("Erro inesperado: utilizador nulo retornado pelo Firebase.")
            )

            val authenticated = AuthState.Authenticated(
                uid = user.uid,
                email = user.email,
                displayName = user.displayName ?: user.email?.substringBefore('@'),
                photoUrl = user.photoUrl?.toString()
            )

            // Assegura existência do documento users/{uid} com dados permitidos
            firestoreRepository.upsertUserDocument(
                uid = user.uid,
                email = user.email,
                displayName = authenticated.displayName,
                photoUrl = authenticated.photoUrl
            )

            SecureLogger.i(TAG, "Autenticação bem-sucedida para o UID: ${user.uid}")
            Result.success(authenticated)
        } catch (e: Exception) {
            SecureLogger.w(TAG, "Falha de autenticação para o e-mail: $trimmedEmail - ${e.message}")
            Result.failure(mapFirebaseException(e))
        }
    }

    /**
     * Registo real de nova conta com E-mail e Senha no Firebase Authentication.
     */
    suspend fun signUpWithEmail(name: String, email: String, pass: String): Result<AuthState.Authenticated> = withContext(Dispatchers.IO) {
        val trimmedEmail = email.trim()
        val trimmedName = name.trim()

        if (trimmedEmail.isBlank() || pass.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("E-mail e senha são de preenchimento obrigatório."))
        }
        if (pass.length < 6) {
            return@withContext Result.failure(IllegalArgumentException("A senha deve ter pelo menos 6 caracteres."))
        }

        try {
            val result = firebaseAuth.createUserWithEmailAndPassword(trimmedEmail, pass).await()
            val user = result.user ?: return@withContext Result.failure(
                IllegalStateException("Erro inesperado: utilizador nulo retornado após criação de conta.")
            )

            // Atualiza o DisplayName no Firebase Auth Profile
            if (trimmedName.isNotBlank()) {
                try {
                    val profileUpdate = UserProfileChangeRequest.Builder()
                        .setDisplayName(trimmedName)
                        .build()
                    user.updateProfile(profileUpdate).await()
                } catch (pe: Exception) {
                    SecureLogger.w(TAG, "Aviso ao definir displayName após registo: ${pe.message}")
                }
            }

            val finalDisplayName = trimmedName.ifBlank { user.email?.substringBefore('@') ?: "Utilizador Kudula" }
            val authenticated = AuthState.Authenticated(
                uid = user.uid,
                email = user.email,
                displayName = finalDisplayName,
                photoUrl = user.photoUrl?.toString()
            )

            // Cria o documento oficial users/{uid}
            firestoreRepository.upsertUserDocument(
                uid = user.uid,
                email = user.email,
                displayName = finalDisplayName,
                photoUrl = user.photoUrl?.toString()
            )

            SecureLogger.i(TAG, "Nova conta registada com sucesso no Firebase. UID: ${user.uid}")
            Result.success(authenticated)
        } catch (e: Exception) {
            SecureLogger.w(TAG, "Falha ao criar conta para: $trimmedEmail - ${e.message}")
            Result.failure(mapFirebaseException(e))
        }
    }

    /**
     * Inicia sessão real através do Google Credential Manager.
     *
     * Obtém o serverClientId de forma dinâmica através do R.string.default_web_client_id.
     * Caso o Web Client ID não esteja presente no google-services.json, retorna erro claro
     * instruindo sobre a configuração pendente no Firebase Console, sem inventar IDs ou tokens falsos.
     */
    suspend fun signInWithGoogle(context: Context): Result<AuthState.Authenticated> = withContext(Dispatchers.IO) {
        val resId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
        if (resId == 0) {
            val missingConfigMsg = "A autenticação com Google requer a ativação do Web Client ID no Firebase Console. " +
                "Adicione o certificado SHA-1 às configurações do app no Firebase Console e faça o download do google-services.json atualizado."
            SecureLogger.w(TAG, missingConfigMsg)
            return@withContext Result.failure(IllegalStateException(missingConfigMsg))
        }

        val serverClientId = context.getString(resId)
        if (serverClientId.isBlank()) {
            return@withContext Result.failure(IllegalStateException("Web Client ID vazio no google-services.json."))
        }

        try {
            val credentialManager = CredentialManager.create(context)
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(serverClientId)
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val credentialResult = credentialManager.getCredential(context = context, request = request)
            val credential = credentialResult.credential

            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken
                return@withContext signInWithGoogleIdToken(idToken)
            } else {
                return@withContext Result.failure(IllegalStateException("Tipo de credencial Google não suportado."))
            }
        } catch (e: GetCredentialCancellationException) {
            SecureLogger.d(TAG, "Início de sessão com Google cancelado pelo utilizador.")
            return@withContext Result.failure(Exception("Seleção de conta Google cancelada."))
        } catch (e: GetCredentialException) {
            SecureLogger.w(TAG, "Erro do Credential Manager: ${e.message}")
            return@withContext Result.failure(mapFirebaseException(e))
        } catch (e: Exception) {
            SecureLogger.e(TAG, "Erro geral no fluxo Google Sign-In", e)
            return@withContext Result.failure(mapFirebaseException(e))
        }
    }

    /**
     * Autentica no Firebase usando um token de ID real do Google.
     */
    suspend fun signInWithGoogleIdToken(idToken: String): Result<AuthState.Authenticated> = withContext(Dispatchers.IO) {
        if (idToken.isBlank() || idToken == "google-token-simulated") {
            return@withContext Result.failure(IllegalArgumentException("Token Google inválido ou não fornecido."))
        }

        try {
            val authCredential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = firebaseAuth.signInWithCredential(authCredential).await()
            val user = authResult.user ?: return@withContext Result.failure(
                IllegalStateException("Erro: utilizador nulo após autenticação com credencial Google.")
            )

            val authenticated = AuthState.Authenticated(
                uid = user.uid,
                email = user.email,
                displayName = user.displayName ?: user.email?.substringBefore('@') ?: "Utilizador Google",
                photoUrl = user.photoUrl?.toString()
            )

            firestoreRepository.upsertUserDocument(
                uid = user.uid,
                email = user.email,
                displayName = authenticated.displayName,
                photoUrl = authenticated.photoUrl
            )

            SecureLogger.i(TAG, "Google Sign-In concluído com sucesso. UID: ${user.uid}")
            Result.success(authenticated)
        } catch (e: Exception) {
            SecureLogger.e(TAG, "Falha na validação da credencial Google no Firebase", e)
            Result.failure(mapFirebaseException(e))
        }
    }

    /**
     * Atualização do nome de exibição no Firebase Auth e no documento do Firestore.
     */
    suspend fun updateDisplayName(name: String): Result<Unit> = withContext(Dispatchers.IO) {
        val user = firebaseAuth.currentUser
            ?: return@withContext Result.failure(SecurityException("Nenhum utilizador com sessão ativa."))
        val trimmed = name.trim()
        if (trimmed.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("O nome não pode estar em branco."))
        }

        try {
            val updates = UserProfileChangeRequest.Builder()
                .setDisplayName(trimmed)
                .build()
            user.updateProfile(updates).await()

            firestoreRepository.upsertUserDocument(
                uid = user.uid,
                email = user.email,
                displayName = trimmed,
                photoUrl = user.photoUrl?.toString()
            )

            SecureLogger.i(TAG, "Nome de perfil atualizado para '$trimmed' no Firebase.")
            Result.success(Unit)
        } catch (e: Exception) {
            SecureLogger.e(TAG, "Erro ao atualizar nome no Firebase", e)
            Result.failure(mapFirebaseException(e))
        }
    }

    /**
     * Envio de e-mail real de redefinição de senha através do Firebase Auth.
     */
    suspend fun sendPasswordReset(email: String): Result<Unit> = withContext(Dispatchers.IO) {
        val trimmed = email.trim()
        if (trimmed.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Insere um e-mail válido para redefinição."))
        }

        try {
            firebaseAuth.sendPasswordResetEmail(trimmed).await()
            SecureLogger.i(TAG, "E-mail de recuperação de senha enviado para: $trimmed")
            Result.success(Unit)
        } catch (e: Exception) {
            SecureLogger.w(TAG, "Erro ao enviar e-mail de recuperação para: $trimmed - ${e.message}")
            Result.failure(mapFirebaseException(e))
        }
    }

    /**
     * Termina a sessão real no Firebase Auth.
     */
    fun signOut() {
        try {
            firebaseAuth.signOut()
            SecureLogger.i(TAG, "Sessão Firebase terminada com sucesso.")
        } catch (e: Exception) {
            SecureLogger.e(TAG, "Erro ao terminar sessão", e)
        }
    }

    /**
     * Mapeia exceções do Firebase para mensagens amigáveis em português,
     * sem expor stack traces ou detalhes técnicos de segurança ao utilizador final.
     */
    private fun mapFirebaseException(e: Exception): Exception {
        val message = when (e) {
            is FirebaseAuthInvalidUserException ->
                "Utilizador não encontrado. Verifica o endereço de e-mail ou cria uma nova conta."
            is FirebaseAuthInvalidCredentialsException ->
                "Credenciais inválidas. Verifica se o e-mail e a senha foram introduzidos corretamente."
            is FirebaseAuthUserCollisionException ->
                "Já existe uma conta associada a este endereço de e-mail."
            is FirebaseAuthWeakPasswordException ->
                "A senha é demasiado simples. Escolhe uma senha com pelo menos 6 caracteres."
            is FirebaseNetworkException ->
                "Não foi possível estabelecer ligação à internet. Verifica a tua rede e tenta novamente."
            is FirebaseTooManyRequestsException ->
                "Acesso temporariamente bloqueado devido a muitas tentativas consecutivas. Aguarda alguns minutos."
            is FirebaseAuthActionCodeException ->
                "O código ou ligação de confirmação é inválido ou expirou."
            else -> {
                val rawMsg = e.message.orEmpty()
                when {
                    rawMsg.contains("network error", ignoreCase = true) ->
                        "Sem ligação à internet. O Kudula mantém os teus dados locais protegidos até restabelecer a rede."
                    rawMsg.contains("user-not-found", ignoreCase = true) ->
                        "Conta não encontrada. Verifica o teu e-mail."
                    rawMsg.contains("wrong-password", ignoreCase = true) ->
                        "Senha incorreta. Tenta novamente ou recupera as tuas credenciais."
                    rawMsg.contains("email-already-in-use", ignoreCase = true) ->
                        "Este e-mail já está a ser utilizado por outra conta."
                    rawMsg.contains("weak-password", ignoreCase = true) ->
                        "A senha deve ter pelo menos 6 caracteres."
                    rawMsg.contains("permission-denied", ignoreCase = true) ->
                        "Acesso negado pelas regras de segurança. Inicia sessão novamente."
                    else -> "Não foi possível concluir a operação no Firebase. Tenta novamente."
                }
            }
        }
        return Exception(message, e)
    }
}
