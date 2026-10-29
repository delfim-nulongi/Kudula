# Kudula Cognitive Operating System - Production R8/ProGuard Rules (OWASP MASVS Hardening)

# Preserve line numbers for safe crash reporting without leaking source file paths
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Compose Rules
-keepclassmembers class * {
    @androidx.compose.runtime.Composable *;
}
-dontwarn androidx.compose.**

# Room Database & Type Converters
-keepclassmembers class * extends androidx.room.RoomDatabase {
    <init>();
}
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keepclassmembers @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface *
-keepclassmembers @androidx.room.Dao interface * { *; }

# Security & Crypto Protection (Keystore, KeyStoreManager, Preferences)
-keep class com.example.data.security.** { *; }
-keepclassmembers class com.example.data.security.** { *; }

# Models & Data Transfer Objects
-keep class com.example.model.** { *; }
-keep class com.example.data.local.entity.** { *; }
-keep class com.example.data.remote.** { *; }

# OkHttp & Certificate Pinning
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.CertificatePinner { *; }

# Coroutines
-keepclassmembernames class kotlinx.coroutines.internal.MainDispatcherFactory {
    java.lang.String getLoadPriority();
}
-keepnames class kotlinx.coroutines.android.AndroidExceptionPreHandler
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler

