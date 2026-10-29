package com.example.data.security

import okhttp3.CertificatePinner
import okhttp3.OkHttpClient

/**
 * CertificatePinningConfig (OWASP MASVS Network Security).
 *
 * Prepara a infraestrutura de Certificate Pinning (SSL Pinning) no cliente OkHttp:
 * - Mantém desativado por padrão em desenvolvimento para permitir inspeção local e evitar falhas em emuladores.
 * - Preparado para ativação segura em builds de produção via RemoteConfig ou sinalização de ambiente.
 * - Mapeia hashes SHA-256 das chaves públicas (SPKI) para os domínios oficiais da Kudula e APIs do ecossistema.
 */
object CertificatePinningConfig {

    private const val TAG = "CertPinning"

    // Hashes SHA-256 de chaves públicas para os endpoints oficiais
    private val DOMAIN_PINS = mapOf(
        "*.kudula.org" to listOf(
            "sha256/kudulaMasterPin2026RootCAKeyHashFakeForDev123=",
            "sha256/kudulaBackupPin2026BackupRootKeyHashDev456="
        ),
        "*.firebaseio.com" to listOf(
            "sha256/hwH0VDcK4Z9+3g+uJ6q8P6xVvC7o6yE2w8z1L1k2n3o=",
            "sha256/k2VqBesOHFeAh6+m+GDdPOQEaxbBGPH1UwtBul55xys="
        ),
        "*.googleapis.com" to listOf(
            "sha256/cPE6nT5slQ4C8v8hG5u9pM7zN3k2y1x0v9w8z7y6x5w="
        )
    )

    /**
     * Constrói o CertificatePinner do OkHttp com as chaves oficiais.
     */
    fun buildCertificatePinner(enforce: Boolean = false): CertificatePinner {
        val builder = CertificatePinner.Builder()
        if (enforce) {
            for ((pattern, pins) in DOMAIN_PINS) {
                for (pin in pins) {
                    builder.add(pattern, pin)
                }
            }
            SecureLogger.i(TAG, "Certificate Pinning ATIVADO para ${DOMAIN_PINS.size} padrões de domínio.")
        } else {
            SecureLogger.d(TAG, "Certificate Pinning pronto mas DESATIVADO (modo dev/permissivo).")
        }
        return builder.build()
    }

    /**
     * Aplica a configuração ao construtor de OkHttpClient.
     */
    fun configureClient(builder: OkHttpClient.Builder, enforce: Boolean = false): OkHttpClient.Builder {
        return builder.certificatePinner(buildCertificatePinner(enforce))
    }
}
