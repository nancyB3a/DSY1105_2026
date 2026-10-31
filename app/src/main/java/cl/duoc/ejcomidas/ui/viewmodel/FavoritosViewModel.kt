package cl.duoc.ejcomidas.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cl.duoc.ejcomidas.EjComidasApp
import cl.duoc.ejcomidas.data.model.Comida
import cl.duoc.ejcomidas.data.repository.ComidasRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Favoritos guardados con Room (antes data-local.service.ts + tab3.page.ts).
 * Se crea a nivel de Activity y lo comparten las tabs 2 y 3.
 */
class FavoritosViewModel(private val repository: ComidasRepository) : ViewModel() {

    val favoritos: StateFlow<List<Comida>> = repository.favoritos
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Ids de favoritos, para marcar el corazón en cualquier lista. */
    val idsFavoritos: StateFlow<Set<String>> = favoritos
        .map { lista -> lista.map { it.id }.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    // Mensajes de una sola vez (reemplaza al ToastController de Ionic)
    private val _mensajes = Channel<String>(Channel.BUFFERED)
    val mensajes: Flow<String> = _mensajes.receiveAsFlow()

    fun agregar(comida: Comida) {
        viewModelScope.launch {
            if (repository.agregarFavorito(comida)) {
                _mensajes.send("¡Agregado a favoritos!")
            } else {
                _mensajes.send("Ya estaba en favoritos")
            }
        }
    }

    fun quitar(comida: Comida) {
        viewModelScope.launch {
            if (repository.quitarFavorito(comida)) {
                _mensajes.send("Se ha eliminado de favoritos")
            }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as EjComidasApp
                FavoritosViewModel(app.repository)
            }
        }
    }
}
