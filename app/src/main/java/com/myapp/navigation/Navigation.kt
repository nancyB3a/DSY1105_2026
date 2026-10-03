package com.myapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.myapp.data.AppState
import com.myapp.ui.views.*

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