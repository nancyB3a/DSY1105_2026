package cl.duoc.ejcomidas.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cl.duoc.ejcomidas.EjComidasApp
import cl.duoc.ejcomidas.data.model.Comida
import cl.duoc.ejcomidas.data.repository.ComidasRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Tab 2 "Por categoría": comidas filtradas según la pestaña elegida (antes tab2.page.ts). */
class PorCategoriaViewModel(private val repository: ComidasRepository) : ViewModel() {

    // Mismas categorías que el arreglo 'tiposcomida' del proyecto Ionic
    val tiposComida = listOf(
        "Beef", "Chicken", "Dessert", "Lamb", "Miscellaneous", "Pasta", "Pork",
        "Seafood", "Side", "Starter", "Vegan", "Vegetarian", "Breakfast", "Goat"
    )

    private val _categoriaSeleccionada = MutableStateFlow(tiposComida.first())
    val categoriaSeleccionada: StateFlow<String> = _categoriaSeleccionada.asStateFlow()

    private val _estado = MutableStateFlow<UiState<List<Comida>>>(UiState.Cargando)
    val estado: StateFlow<UiState<List<Comida>>> = _estado.asStateFlow()

    private var trabajoCarga: Job? = null

    init {
        cargarComidas(tiposComida.first())
    }

    /** Equivale al evento (ionChange) del ion-segment. */
    fun seleccionarCategoria(categoria: String) {
        if (categoria == _categoriaSeleccionada.value && _estado.value is UiState.Exito) return
        _categoriaSeleccionada.value = categoria
        cargarComidas(categoria)
    }

    fun reintentar() = cargarComidas(_categoriaSeleccionada.value)

    private fun cargarComidas(categoria: String) {
        // Si el usuario cambia rápido de pestaña, se cancela la petición anterior
        trabajoCarga?.cancel()
        trabajoCarga = viewModelScope.launch {
            _estado.value = UiState.Cargando
            _estado.value = try {
                UiState.Exito(repository.obtenerComidasPorCategoria(categoria))
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
                PorCategoriaViewModel(app.repository)
            }
        }
    }
}
