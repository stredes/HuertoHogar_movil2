// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.kapt) apply false

    // 🔹 agrega el plugin de Hilt aquí con versión
    id("com.google.dagger.hilt.android") version "2.51.1" apply false
}
