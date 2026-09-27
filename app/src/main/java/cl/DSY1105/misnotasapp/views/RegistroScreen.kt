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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import cl.DSY1105.misnotasapp.utils.esEmailValido

//creo una función para dibujar mi pantalla de Login
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroScreen(
    navController: NavController,
    existeUsuario: (String) -> Boolean,
    onRegistrar: (String, String) -> Unit
    ){
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Registro de Usuario") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email")},
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp)) //Para dibujar espacio entre controles
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña")},
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))
            //verifico si hay algún error
            if (error.isNotEmpty()){
                Text(error, color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(8.dp))
            }
            Button(
                onClick = { //volver a Login
                    if (email.isBlank() || password.isBlank()){
                        error = "Debe Ingresar Usuario y Contraseña"
                    }else if (!esEmailValido(email)){
                        error = "El formato de email NO es válido"
                    }else if(password.length < 3){
                        error = "La contraseña debe tener al menos 3 caracteres."
                    }else if(existeUsuario(email)){
                        error = "Ese email ya está registrado."
                    }else{
                        error = ""
                        onRegistrar(email, password) // guarda en el estado de MyApp
                        // Volver al Login que ya está en la pila (no crear uno nuevo)
                        navController.popBackStack()
                    }
                },
                modifier =  Modifier.fillMaxWidth()
            ) {
                Text("Registrarse")
            }
        }
    }
}
