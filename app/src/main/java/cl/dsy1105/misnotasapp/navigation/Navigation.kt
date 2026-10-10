package cl.dsy1105.misnotasapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import cl.dsy1105.misnotasapp.data.AppState
import cl.dsy1105.misnotasapp.ui.views.LoginScreen
import cl.dsy1105.misnotasapp.ui.views.NotasScreen
import cl.dsy1105.misnotasapp.ui.views.RegistroScreen

@Composable
fun AppNavigation(navController: NavHostController, appState: AppState){
    NavHost(
        navController = navController, startDestination = "login",
    ){
        composable("login") { LoginScreen(navController,appState) }
        composable("registro") { RegistroScreen(navController,appState) }
        composable("notas") { NotasScreen(navController,appState) }
    }
}