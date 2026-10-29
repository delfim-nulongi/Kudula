package com.example.data.security

import android.content.Context
import com.example.BuildConfig
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory

/**
 * KudulaAppCheckManager (F0.7 Firebase App Check Foundation).
 *
 * Prepara a camada de atestação de integridade de dispositivo e aplicação:
 * - Em DEBUG: Utiliza DebugAppCheckProviderFactory para permitir testes em emuladores e CI/CD.
 * - Em RELEASE: Prepara o PlayIntegrityAppCheckProviderFactory (Google Play Integrity API).
 *
 * Diretrizes:
 * - Não ativa bloqueio (enforcement) forçado que impeça o acesso enquanto o SHA-256 definitivo
 *   e a vinculação no Firebase Console não forem concluídos pelo administrador.
 * - Fornece telemetria não intrusiva via [SecureLogger].
 */
object KudulaAppCheckManager {

    private const val TAG = "KudulaAppCheck"

    fun initAppCheck(context: Context) {
        try {
            FirebaseApp.initializeApp(context)
            val firebaseAppCheck = FirebaseAppCheck.getInstance()

            if (BuildConfig.DEBUG) {
                firebaseAppCheck.installAppCheckProviderFactory(
                    DebugAppCheckProviderFactory.getInstance()
                )
                SecureLogger.d(TAG, "App Check inicializado em modo DEBUG com DebugAppCheckProviderFactory.")
            } else {
                firebaseAppCheck.installAppCheckProviderFactory(
                    PlayIntegrityAppCheckProviderFactory.getInstance()
                )
                SecureLogger.i(TAG, "App Check preparado para produção com PlayIntegrityAppCheckProviderFactory (Enforcement desativado na consola até registro de SHA-256).")
            }
        } catch (e: Exception) {
            SecureLogger.w(TAG, "Aviso ao inicializar Firebase App Check (Modo permissivo mantido): ${e.message}")
        }
    }
}
