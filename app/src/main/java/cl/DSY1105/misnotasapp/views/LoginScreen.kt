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
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import cl.DSY1105.misnotasapp.utils.esEmailValido

//creo una función para dibujar mi pantalla de Login
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavController,
    // La pantalla no sabe DÓNDE están los usuarios, solo recibe una función para validarlos
    validarCredenciales: (String, String) -> Boolean){
    var usuario by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Login") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            OutlinedTextField(
                value = usuario,
                onValueChange = { usuario = it },
                label = { Text("Usuario")},
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp)) //Para dibujar espacio entre controles
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña")},
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),//oculta la contraseña
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))
            //verifico si hay algún error
            if (error.isNotEmpty()){
                Text(error, color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(8.dp))
            }
            Button(
                onClick = { //verificar reglas de negocio implícitas
                    if (usuario.isBlank() || password.isBlank()){
                        error = "Debe Ingresar Usuario y Contraseña"
                    }else if (!esEmailValido(usuario)){
                        error = "El formato de email NO es válido"
                    }else if(password.length < 3){
                        error = "La contraseña debe tener al menos 3 caracteres."
                    }else if(!validarCredenciales(usuario.trim(), password)){
                        error = "Usuario o contraseña incorrectos."
                    }else{
                        error = ""
                        // Ir a Notas y sacar Login de la pila, para que "atrás" no vuelva al Login
                        navController.navigate("notas") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                },
                modifier =  Modifier.fillMaxWidth()
            ) {
                Text("Ingresar")
            }
            TextButton(onClick = {/*navegar hacia pantalla de registro*/
                                    navController.navigate("registro")
            }) {
                Text("¿No tienes cuenta?...Regístrate Aquí!!")
            }
        }
    }
}

