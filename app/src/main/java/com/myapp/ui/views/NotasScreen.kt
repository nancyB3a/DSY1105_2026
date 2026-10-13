package com.myapp.ui.views

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.myapp.ui.viewmodel.AppViewModelProvider
import com.myapp.ui.viewmodel.NotasEvento
import com.myapp.ui.viewmodel.NotasViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotasScreen(
    onSesionCerrada: () -> Unit,
    viewModel: NotasViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Eventos de una sola vez: mensajes (Toast) y volver al Login
    LaunchedEffect(Unit) {
        viewModel.eventos.collect { evento ->
            when (evento) {
                is NotasEvento.Mensaje ->
                    Toast.makeText(context, evento.texto, Toast.LENGTH_SHORT).show()
                NotasEvento.SesionCerrada -> {
                    Toast.makeText(context, "Sesión cerrada", Toast.LENGTH_SHORT).show()
                    onSesionCerrada()
                }
            }
        }
    }

    // ---------- ALERT DIALOG 1: confirmar borrado de nota ----------
    // El diálogo aparece mientras el ESTADO tenga una nota por borrar (notaPorBorrar != null).
    uiState.notaPorBorrar?.let { aBorrar ->
        AlertDialog(
            onDismissRequest = viewModel::cancelarBorrado,
            title = { Text("Borrar nota") },
            text = { Text("¿Seguro que quieres borrar \"${aBorrar.texto}\"? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = viewModel::confirmarBorrado) {
                    Text("Borrar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::cancelarBorrado) { Text("Cancelar") }
            }
        )
    }

    // ---------- ALERT DIALOG 2: confirmar cierre de sesión ----------
    if (uiState.mostrarDialogoSalir) {
        AlertDialog(
            onDismissRequest = viewModel::cancelarCerrarSesion,
            title = { Text("Cerrar sesión") },
            text = { Text("¿Quieres salir de tu cuenta? Tus notas quedan guardadas.") },
            confirmButton = {
                TextButton(onClick = viewModel::confirmarCerrarSesion) { Text("Salir") }
            },
            dismissButton = {
                TextButton(onClick = viewModel::cancelarCerrarSesion) { Text("Quedarme") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = { Text("Notas") },
                actions = {
                    TextButton(onClick = viewModel::pedirCerrarSesion) {
                        Text("Salir", color = MaterialTheme.colorScheme.primary)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = uiState.textoNota,
                onValueChange = viewModel::onTextoNotaChange,
                label = { Text("Escribe una nota") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            Button(onClick = viewModel::guardarNota) {
                Text("Guardar Nota")
            }
            Spacer(Modifier.height(16.dp))

            Text("Notas guardadas:")
            LazyColumn {
                // key = id de la nota: Compose sabe QUÉ fila es cuál y anima bien al agregar/quitar
                items(uiState.notas, key = { it.id }) { nota ->
                    NotaItem(
                        texto = nota.texto,
                        saliendo = nota.id in uiState.idsSaliendo,
                        onBorrarClick = { viewModel.pedirBorrarNota(nota) },
                        modifier = Modifier.animateItem()
                    )
                }
            }
        }
    }
}

/** Una fila de la lista, con animación de entrada (al aparecer) y de salida (al borrar). */
@Composable
private fun NotaItem(
    texto: String,
    saliendo: Boolean,
    onBorrarClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // La fila "nace" invisible y pasa a visible -> se ejecuta la animación de ENTRADA
    val estadoVisible = remember { MutableTransitionState(false).apply { targetState = true } }
    // Cuando el ViewModel marca la nota como "saliendo", se ejecuta la animación de SALIDA
    estadoVisible.targetState = !saliendo

    AnimatedVisibility(
        visibleState = estadoVisible,
        modifier = modifier,
        enter = slideInHorizontally(initialOffsetX = { -it }) + fadeIn() + expandVertically(),
        exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut() + shrinkVertically()
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("• $texto", modifier = Modifier.weight(1f))
                TextButton(onClick = onBorrarClick) {
                    Text("Borrar", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
