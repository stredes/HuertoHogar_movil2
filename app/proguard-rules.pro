# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# ========================================
# 🔒 SEGURIDAD: Eliminar logs en producción
# ========================================

# Eliminar logs de android.util.Log EXCEPTO errores críticos
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
    public static int w(...);
}

# Mantener solo errores fatales
-keepclassmembers class android.util.Log {
    public static int e(...);
}

# Mantener SecureLogger pero permitir optimización
-keep class com.example.huertohogar_mobil.utils.SecureLogger {
    public *;
}

# ========================================
# 🔐 PROTECCIÓN DE DATOS SENSIBLES
# ========================================

# Proteger clases relacionadas con autenticación
-keep class com.example.huertohogar_mobil.data.Usuario { *; }
-keep class com.example.huertohogar_mobil.data.Mensaje { *; }

# Mantener nombres de campos en modelos para serialización
-keepclassmembers class com.example.huertohogar_mobil.data.** {
    <fields>;
}

# ========================================
# FIREBASE
# ========================================

# Mantener clases de Firebase
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

# ========================================
# RETROFIT / GSON
# ========================================

-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# ========================================
# KOTLIN
# ========================================

-keep class kotlin.** { *; }
-keep class kotlinx.** { *; }
-dontwarn kotlin.**
-dontwarn kotlinx.**

# Mantener metadata de Kotlin
-keepattributes *Annotation*
-keepattributes RuntimeVisibleAnnotations
-keepattributes RuntimeInvisibleAnnotations

# ========================================
# COMPOSE
# ========================================

-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# ========================================
# HILT / DAGGER
# ========================================

-keep class dagger.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }

# ========================================
# ROOM
# ========================================

-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keepclassmembers class * extends androidx.room.RoomDatabase {
    public static ** sInstance;
}

# ========================================
# GENERAL
# ========================================

# Mantener información de líneas para stack traces útiles
-keepattributes SourceFile,LineNumberTable

# Renombrar archivo fuente para ofuscar
-renamesourcefileattribute SourceFile

