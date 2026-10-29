package com.example.data.security

sealed class GuardianPromptResult {
    data class Safe(
        val sanitizedPrompt: String,
        val redactedPiiCount: Int,
        val originalLength: Int
    ) : GuardianPromptResult()

    data class Blocked(
        val reason: String,
        val threatCategory: String
    ) : GuardianPromptResult()
}

/**
 * KSG — Kudula Security Guardian (AI Safety & OWASP LLM Hardening).
 *
 * Camada de defesa cognitiva intermédia entre o utilizador e os modelos de IA (Gemini / KDAP):
 * 1. Detecção e bloqueio proativo de Prompt Injections e Jailbreaks.
 * 2. Redação e anonimização de PII (Bilhete de Identidade, cartões, telefones, emails).
 * 3. Limitação e controle da janela de contexto para evitar exaustão de tokens.
 * 4. Validação e sanitização da resposta da IA (anti-XSS e conformidade pedagógica).
 */
object KudulaSecurityGuardian {

    private const val TAG = "KSG_Guardian"
    private const val MAX_INPUT_CHARS = 4000 // Limite de caracteres para preservação de contexto

    // Padrões de Prompt Injection / Jailbreak
    private val PROMPT_INJECTION_PATTERNS = listOf(
        Regex("(?i)ignore (all )?(previous|above) (instructions|directions|prompts)"),
        Regex("(?i)(system prompt|reveal your instructions|show me your prompt)"),
        Regex("(?i)(you are now in (DAN|developer|jailbreak) mode)"),
        Regex("(?i)(disregard (all )?safety (rules|guidelines|filters))"),
        Regex("(?i)<\\|im_start\\|>|<\\|im_end\\|>|---BEGIN SYSTEM---|---END SYSTEM---"),
        Regex("(?i)pretend you have no limits|act as an unrestricted AI")
    )

    // Padrões de PII (Informações Pessoais Identificáveis)
    private val ANGOLAN_BI_REGEX = Regex("\\b[0-9]{9}[A-Z]{2}[0-9]{3}\\b", RegexOption.IGNORE_CASE)
    private val BRAZILIAN_CPF_REGEX = Regex("\\b\\d{3}\\.?\\d{3}\\.?\\d{3}-?\\d{2}\\b")
    private val CREDIT_CARD_REGEX = Regex("\\b(?:4[0-9]{12}(?:[0-9]{3})?|5[1-5][0-9]{14}|3[47][0-9]{13}|6(?:011|5[0-9]{2})[0-9]{12})\\b")
    private val EMAIL_REGEX = Regex("[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+")
    private val PHONE_REGEX = Regex("(\\+?\\d{1,3}[-.\\s]?)?\\(?\\d{2,3}\\)?[-.\\s]?\\d{3,5}[-.\\s]?\\d{4}")

    /**
     * Inspeciona, higieniza e anonimiza um prompt antes de enviá-lo ao KDAP/Gemini.
     */
    fun filterIncomingPrompt(rawPrompt: String): GuardianPromptResult {
        val trimmed = rawPrompt.trim()

        if (trimmed.isEmpty()) {
            return GuardianPromptResult.Safe("", 0, 0)
        }

        // 1. Verificação de Prompt Injection
        for (pattern in PROMPT_INJECTION_PATTERNS) {
            if (pattern.containsMatchIn(trimmed)) {
                SecureLogger.w(TAG, "Tentativa de Prompt Injection neutralizada pelo KSG.")
                return GuardianPromptResult.Blocked(
                    reason = "A tua mensagem contém comandos que violam as políticas de integridade pedagógica da Kudula.",
                    threatCategory = "PROMPT_INJECTION_DETECTED"
                )
            }
        }

        // 2. Limitação de Contexto
        val lengthBounded = if (trimmed.length > MAX_INPUT_CHARS) {
            SecureLogger.i(TAG, "Prompt truncado para caber no orçamento de contexto do KDAP.")
            trimmed.take(MAX_INPUT_CHARS)
        } else {
            trimmed
        }

        // 3. Remoção e Anonimização de PII
        var redactedCount = 0
        var sanitized = lengthBounded

        // Mascarar BI Angolano
        sanitized = ANGOLAN_BI_REGEX.replace(sanitized) {
            redactedCount++
            "[BI_ANONIMIZADO]"
        }

        // Mascarar CPF
        sanitized = BRAZILIAN_CPF_REGEX.replace(sanitized) {
            redactedCount++
            "[CPF_ANONIMIZADO]"
        }

        // Mascarar Cartão de Crédito
        sanitized = CREDIT_CARD_REGEX.replace(sanitized) {
            redactedCount++
            "[CARTAO_ANONIMIZADO]"
        }

        // Mascarar Email
        sanitized = EMAIL_REGEX.replace(sanitized) {
            redactedCount++
            "[EMAIL_ANONIMIZADO]"
        }

        // Mascarar Telefones
        sanitized = PHONE_REGEX.replace(sanitized) {
            redactedCount++
            "[TELEFONE_ANONIMIZADO]"
        }

        if (redactedCount > 0) {
            SecureLogger.i(TAG, "KSG anonimizou $redactedCount dados de PII antes do envio à IA.")
        }

        return GuardianPromptResult.Safe(
            sanitizedPrompt = sanitized,
            redactedPiiCount = redactedCount,
            originalLength = trimmed.length
        )
    }

    /**
     * Higieniza e valida o output retornado pelo modelo de linguagem.
     */
    fun sanitizeOutgoingAiResponse(rawResponse: String): String {
        var clean = rawResponse
        // Prevenir tags HTML executáveis perigosas
        clean = clean.replace(Regex("(?i)<script.*?>.*?</script>"), "[SCRIPT_REMOVIDO]")
        clean = clean.replace(Regex("(?i)<iframe.*?>.*?</iframe>"), "")
        clean = clean.replace(Regex("(?i)javascript:"), "")
        return clean
    }
}
