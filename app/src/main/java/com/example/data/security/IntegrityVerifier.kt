package com.example.data.security

import android.os.Build
import android.os.Debug
import com.example.BuildConfig
import java.io.File

data class DeviceIntegrityReport(
    val isDebuggerAttached: Boolean,
    val hasRootBinaries: Boolean,
    val isRunningOnEmulator: Boolean,
    val isReleaseBuild: Boolean,
    val integrityScore: Int // 0 a 100
)

/**
 * IntegrityVerifier (OWASP MASVS / F0.6 Hardening).
 *
 * Realiza verificações não invasivas de integridade no dispositivo:
 * - Detecta presença de binários comuns de root e adulteração.
 * - Detecta emuladores e analisadores de depuração acoplados.
 * - Integração limpa com o Firebase App Check já presente no projeto.
 * - Não bloqueia abruptamente o utilizador em desenvolvimento, mantendo telemetria defensiva.
 */
object IntegrityVerifier {

    private const val TAG = "IntegrityVerifier"

    private val ROOT_PATHS = listOf(
        "/system/app/Superuser.apk",
        "/sbin/su",
        "/system/bin/su",
        "/system/xbin/su",
        "/data/local/xbin/su",
        "/data/local/bin/su",
        "/system/sd/xbin/su",
        "/system/bin/failsafe/su",
        "/data/local/su"
    )

    fun assessDeviceIntegrity(): DeviceIntegrityReport {
        val debugger = Debug.isDebuggerConnected() || Debug.waitingForDebugger()
        val root = checkRootBinaries()
        val emulator = checkEmulatorEnvironment()
        val isRelease = !BuildConfig.DEBUG

        var score = 100
        if (root) score -= 40
        if (debugger && isRelease) score -= 30
        if (emulator) score -= 10

        val report = DeviceIntegrityReport(
            isDebuggerAttached = debugger,
            hasRootBinaries = root,
            isRunningOnEmulator = emulator,
            isReleaseBuild = isRelease,
            integrityScore = score.coerceIn(0, 100)
        )

        SecureLogger.d(TAG, "Relatório de integridade do dispositivo: score=${report.integrityScore}")
        return report
    }

    private fun checkRootBinaries(): Boolean {
        for (path in ROOT_PATHS) {
            try {
                if (File(path).exists()) return true
            } catch (_: Exception) {
                // Permissão negada ou sandbox restritivo é sinal positivo
            }
        }
        return false
    }

    private fun checkEmulatorEnvironment(): Boolean {
        return (Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || "google_sdk" == Build.PRODUCT)
    }
}
