package cl.duoc.ejcomidas.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.duoc.ejcomidas.data.model.Comida
import cl.duoc.ejcomidas.ui.components.ComidaCard

/** Grilla de comidas reutilizada por las tabs 2 y 3 (antes meals.component). */
@Composable
fun ListaComidas(
    comidas: List<Comida>,
    idsFavoritos: Set<String>,
    onAgregarFavorito: (Comida) -> Unit,
    onQuitarFavorito: (Comida) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 300.dp),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
    ) {
        items(comidas, key = { it.id }) { comida ->
            ComidaCard(
                comida = comida,
                esFavorita = comida.id in idsFavoritos,
                onAgregarFavorito = onAgregarFavorito,
                onQuitarFavorito = onQuitarFavorito,
                // Anima la entrada/salida (reemplaza a animate.css)
                modifier = Modifier.animateItem()
            )
        }
    }
}
