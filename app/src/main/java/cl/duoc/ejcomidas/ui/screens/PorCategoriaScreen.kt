package cl.duoc.ejcomidas.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.ejcomidas.ui.components.CargandoView
import cl.duoc.ejcomidas.ui.components.ErrorView
import cl.duoc.ejcomidas.ui.viewmodel.FavoritosViewModel
import cl.duoc.ejcomidas.ui.viewmodel.PorCategoriaViewModel
import cl.duoc.ejcomidas.ui.viewmodel.UiState

/** Tab 2 "Por categoría" (antes tab2.page.html con ion-segment). */
@Composable
fun PorCategoriaScreen(
    favoritosViewModel: FavoritosViewModel,
    modifier: Modifier = Modifier,
    viewModel: PorCategoriaViewModel = viewModel(factory = PorCategoriaViewModel.Factory)
) {
    val seleccionada by viewModel.categoriaSeleccionada.collectAsStateWithLifecycle()
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val idsFavoritos by favoritosViewModel.idsFavoritos.collectAsStateWithLifecycle()

    Column(modifier.fillMaxSize()) {
        // ScrollableTabRow = ion-segment scrollable
        ScrollableTabRow(
            selectedTabIndex = viewModel.tiposComida.indexOf(seleccionada),
            edgePadding = 8.dp,
            containerColor = MaterialTheme.colorScheme.background
        ) {
            viewModel.tiposComida.forEach { tipo ->
                Tab(
                    selected = tipo == seleccionada,
                    onClick = { viewModel.seleccionarCategoria(tipo) },
                    text = { Text(tipo) },
                    selectedContentColor = MaterialTheme.colorScheme.primary,
                    unselectedContentColor = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        when (val e = estado) {
            is UiState.Cargando -> CargandoView()
            is UiState.Error -> ErrorView(e.mensaje, viewModel::reintentar)
            is UiState.Exito ->
                if (e.datos.isEmpty()) {
                    Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("No hay comidas en esta categoría")
                    }
                } else {
                    ListaComidas(
                        comidas = e.datos,
                        idsFavoritos = idsFavoritos,
                        onAgregarFavorito = favoritosViewModel::agregar,
                        onQuitarFavorito = favoritosViewModel::quitar
                    )
                }
        }
    }
}
