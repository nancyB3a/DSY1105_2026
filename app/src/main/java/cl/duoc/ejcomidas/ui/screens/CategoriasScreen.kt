package cl.duoc.ejcomidas.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.ejcomidas.ui.components.CargandoView
import cl.duoc.ejcomidas.ui.components.CategoriaCard
import cl.duoc.ejcomidas.ui.components.ErrorView
import cl.duoc.ejcomidas.ui.viewmodel.CategoriasViewModel
import cl.duoc.ejcomidas.ui.viewmodel.UiState

/** Tab 1 "Tipos" (antes tab1.page.html + comidas.component). */
@Composable
fun CategoriasScreen(
    modifier: Modifier = Modifier,
    viewModel: CategoriasViewModel = viewModel(factory = CategoriasViewModel.Factory)
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()

    when (val e = estado) {
        is UiState.Cargando -> CargandoView(modifier)
        is UiState.Error -> ErrorView(e.mensaje, viewModel::cargarCategorias, modifier)
        is UiState.Exito -> LazyVerticalGrid(
            // Equivale a las columnas responsivas del ion-grid
            columns = GridCells.Adaptive(minSize = 300.dp),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = modifier
        ) {
            items(e.datos, key = { it.id }) { categoria ->
                CategoriaCard(categoria)
            }
        }
    }
}
