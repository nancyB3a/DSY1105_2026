package cl.duoc.ejcomidas.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cl.duoc.ejcomidas.EjComidasApp
import cl.duoc.ejcomidas.data.model.Categoria
import cl.duoc.ejcomidas.data.repository.ComidasRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Tab 1 "Tipos": lista general de categorías (antes tab1.page.ts). */
class CategoriasViewModel(private val repository: ComidasRepository) : ViewModel() {

    private val _estado = MutableStateFlow<UiState<List<Categoria>>>(UiState.Cargando)
    val estado: StateFlow<UiState<List<Categoria>>> = _estado.asStateFlow()

    init {
        cargarCategorias()
    }

    fun cargarCategorias() {
        viewModelScope.launch {
            _estado.value = UiState.Cargando
            _estado.value = try {
                UiState.Exito(repository.obtenerCategorias())
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                UiState.Error(e.aMensaje())
            }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as EjComidasApp
                CategoriasViewModel(app.repository)
            }
        }
    }
}
