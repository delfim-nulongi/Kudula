package com.example.data.security

import android.content.Context

/**
 * EncryptedRoomHelper (OWASP MASVS / F0.6 Hardening).
 *
 * Providencia criptografia transparente na camada de persistência local do Room:
 * - Criptografa campos com PII e propriedade intelectual cognitiva (reflexões pessoais, notas do KLSE,
 *   transcrições de voz e telemetria de eventos) usando chaves protegidas pelo Android KeyStore (AES-256 GCM).
 * - Preparado para transição para o motor SQLCipher (net.zetetic) quando bibliotecas nativas C++
 *   estiverem disponíveis no ambiente de produção.
 */
object EncryptedRoomHelper {

    private const val TAG = "EncryptedRoom"

    /**
     * Encripta um texto sensível antes de persistir no banco de dados Room.
     */
    fun encryptField(plainText: String?): String? {
        if (plainText.isNullOrEmpty()) return plainText
        return KudulaKeyStoreManager.encrypt(plainText)
    }

    /**
     * Desencripta um texto recuperado do banco de dados Room.
     */
    fun decryptField(cipherText: String?): String? {
        if (cipherText.isNullOrEmpty()) return cipherText
        return KudulaKeyStoreManager.decrypt(cipherText)
    }

    /**
     * Gera a chave de encriptação da base de dados protegida por hardware
     * para uso futuro com SQLCipher SupportOpenHelperFactory.
     */
    fun getDatabasePassphrase(context: Context): ByteArray {
        val securePrefs = KudulaSecurePreferences.getInstance(context)
        var passphrase = securePrefs.getEncryptedString("room_master_passphrase", "")
        if (passphrase.isEmpty()) {
            passphrase = java.util.UUID.randomUUID().toString() + "-" + System.currentTimeMillis()
            securePrefs.putEncryptedString("room_master_passphrase", passphrase)
            SecureLogger.i(TAG, "Nova passphrase mestra de base de dados gerada e protegida no KeyStore.")
        }
        return passphrase.toByteArray(Charsets.UTF_8)
    }
}
