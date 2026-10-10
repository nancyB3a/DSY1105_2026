package cl.dsy1105.misnotasapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import cl.dsy1105.misnotasapp.data.AppState
import cl.dsy1105.misnotasapp.data.room.AppDatabase

import cl.dsy1105.misnotasapp.navigation.AppNavigation
import cl.dsy1105.misnotasapp.ui.views.LoginScreen
import cl.dsy1105.misnotasapp.ui.views.NotasScreen
import cl.dsy1105.misnotasapp.ui.views.RegistroScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val db = AppDatabase.getInstance(applicationContext)
        val appState = AppState(db)

        appState.cargarDatos() // cargar inicial
        setContent {
            MyApp(appState)
        }
    }
}

@Composable
fun MyApp(appState: AppState){
    val navController = rememberNavController()
    MaterialTheme() {
        AppNavigation(navController,appState)
    }


}

