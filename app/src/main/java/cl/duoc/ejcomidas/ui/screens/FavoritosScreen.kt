package cl.duoc.ejcomidas.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.duoc.ejcomidas.ui.viewmodel.FavoritosViewModel

/** Tab 3 "Favoritas" (antes tab3.page.html), leídas desde Room. */
@Composable
fun FavoritosScreen(
    favoritosViewModel: FavoritosViewModel,
    modifier: Modifier = Modifier
) {
    val favoritos by favoritosViewModel.favoritos.collectAsStateWithLifecycle()

    if (favoritos.isEmpty()) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Filled.FavoriteBorder,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(72.dp)
            )
            Text(
                "No tienes favoritos guardados",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )
        }
    } else {
        ListaComidas(
            comidas = favoritos,
            idsFavoritos = favoritos.map { it.id }.toSet(),
            onAgregarFavorito = favoritosViewModel::agregar,
            onQuitarFavorito = favoritosViewModel::quitar,
            modifier = modifier
        )
    }
}
