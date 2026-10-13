package com.myapp.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myapp.data.repository.UsuarioRepository
import com.myapp.utils.MIN_PASSWORD
import com.myapp.utils.esEmailValido
import com.myapp.utils.esPasswordValida
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

data class RegistroUiState(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val error: String = ""
) {
    val passwordValida: Boolean get() = esPasswordValida(password)
    val passwordsCoinciden: Boolean get() = password == confirmPassword
}

sealed interface RegistroEvento {
    data object RegistroExitoso : RegistroEvento
}

/**
 * Mismo patrón que LoginViewModel, pero aquí el estado se guarda con `State` de Compose
 * (mutableStateOf) en vez de StateFlow. Las dos formas son válidas en MVVM:
 *  - StateFlow  -> independiente de Compose (se puede probar y reutilizar en cualquier capa)
 *  - State      -> más corto, pero atado a Compose
 * La pantalla lee `uiState` directamente, sin collectAsState.
 */
class RegistroViewModel(
    private val usuarioRepository: UsuarioRepository
) : ViewModel() {

    var uiState by mutableStateOf(RegistroUiState())
        private set // la pantalla puede LEER, pero solo el ViewModel puede modificar

    private val _eventos = Channel<RegistroEvento>(Channel.BUFFERED)
    val eventos: Flow<RegistroEvento> = _eventos.receiveAsFlow()

    fun onEmailChange(valor: String) {
        uiState = uiState.copy(email = valor)
    }

    fun onPasswordChange(valor: String) {
        uiState = uiState.copy(password = valor)
    }

    fun onConfirmPasswordChange(valor: String) {
        uiState = uiState.copy(confirmPassword = valor)
    }

    fun registrar() {
        val actual = uiState

        val mensajeError: String? = when {
            actual.email.isBlank() || actual.password.isBlank() || actual.confirmPassword.isBlank() ->
                "Todos los campos son obligatorios"
            !esEmailValido(actual.email) ->
                "El formato de email NO es válido"
            !actual.passwordValida ->
                "La contraseña debe tener al menos $MIN_PASSWORD caracteres"
            !actual.passwordsCoinciden ->
                "Las contraseñas no coinciden"
            else -> null
        }
        if (mensajeError != null) {
            uiState = actual.copy(error = mensajeError)
            return
        }

        viewModelScope.launch {
            if (usuarioRepository.registrar(actual.email.trim(), actual.password)) {
                uiState = uiState.copy(error = "")
                _eventos.send(RegistroEvento.RegistroExitoso)
            } else {
                uiState = uiState.copy(error = "El usuario ya existe")
            }
        }
    }
}
