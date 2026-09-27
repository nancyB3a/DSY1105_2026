package cl.DSY1105.misnotasapp.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotasScreen(){
    Scaffold(
        topBar = { TopAppBar(title = { Text("Notas") }) }
    ) { padding ->

        var nota by remember { mutableStateOf("") }
        var notaList = remember { mutableListOf<String>() }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            OutlinedTextField(
                value = nota,
                onValueChange = { nota = it },
                label = { Text("Escribe una nota")},
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp)) //Para dibujar espacio entre controles

            Button(
                onClick = {
                    if (nota.isNotBlank()){
                        notaList.add(nota)
                        nota = ""
                    }
                }) {
                Text("Guardar Nota")
            }
            Spacer(Modifier.height(16.dp))
            Text("Notas Guardadas:")
            notaList.forEach { n ->
                Text("• $n")
            }
        }
    }
}