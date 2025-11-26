package com.example.huertohogar_mobil

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Clase principal de aplicación de HuertoHogar.
 *
 * Esta clase se inicializa antes que cualquier otra parte del código
 * y es necesaria para que Hilt configure automáticamente la inyección
 * de dependencias en toda la app.
 *
 * 🔹 Importante:
 *  - Está registrada en el AndroidManifest.xml como android:name=".HuertoHogarApp"
 *  - Requiere que el plugin Hilt esté activo y configurado en Gradle.
 */
@HiltAndroidApp
class HuertoHogarApp : Application()
