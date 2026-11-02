package cl.duoc.recetasapi.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.recetasapi.data.model.Receta
import cl.duoc.recetasapi.data.repository.RecetaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/*
 * ⭐ PASO 5: VIEWMODEL
 *
 * - Llama al repositorio dentro de una corrutina (viewModelScope.launch).
 * - Guarda el resultado en un StateFlow que la pantalla observa.
 * - Maneja los 3 estados típicos de una llamada a una API.
 */

/** Estados posibles de la pantalla. */
sealed interface RecetasUiState {
    data object Inicial : RecetasUiState
    data object Cargando : RecetasUiState
    data class Exito(val recetas: List<Receta>) : RecetasUiState
    data class Error(val mensaje: String) : RecetasUiState
}

class RecetaViewModel(
    private val repository: RecetaRepository = RecetaRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<RecetasUiState>(RecetasUiState.Inicial)
    val uiState: StateFlow<RecetasUiState> = _uiState.asStateFlow()

    init {
        buscar("chicken") // búsqueda inicial para que la pantalla no parta vacía
    }

    fun buscar(texto: String) {
        viewModelScope.launch {
            _uiState.value = RecetasUiState.Cargando
            try {
                val recetas = repository.buscarRecetas(texto.trim())
                _uiState.value = RecetasUiState.Exito(recetas)
            } catch (e: Exception) {
                // Sin internet, servidor caído, JSON distinto al esperado, etc.
                _uiState.value = RecetasUiState.Error("No se pudo conectar: ${e.message}")
            }
        }
    }
}
