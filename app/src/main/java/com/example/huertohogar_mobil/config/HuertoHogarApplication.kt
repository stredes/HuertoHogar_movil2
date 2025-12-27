package com.example.huertohogar_mobil.config

import android.app.Application
import android.util.Log

private const val TAG = "RedPrivadaApp"

/**
 * NOTA: Esta clase ya no se usa.
 * La funcionalidad fue migrada a HuertoHogarApp.kt
 * Se mantiene temporalmente para referencia.
 * @deprecated Usar HuertoHogarApp en su lugar
 */
@Deprecated("Usar HuertoHogarApp en su lugar")
class HuertoHogarApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        Log.d(TAG, "════════════════════════════════════════════════════════════")
        Log.d(TAG, "🚀 INICIANDO RED PRIVADA")
        Log.d(TAG, "════════════════════════════════════════════════════════════")
        Log.d(TAG, "Sincronización bidireccional: LISTA")
        Log.d(TAG, "Firebase persistence: HABILITADA")
        Log.d(TAG, "Offline-first: ACTIVO")
        Log.d(TAG, "════════════════════════════════════════════════════════════")
        Log.d(TAG, "")
        Log.d(TAG, "La aplicación está lista para usar sincronización bidireccional.")
        Log.d(TAG, "Para activarla, llama a: SincronizacionBidireccional.activar()")
        Log.d(TAG, "")
    }
}

