package com.myapp.ui.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.myapp.data.AppState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotasScreen(navController: NavController, appState: AppState) {

    var nota by remember { mutableStateOf("") }

    // La usan tanto la flecha "atrás" como el botón "Salir": como el login ya
    // se sacó de la pila al llegar aquí (popUpTo inclusive), "volver atrás"
    // desde Notas solo tiene sentido si equivale a cerrar sesión.
    fun cerrarSesion() {
        appState.logout()
        navController.navigate("login") {
            popUpTo("login") { inclusive = true }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    Text("Notas")
                },
                /*navigationIcon = {
                    IconButton(onClick = { cerrarSesion() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Cerrar sesión"
                        )
                    }
                },*/
                actions = {
                    TextButton(onClick = { cerrarSesion() }) {
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
                value = nota,
                onValueChange = { nota = it },
                label = { Text("Escribe una nota") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            Button(onClick = {
                if (nota.isNotBlank()) {
                    appState.agregarNota(nota)
                    nota = ""
                }
            }) {
                Text("Guardar Nota")
            }
            Spacer(Modifier.height(16.dp))

            Text("Notas guardadas:")
            LazyColumn {
                itemsIndexed(appState.obtenerNotas()) { index, n ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("• $n")
                        TextButton(onClick = { appState.borrarNota(index) }) {
                            Text("Borrar", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}
