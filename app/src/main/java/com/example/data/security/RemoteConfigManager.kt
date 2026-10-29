package com.example.data.security

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class RemoteConfigValues(
    val priceStudentMonthly: String = "$5",
    val pricePlusMonthly: String = "$10",
    val priceProMonthly: String = "$20",
    val freeAiDailyLimit: Int = 20,
    val studentAiDailyLimit: Int = 200,
    val freeUploadMonthlyLimit: Int = 3,
    val isCampaignActive: Boolean = true,
    val campaignBannerTitle: String = "Campanha Universitária Luanda 2026",
    val campaignDiscountPercent: Int = 20,
    val isCertificatePinningEnforced: Boolean = false,
    val isPromptInjectionStrict: Boolean = true
)

/**
 * RemoteConfigManager (OWASP MASVS / F0.6 Hardening).
 *
 * Gere valores de configuração dinâmicos da plataforma:
 * - No MVP atual: fornece valores padrão locais e seguros com cache local.
 * - Preparado para integração imediata com o Firebase Remote Config SDK / NestJS Config.
 * - Permite atualização dinâmica de preços, limites e parâmetros de segurança sem recompilar o app.
 */
class RemoteConfigManager private constructor(context: Context) {

    companion object {
        private const val TAG = "RemoteConfigManager"

        @Volatile
        private var instance: RemoteConfigManager? = null

        fun getInstance(context: Context): RemoteConfigManager {
            return instance ?: synchronized(this) {
                instance ?: RemoteConfigManager(context.applicationContext).also { instance = it }
            }
        }
    }

    private val securePrefs = KudulaSecurePreferences.getInstance(context)

    private val _config = MutableStateFlow(
        RemoteConfigValues(
            priceStudentMonthly = securePrefs.getEncryptedString("rc_price_student", "$5"),
            pricePlusMonthly = securePrefs.getEncryptedString("rc_price_plus", "$10"),
            priceProMonthly = securePrefs.getEncryptedString("rc_price_pro", "$20"),
            freeAiDailyLimit = securePrefs.getEncryptedInt("rc_free_ai_limit", 20),
            studentAiDailyLimit = securePrefs.getEncryptedInt("rc_student_ai_limit", 200),
            freeUploadMonthlyLimit = securePrefs.getEncryptedInt("rc_free_upload_limit", 3),
            isCampaignActive = securePrefs.getEncryptedBoolean("rc_campaign_active", true),
            campaignBannerTitle = securePrefs.getEncryptedString("rc_campaign_title", "Campanha Universitária Luanda 2026"),
            campaignDiscountPercent = securePrefs.getEncryptedInt("rc_campaign_discount", 20),
            isCertificatePinningEnforced = securePrefs.getEncryptedBoolean("rc_cert_pinning", false),
            isPromptInjectionStrict = securePrefs.getEncryptedBoolean("rc_pi_strict", true)
        )
    )
    val config: StateFlow<RemoteConfigValues> = _config.asStateFlow()

    /**
     * Simula a busca e ativação de novos parâmetros do Firebase Remote Config.
     */
    suspend fun fetchAndActivate(): Boolean {
        SecureLogger.d(TAG, "Fetching remote configuration parameters...")
        // Preparado para conectar a FirebaseRemoteConfig.getInstance().fetchAndActivate()
        return true
    }

    fun updateConfigOverride(newValues: RemoteConfigValues) {
        _config.value = newValues
        securePrefs.putEncryptedString("rc_price_student", newValues.priceStudentMonthly)
        securePrefs.putEncryptedString("rc_price_plus", newValues.pricePlusMonthly)
        securePrefs.putEncryptedString("rc_price_pro", newValues.priceProMonthly)
        securePrefs.putEncryptedInt("rc_free_ai_limit", newValues.freeAiDailyLimit)
        securePrefs.putEncryptedInt("rc_student_ai_limit", newValues.studentAiDailyLimit)
        securePrefs.putEncryptedInt("rc_free_upload_limit", newValues.freeUploadMonthlyLimit)
        securePrefs.putEncryptedBoolean("rc_campaign_active", newValues.isCampaignActive)
        securePrefs.putEncryptedString("rc_campaign_title", newValues.campaignBannerTitle)
        securePrefs.putEncryptedInt("rc_campaign_discount", newValues.campaignDiscountPercent)
        securePrefs.putEncryptedBoolean("rc_cert_pinning", newValues.isCertificatePinningEnforced)
        securePrefs.putEncryptedBoolean("rc_pi_strict", newValues.isPromptInjectionStrict)
        SecureLogger.i(TAG, "Configurações remotas atualizadas no cache seguro.")
    }
}
