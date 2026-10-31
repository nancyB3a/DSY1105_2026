package cl.duoc.ejcomidas.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.HeartBroken
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cl.duoc.ejcomidas.data.model.Comida
import android.widget.Toast
import cl.duoc.ejcomidas.util.compartirReceta
import cl.duoc.ejcomidas.util.enviarRecetaPorEmail
import cl.duoc.ejcomidas.util.urlReceta
import coil.compose.AsyncImage

/**
 * Tarjeta de una comida (antes meal.component).
 * - Tocar el nombre o la imagen abre la receta en el navegador (InAppBrowser).
 * - El menú ⋮ reemplaza al ActionSheet: Compartir en… / Enviar por email /
 *   Agregar o Quitar de favoritos.
 */
@Composable
fun ComidaCard(
    comida: Comida,
    esFavorita: Boolean,
    onAgregarFavorito: (Comida) -> Unit,
    onQuitarFavorito: (Comida) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    var menuAbierto by remember { mutableStateOf(false) }
    val abrirReceta = { uriHandler.openUri(urlReceta(comida)) }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 16.dp, top = 8.dp, end = 4.dp)
        ) {
            Text(
                text = comida.nombre,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .weight(1f)
                    .clickable { abrirReceta() }
            )
            Box {
                IconButton(onClick = { menuAbierto = true }) {
                    Icon(Icons.Filled.MoreVert, contentDescription = "Opciones")
                }
                DropdownMenu(expanded = menuAbierto, onDismissRequest = { menuAbierto = false }) {
                    DropdownMenuItem(
                        text = { Text("Compartir en…") },
                        leadingIcon = { Icon(Icons.Filled.Share, null) },
                        onClick = {
                            menuAbierto = false
                            compartirReceta(context, comida)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Enviar por email") },
                        leadingIcon = { Icon(Icons.Filled.Email, null) },
                        onClick = {
                            menuAbierto = false
                            if (!enviarRecetaPorEmail(context, comida)) {
                                Toast.makeText(
                                    context,
                                    "No hay una app de correo configurada",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    )
                    HorizontalDivider()
                    if (esFavorita) {
                        DropdownMenuItem(
                            text = { Text("Quitar de favoritos") },
                            leadingIcon = { Icon(Icons.Filled.HeartBroken, null) },
                            onClick = {
                                menuAbierto = false
                                onQuitarFavorito(comida)
                            }
                        )
                    } else {
                        DropdownMenuItem(
                            text = { Text("Agregar a favoritos") },
                            leadingIcon = { Icon(Icons.Filled.Favorite, null) },
                            onClick = {
                                menuAbierto = false
                                onAgregarFavorito(comida)
                            }
                        )
                    }
                    DropdownMenuItem(
                        text = { Text("Cancelar") },
                        leadingIcon = { Icon(Icons.Filled.Close, null) },
                        onClick = { menuAbierto = false }
                    )
                }
            }
        }

        if (comida.imagenUrl.isNotBlank()) {
            AsyncImage(
                model = comida.imagenUrl,
                contentDescription = comida.nombre,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { abrirReceta() }
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            if (esFavorita) {
                Icon(
                    Icons.Filled.Favorite,
                    contentDescription = "Favorita",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                "Toca la imagen para ver la receta",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
