package cl.DSY1105.misnotasapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember

import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

import cl.DSY1105.misnotasapp.views.LoginScreen
import cl.DSY1105.misnotasapp.views.NotasScreen
import cl.DSY1105.misnotasapp.views.RegistroScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApp()
        }
    }
}

@Composable
fun MyApp(){
    val navController = rememberNavController()

    // "Base de datos" en memoria: email -> contraseña.
    // Se declara AQUÍ (y no dentro de cada pantalla) para que sobreviva
    // al navegar entre Registro y Login. Esto se llama "elevar el estado"
    // (state hoisting). Se pierde al cerrar la app: aún no hay persistencia.

    val usuarios = remember { mutableStateMapOf<String, String>() }
    NavHost(navController = navController, startDestination = "login"){
        composable("login") {
            LoginScreen(
                navController = navController,
                validarCredenciales = { email, password -> usuarios[email] == password }
            )
        }
        composable("registro") {
            RegistroScreen(
                navController = navController,
                existeUsuario = { email -> usuarios.containsKey(email) },
                onRegistrar = { email, password -> usuarios[email] = password }
            )
        }
        composable("notas") { NotasScreen() }
    }

}