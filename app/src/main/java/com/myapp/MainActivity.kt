package com.myapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.myapp.data.AppState
import com.myapp.data.DataStoreManager
import com.myapp.navigation.AppNavigation
import com.myapp.ui.theme.MyAppTheme
import com.myapp.ui.views.LoginScreen
import com.myapp.ui.views.NotasScreen
import com.myapp.ui.views.RegistroScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val dataStore = DataStoreManager(applicationContext)
        val appState = AppState(dataStore)

        appState.cargarDatos() //carga inicial
        setContent {
            MyApp(appState)
        }
    }
}

@Composable
fun MyApp(appState: AppState){
    val navController = rememberNavController()
    MaterialTheme {
        AppNavigation(navController, appState)
    }
}
