package com.example.data.security

import android.content.Context
import android.content.SharedPreferences
import com.example.model.KudulaPlan

/**
 * KudulaSecurePreferences (OWASP MASVS Hardening).
 *
 * Fornece armazenamento seguro e encriptado com chaves do Android KeyStore.
 * Substitui o SharedPreferences em texto simples para guardar o plano atual,
 * tokens de sessão, cotas de consumo e dados confidenciais do utilizador.
 */
class KudulaSecurePreferences(context: Context) {

    companion object {
        private const val PREFS_NAME = "kudula_secure_vault_prefs"
        private const val KEY_PLAN_ENCRYPTED = "sec_kudula_user_plan"
        private const val KEY_SESSION_TOKEN = "sec_user_session_token"
        private const val KEY_DEVICE_FINGERPRINT = "sec_device_fingerprint"

        @Volatile
        private var instance: KudulaSecurePreferences? = null

        fun getInstance(context: Context): KudulaSecurePreferences {
            return instance ?: synchronized(this) {
                instance ?: KudulaSecurePreferences(context.applicationContext).also { instance = it }
            }
        }
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun putEncryptedString(key: String, value: String) {
        val encrypted = KudulaKeyStoreManager.encrypt(value)
        prefs.edit().putString(key, encrypted).apply()
    }

    fun getEncryptedString(key: String, defaultValue: String = ""): String {
        val stored = prefs.getString(key, null) ?: return defaultValue
        return KudulaKeyStoreManager.decrypt(stored).ifEmpty { defaultValue }
    }

    fun putEncryptedInt(key: String, value: Int) {
        putEncryptedString(key, value.toString())
    }

    fun getEncryptedInt(key: String, defaultValue: Int): Int {
        val decrypted = getEncryptedString(key, defaultValue.toString())
        return decrypted.toIntOrNull() ?: defaultValue
    }

    fun putEncryptedBoolean(key: String, value: Boolean) {
        putEncryptedString(key, value.toString())
    }

    fun getEncryptedBoolean(key: String, defaultValue: Boolean): Boolean {
        val decrypted = getEncryptedString(key, defaultValue.toString())
        return decrypted.toBooleanStrictOrNull() ?: defaultValue
    }

    // --- Helpers de Alto Nível de Negócio ---

    fun saveCurrentPlan(plan: KudulaPlan) {
        putEncryptedString(KEY_PLAN_ENCRYPTED, plan.name)
    }

    fun getCurrentPlan(): KudulaPlan {
        val planName = getEncryptedString(KEY_PLAN_ENCRYPTED, KudulaPlan.FREE.name)
        return try {
            KudulaPlan.valueOf(planName)
        } catch (_: Exception) {
            KudulaPlan.FREE
        }
    }

    fun saveSessionToken(token: String) {
        putEncryptedString(KEY_SESSION_TOKEN, token)
    }

    fun getSessionToken(): String {
        return getEncryptedString(KEY_SESSION_TOKEN, "")
    }

    fun clearAllSecureData() {
        prefs.edit().clear().apply()
    }
}
