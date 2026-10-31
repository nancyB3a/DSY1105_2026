package cl.duoc.ejcomidas.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalPizza
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import cl.duoc.ejcomidas.ui.screens.CategoriasScreen
import cl.duoc.ejcomidas.ui.screens.FavoritosScreen
import cl.duoc.ejcomidas.ui.screens.PorCategoriaScreen
import cl.duoc.ejcomidas.ui.viewmodel.FavoritosViewModel

/** Las 3 tabs de la app (antes tabs.page.html). */
enum class Pantalla(val ruta: String, val titulo: String, val etiqueta: String, val icono: ImageVector) {
    Tipos("tipos", "Que Cocino App", "Tipos", Icons.Filled.Bookmarks),
    PorCategoria("por_categoria", "Categorías", "Por Categoría", Icons.Filled.LocalPizza),
    Favoritas("favoritas", "Favoritos", "Favoritas", Icons.Filled.Favorite)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val destinoActual = backStackEntry?.destination
    val pantallaActual = Pantalla.entries.firstOrNull { it.ruta == destinoActual?.route } ?: Pantalla.Tipos

    // Creado aquí (dueño = Activity) para que las tabs 2 y 3 compartan los favoritos
    val favoritosViewModel: FavoritosViewModel = viewModel(factory = FavoritosViewModel.Factory)

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(Unit) {
        favoritosViewModel.mensajes.collect { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(pantallaActual.titulo) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            NavigationBar {
                Pantalla.entries.forEach { pantalla ->
                    NavigationBarItem(
                        selected = destinoActual?.hierarchy?.any { it.route == pantalla.ruta } == true,
                        onClick = {
                            navController.navigate(pantalla.ruta) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(pantalla.icono, contentDescription = null) },
                        label = { Text(pantalla.etiqueta) }
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Pantalla.Tipos.ruta,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Pantalla.Tipos.ruta) { CategoriasScreen() }
            composable(Pantalla.PorCategoria.ruta) { PorCategoriaScreen(favoritosViewModel) }
            composable(Pantalla.Favoritas.ruta) { FavoritosScreen(favoritosViewModel) }
        }
    }
}
