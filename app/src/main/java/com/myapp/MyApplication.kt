package com.myapp

import android.app.Application
import com.myapp.di.AppContainer

/**
 * Se crea UNA vez cuando arranca el proceso de la app y vive hasta que se cierra,
 * a diferencia de la Activity, que se destruye y recrea al rotar la pantalla.
 * Por eso el AppContainer vive aquí: así la sesión y los repositorios no se pierden al rotar.
 */
class MyApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
