package cl.dsy1105.misnotasapp.ui.views

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
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import cl.dsy1105.misnotasapp.data.AppState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotasScreen(navController: NavHostController,
                appState: AppState){

    fun cerrarSesion(){
        appState.logout()
        navController.navigate("login"){
            popUpTo("login") { inclusive = true }
        }
    }

    Scaffold(
        topBar = { TopAppBar(
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                titleContentColor = MaterialTheme.colorScheme.primary,
            ),
            title = { Text("Mis Notas") },
            actions = {
                TextButton (onClick = { cerrarSesion()}) {
                    Text("Salir", color = MaterialTheme.colorScheme.primary)
                }
            }) }
    ) {
        padding ->
        var nota by remember { mutableStateOf("") }
        var notasList = remember { mutableListOf<String>() }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            OutlinedTextField(
                value = nota,
                onValueChange = {nota = it},
                label = {Text("Escribe una nota")},
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp)) //Para dibujar espacios entre un control y otro

            //dibujo un botón
            Button(
                onClick = { //verifico reglas de negocio
                    if (nota.isNotBlank()){
                        appState.agregarNota(nota)
                        nota = ""
                    }
                },
            ) {
                Text("Guardar Nota")
            }
            Spacer(Modifier.height(16.dp))
            Text("Notas Guardadas: ")
            LazyColumn() {
                itemsIndexed(appState.obtenerNotas()) { index, n ->
                    Row (
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

