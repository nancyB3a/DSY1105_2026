package com.myapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.myapp.ui.views.LoginScreen
import com.myapp.ui.views.NotasScreen
import com.myapp.ui.views.RegistroScreen

/**
 * Único lugar que conoce el NavController. Las pantallas no lo reciben: solo reciben
 * lambdas ("qué hacer cuando termino"), lo que las deja independientes de la navegación
 * y fáciles de reutilizar o probar.
 *
 * Cada pantalla crea su propio ViewModel (viewModel(factory=...) en su parámetro por
 * defecto); queda atado a la entrada de la pila de navegación, por lo que sobrevive a
 * rotaciones y se destruye cuando la pantalla sale de la pila.
 */
@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            LoginScreen(
                onLoginExitoso = {
                    // Saca "login" de la pila para que "atrás" no vuelva a una sesión ya iniciada
                    navController.navigate("notas") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onIrARegistro = { navController.navigate("registro") }
            )
        }
        composable("registro") {
            RegistroScreen(
                onVolver = { navController.popBackStack() }
            )
        }
        composable("notas") {
            NotasScreen(
                onSesionCerrada = {
                    // Al cerrar sesión se saca "notas" de la pila (ahí ya no existe "login")
                    navController.navigate("login") {
                        popUpTo("notas") { inclusive = true }
                    }
                }
            )
        }
    }
}
