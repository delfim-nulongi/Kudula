package com.example.data.security

import android.util.Log
import com.example.BuildConfig

/**
 * SecureLogger (OWASP MASVS Hardening).
 *
 * Garante que dados sensíveis (PII, tokens, senhas, chaves de API, prompts integrais de IA)
 * nunca sejam expostos nos logs do logcat do sistema.
 * Em builds de RELEASE, logs verbosos e de depuração são completamente silenciados.
 */
object SecureLogger {

    private val EMAIL_REGEX = Regex("([a-zA-Z0-9_.+-])[a-zA-Z0-9_.+-]*@([a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+)")
    private val TOKEN_REGEX = Regex("(Bearer\\s+)[a-zA-Z0-9-_.]+", RegexOption.IGNORE_CASE)
    private val SENSITIVE_KEY_REGEX = Regex("(?i)(password|secret|apikey|token|auth)\\s*[:=]\\s*[^,\\s]+")

    fun d(tag: String, message: String) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, sanitize(message))
        }
    }

    fun i(tag: String, message: String) {
        Log.i(tag, sanitize(message))
    }

    fun w(tag: String, message: String, throwable: Throwable? = null) {
        val sanitized = sanitize(message)
        if (throwable != null) {
            Log.w(tag, sanitized, throwable)
        } else {
            Log.w(tag, sanitized)
        }
    }

    fun e(tag: String, message: String, throwable: Throwable? = null) {
        val sanitized = sanitize(message)
        if (throwable != null) {
            Log.e(tag, sanitized, throwable)
        } else {
            Log.e(tag, sanitized)
        }
    }

    /**
     * Remove ou mascara PII e credenciais das mensagens de log.
     */
    fun sanitize(input: String): String {
        var clean = input
        clean = EMAIL_REGEX.replace(clean) { matchResult ->
            val firstChar = matchResult.groupValues[1]
            val domain = matchResult.groupValues[2]
            "${firstChar}***@$domain"
        }
        clean = TOKEN_REGEX.replace(clean, "$1[REDACTED_TOKEN]")
        clean = SENSITIVE_KEY_REGEX.replace(clean, "$1=[REDACTED]")
        return clean
    }

    /**
     * Sanitiza e trunca prompts para telemetria sem vazamento de privacidade.
     */
    fun sanitizePrompt(prompt: String): String {
        val clean = sanitize(prompt)
        return if (clean.length > 80) "${clean.take(80)}... [TRUNCATED_${clean.length}_CHARS]" else clean
    }
}
