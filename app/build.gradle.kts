plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "cl.duoc.recetasapi"
    compileSdk = 34

    defaultConfig {
        applicationId = "cl.duoc.recetasapi"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)   // viewModel() en Compose
    implementation(libs.androidx.lifecycle.runtime.compose)     // collectAsStateWithLifecycle()

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    debugImplementation(libs.androidx.ui.tooling)

    // ⭐ Retrofit: cliente HTTP + convertidor JSON -> objetos Kotlin (Gson)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)

    // Coil: cargar imágenes desde una URL
    implementation(libs.coil.compose)
}
