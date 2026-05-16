# Namma-Shaale Inventory — ProGuard Rules

# Keep source file names and line numbers for meaningful crash reports
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ── Room Database ──────────────────────────────────────────────────────────────
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# ── Hilt / Dagger ──────────────────────────────────────────────────────────────
-keep class dagger.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.HiltAndroidApp

# ── Retrofit + OkHttp ──────────────────────────────────────────────────────────
-keepattributes Signature
-keepattributes *Annotation*
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**
-keep class retrofit2.** { *; }
-keep interface retrofit2.** { *; }
-dontwarn retrofit2.**

# ── Gson (JSON serialization) ───────────────────────────────────────────────────
-keepattributes Signature
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer
# Keep all Groq API request/response models
-keep class com.nammashalli.inventory.network.** { *; }

# ── Kotlin Coroutines ───────────────────────────────────────────────────────────
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

# ── Kotlin Serialization ────────────────────────────────────────────────────────
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

# ── Android Keystore (EncryptionUtil) ──────────────────────────────────────────
-keep class android.security.keystore.** { *; }

# ── DataStore ───────────────────────────────────────────────────────────────────
-keep class androidx.datastore.** { *; }

# ── Coil ────────────────────────────────────────────────────────────────────────
-dontwarn coil.**
