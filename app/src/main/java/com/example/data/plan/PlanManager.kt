package com.example.data.plan

import android.content.Context
import com.example.data.security.KudulaSecurePreferences
import com.example.data.security.PlanGuard
import com.example.data.security.PlanSwitchVerification
import com.example.data.security.SecureLogger
import com.example.model.KudulaPlan
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * PlanManager (Kudula F0.6 Architecture + Hardening).
 *
 * Camada central de governação de planos e assinaturas:
 * - Mantém o plano ativo do utilizador (padrão FREE).
 * - Utiliza KudulaSecurePreferences com encriptação AES-256 GCM do Android KeyStore.
 * - Integração com o PlanGuard para restringir trocas manuais a ambientes de DEBUG.
 * - Calcula as Feature Flags correspondentes sem acoplamento a UI.
 * - Monitoriza o consumo potencial do plano para alimentar o cartão de upgrade contextual do Kudu.
 * - Preparado para sincronização transparente com Firebase / RevenueCat / NestJS.
 */
class PlanManager private constructor(context: Context?) {

    companion object {
        private const val TAG = "PlanManager"

        @Volatile
        private var instance: PlanManager? = null

        fun getInstance(context: Context? = null): PlanManager {
            return instance ?: synchronized(this) {
                instance ?: PlanManager(context?.applicationContext).also { instance = it }
            }
        }
    }

    private val securePrefs: KudulaSecurePreferences? = context?.let {
        KudulaSecurePreferences.getInstance(it)
    }

    private val initialPlan: KudulaPlan = securePrefs?.getCurrentPlan() ?: KudulaPlan.FREE

    private val _currentPlan = MutableStateFlow(initialPlan)
    val currentPlan: StateFlow<KudulaPlan> = _currentPlan.asStateFlow()

    private val _featureFlags = MutableStateFlow(FeatureFlags.forPlan(initialPlan))
    val featureFlags: StateFlow<FeatureFlags> = _featureFlags.asStateFlow()

    // Indicador dinâmico de uso do plano atual (inicia em 0% para utilizadores novos)
    private val _planUsagePercent = MutableStateFlow(0)
    val planUsagePercent: StateFlow<Int> = _planUsagePercent.asStateFlow()

    /**
     * Atualiza o consumo real do plano com base na atividade realizada pelo utilizador.
     */
    fun updateUsageBasedOnActivity(totalStudyMinutes: Int, sessionsCount: Int) {
        if (_currentPlan.value == KudulaPlan.FREE) {
            // Plano Gratuito: 120 minutos ou 10 sessões mensais
            val minuteRatio = totalStudyMinutes.toFloat() / 120f
            val sessionRatio = sessionsCount.toFloat() / 10f
            val calculated = (maxOf(minuteRatio, sessionRatio) * 100).toInt().coerceIn(0, 100)
            _planUsagePercent.value = calculated
        }
    }

    /**
     * Tenta ativar ou alterar o plano do utilizador protegido pelo PlanGuard.
     * Retorna a verificação indicando se foi permitida (em DEBUG) ou se exige checkout seguro (em RELEASE).
     */
    fun requestPlanTransition(targetPlan: KudulaPlan): PlanSwitchVerification {
        val verification = PlanGuard.verifyPlanTransition(targetPlan, _currentPlan.value)
        when (verification) {
            is PlanSwitchVerification.AllowedInDebug -> {
                forceActivatePlan(targetPlan)
            }
            is PlanSwitchVerification.RequiresCheckout -> {
                SecureLogger.i(TAG, "Transição requer checkout seguro para: ${targetPlan.displayName}")
            }
            is PlanSwitchVerification.CorporateContactRequired -> {
                SecureLogger.i(TAG, "Transição requer contacto corporativo para: ${targetPlan.displayName}")
            }
            is PlanSwitchVerification.AlreadyActive -> {
                SecureLogger.d(TAG, "Plano já ativo: ${targetPlan.displayName}")
            }
        }
        return verification
    }

    /**
     * Ativação forçada de plano (usada internamente por verificação de DEBUG ou callback de checkout).
     */
    fun forceActivatePlan(plan: KudulaPlan) {
        SecureLogger.i(TAG, "Ativando plano: ${_currentPlan.value} -> $plan")
        _currentPlan.value = plan
        _featureFlags.value = FeatureFlags.forPlan(plan)
        securePrefs?.saveCurrentPlan(plan)

        // Reset do indicador de saturação após upgrade
        _planUsagePercent.value = 0
    }

    /**
     * Compatibilidade direta: chama requestPlanTransition().
     */
    fun activatePlan(plan: KudulaPlan) {
        requestPlanTransition(plan)
    }

    /**
     * Sugere o próximo plano de aceleração cognitiva para o utilizador.
     */
    fun getRecommendedUpgrade(): KudulaPlan = when (_currentPlan.value) {
        KudulaPlan.FREE -> KudulaPlan.STUDENT
        KudulaPlan.STUDENT -> KudulaPlan.PLUS
        KudulaPlan.PLUS -> KudulaPlan.PRO
        KudulaPlan.PRO -> KudulaPlan.BUSINESS
        KudulaPlan.BUSINESS -> KudulaPlan.INSTITUTION
        KudulaPlan.INSTITUTION -> KudulaPlan.INSTITUTION
    }

    /**
     * Stub arquitetural preparado para sincronizar o plano quando o Firebase Auth estiver conectado.
     */
    fun syncPlanFromFirebase(planName: String?) {
        if (planName.isNullOrBlank()) return
        try {
            val remotePlan = KudulaPlan.valueOf(planName.uppercase())
            forceActivatePlan(remotePlan)
        } catch (e: Exception) {
            SecureLogger.w(TAG, "Unrecognized plan from Firebase: $planName", e)
        }
    }
}
