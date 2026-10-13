package com.myapp.ui.views

import android.widget.Toast
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.myapp.ui.components.CampoPassword
import com.myapp.ui.viewmodel.AppViewModelProvider
import com.myapp.ui.viewmodel.LoginEvento
import com.myapp.ui.viewmodel.LoginViewModel

/**
 * VIEW (MVVM): solo DIBUJA el estado y avisa al ViewModel qué hizo el usuario.
 * No tiene lógica de negocio, no conoce Room, repositorios ni NavController:
 * para navegar recibe lambdas (onLoginExitoso, onIrARegistro) que decide quien la usa.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginExitoso: () -> Unit,
    onIrARegistro: () -> Unit,
    viewModel: LoginViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    // STATEFLOW -> STATE: cada valor nuevo del ViewModel recompone esta pantalla.
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // EVENTOS de una sola vez (Toast + navegar). LaunchedEffect(Unit) = "colecta mientras
    // esta pantalla esté en pantalla"; se ejecuta una vez, no en cada recomposición.
    LaunchedEffect(Unit) {
        viewModel.eventos.collect { evento ->
            when (evento) {
                is LoginEvento.LoginExitoso -> {
                    Toast.makeText(context, "¡Bienvenido/a, ${evento.email}!", Toast.LENGTH_SHORT).show()
                    onLoginExitoso()
                }
            }
        }
    }

    // ANIMACIÓN INFINITA del ícono
    val transicion = rememberInfiniteTransition(label = "logo")
    val escala by transicion.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "escala"
    )
    val rotacion by transicion.animateFloat(
        initialValue = -15f,
        targetValue = 15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rotacion"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = { Text("Login") }
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Filled.Edit,
                contentDescription = "Logo de Mis Notas",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(72.dp)
                    .graphicsLayer {
                        scaleX = escala
                        scaleY = escala
                        rotationZ = rotacion
                    }
            )
            Spacer(Modifier.height(24.dp))
            OutlinedTextField(
                value = uiState.usuario,
                onValueChange = viewModel::onUsuarioChange,
                label = { Text("Usuario") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            CampoPassword(
                value = uiState.password,
                onValueChange = viewModel::onPasswordChange,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))

            if (uiState.error.isNotEmpty()) {
                Text(uiState.error, color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(8.dp))
            }
            Button(
                onClick = viewModel::ingresar,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Ingresar")
            }

            TextButton(onClick = onIrARegistro) {
                Text("¿No tienes cuenta? Registrarse")
            }
        }
    }
}
