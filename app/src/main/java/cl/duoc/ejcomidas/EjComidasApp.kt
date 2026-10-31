package cl.duoc.ejcomidas

import android.app.Application
import cl.duoc.ejcomidas.data.local.AppDatabase
import cl.duoc.ejcomidas.data.remote.RetrofitClient
import cl.duoc.ejcomidas.data.repository.ComidasRepository

/**
 * Clase Application: crea las dependencias una sola vez (inyección manual).
 * Los ViewModels obtienen el repositorio desde aquí mediante su Factory.
 */
class EjComidasApp : Application() {

    val repository: ComidasRepository by lazy {
        ComidasRepository(
            api = RetrofitClient.api,
            favoritoDao = AppDatabase.getDatabase(this).favoritoDao()
        )
    }
}
