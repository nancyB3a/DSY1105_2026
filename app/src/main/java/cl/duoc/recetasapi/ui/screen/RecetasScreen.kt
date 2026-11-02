package cl.duoc.recetasapi.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.recetasapi.data.model.Receta
import cl.duoc.recetasapi.ui.viewmodel.RecetaViewModel
import cl.duoc.recetasapi.ui.viewmodel.RecetasUiState
import coil.compose.AsyncImage

/*
 * ⭐ PASO 6: PANTALLA (Compose)
 *
 * Observa el estado del ViewModel y dibuja según corresponda.
 * La pantalla NO llama a Retrofit directamente: solo le pide al ViewModel.
 */
@Composable
fun RecetasScreen(
    modifier: Modifier = Modifier,
    viewModel: RecetaViewModel = viewModel()
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    var texto by rememberSaveable { mutableStateOf("chicken") }

    Column(modifier.fillMaxSize().padding(16.dp)) {
        Text("Buscador de recetas", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Consumo de TheMealDB con Retrofit",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(vertical = 12.dp)
        ) {
            OutlinedTextField(
                value = texto,
                onValueChange = { texto = it },
                label = { Text("Ej: pasta, beef, cake") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            Button(onClick = { viewModel.buscar(texto) }) { Text("Buscar") }
        }

        // Un 'when' sobre el estado: cada caso dibuja algo distinto
        when (val e = estado) {
            is RecetasUiState.Inicial -> Unit
            is RecetasUiState.Cargando -> Centrado { CircularProgressIndicator() }
            is RecetasUiState.Error -> Centrado {
                Text(e.mensaje, color = MaterialTheme.colorScheme.error)
            }
            is RecetasUiState.Exito ->
                if (e.recetas.isEmpty()) {
                    Centrado { Text("No se encontraron recetas") }
                } else {
                    Text("${e.recetas.size} resultados", style = MaterialTheme.typography.labelLarge)
                    LazyColumn(
                        contentPadding = PaddingValues(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(e.recetas, key = { it.id }) { receta -> RecetaItem(receta) }
                    }
                }
        }
    }
}

@Composable
private fun RecetaItem(receta: Receta) {
    Card(Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            AsyncImage(
                model = receta.imagenUrl,
                contentDescription = receta.nombre,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(72.dp).clip(RoundedCornerShape(8.dp))
            )
            Column(Modifier.padding(start = 12.dp)) {
                Text(receta.nombre, fontWeight = FontWeight.Bold)
                Text(
                    "${receta.categoria ?: "-"} · ${receta.origen ?: "-"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
private fun Centrado(contenido: @Composable () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { contenido() }
}
