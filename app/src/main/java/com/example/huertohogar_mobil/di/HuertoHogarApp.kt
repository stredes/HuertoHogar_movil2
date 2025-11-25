package com.example.huertohogar_mobil.di

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Clase Application personalizada, necesaria para inicializar Hilt.
 * La anotación @HiltAndroidApp activa la generación de código de Hilt.
 */
@HiltAndroidApp
class HuertoHogarApp : Application()