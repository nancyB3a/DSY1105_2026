package com.myapp.ui.viewmodel

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.myapp.MyApplication

/**
 * Los ViewModels de este proyecto necesitan repositorios en su constructor, y Android
 * por sí solo no sabe cómo crearlos. Esta "fábrica" le enseña cómo: toma los repositorios
 * del AppContainer (que vive en MyApplication) y se los entrega a cada ViewModel.
 *
 * Uso en una pantalla:  viewModel(factory = AppViewModelProvider.Factory)
 */
object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            LoginViewModel(
                usuarioRepository = miApp().container.usuarioRepository,
                sesionRepository = miApp().container.sesionRepository
            )
        }
        initializer {
            RegistroViewModel(
                usuarioRepository = miApp().container.usuarioRepository
            )
        }
        initializer {
            NotasViewModel(
                notaRepository = miApp().container.notaRepository,
                sesionRepository = miApp().container.sesionRepository
            )
        }
    }
}

// Obtiene la instancia de MyApplication desde los "extras" con los que Android crea el ViewModel
private fun CreationExtras.miApp(): MyApplication =
    this[APPLICATION_KEY] as MyApplication
