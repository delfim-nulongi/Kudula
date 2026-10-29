package com.example.data.security

import com.example.BuildConfig
import com.example.model.KudulaPlan

sealed class PlanSwitchVerification {
    data object AllowedInDebug : PlanSwitchVerification()
    data class RequiresCheckout(val plan: KudulaPlan) : PlanSwitchVerification()
    data class CorporateContactRequired(val plan: KudulaPlan) : PlanSwitchVerification()
    data object AlreadyActive : PlanSwitchVerification()
}

/**
 * PlanGuard (OWASP MASVS / F0.6 Security Hardening).
 *
 * Controla e protege a integridade da transição de planos de subscrição:
 * - Em builds DEBUG, permite alternância manual instantânea para desenvolvimento e testes.
 * - Em builds RELEASE (produção), bloqueia alterações diretas de estado na UI,
 *   exigindo validação criptográfica via Firebase / Google Play Billing / Backend NestJS.
 */
object PlanGuard {

    private const val TAG = "PlanGuard"

    /**
     * Retorna se o ambiente atual permite troca manual para testes.
     */
    fun isManualSwitchAllowed(): Boolean {
        return BuildConfig.DEBUG
    }

    /**
     * Valida a tentativa de mudança de plano.
     */
    fun verifyPlanTransition(targetPlan: KudulaPlan, currentPlan: KudulaPlan): PlanSwitchVerification {
        if (targetPlan == currentPlan) {
            return PlanSwitchVerification.AlreadyActive
        }

        if (targetPlan.isCustomQuote) {
            SecureLogger.i(TAG, "Tentativa de adesão a plano empresarial: ${targetPlan.name}")
            return PlanSwitchVerification.CorporateContactRequired(targetPlan)
        }

        return if (isManualSwitchAllowed()) {
            SecureLogger.d(TAG, "Permitida troca manual em ambiente de DEBUG: ${currentPlan.name} -> ${targetPlan.name}")
            PlanSwitchVerification.AllowedInDebug
        } else {
            SecureLogger.w(TAG, "Troca manual bloqueada em RELEASE. Checkout remoto obrigatório para: ${targetPlan.name}")
            PlanSwitchVerification.RequiresCheckout(targetPlan)
        }
    }
}
