package com.myapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myapp.data.repository.SesionRepository
import com.myapp.data.repository.UsuarioRepository
import com.myapp.utils.MIN_PASSWORD
import com.myapp.utils.esEmailValido
import com.myapp.utils.esPasswordValida
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** ESTADO: lo que la pantalla necesita DIBUJAR. Es inmutable (data class + val). */
data class LoginUiState(
    val usuario: String = "",
    val password: String = "",
    val error: String = ""
) {
    val passwordValida: Boolean get() = esPasswordValida(password)
}

/**
 * EVENTOS: cosas que ocurren UNA sola vez (mostrar un Toast, navegar).
 * No son estado: si fueran estado, al rotar la pantalla se volverían a ejecutar.
 * Es una sealed interface, igual que las clases selladas de la Experiencia 1.
 */
sealed interface LoginEvento {
    data class LoginExitoso(val email: String) : LoginEvento
}

/**
 * VIEWMODEL: contiene la lógica de la pantalla y el estado. NO conoce Compose, ni Toast,
 * ni NavController: solo habla con los repositorios (Model) y expone estado + eventos (View).
 */
class LoginViewModel(
    private val usuarioRepository: UsuarioRepository,
    private val sesionRepository: SesionRepository
) : ViewModel() {

    // Estado: privado mutable, público de solo lectura (encapsulamiento)
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    // Eventos de una sola vez: la pantalla los recoge con LaunchedEffect
    private val _eventos = Channel<LoginEvento>(Channel.BUFFERED)
    val eventos: Flow<LoginEvento> = _eventos.receiveAsFlow()

    fun onUsuarioChange(valor: String) {
        _uiState.update { it.copy(usuario = valor) }
    }

    fun onPasswordChange(valor: String) {
        _uiState.update { it.copy(password = valor) }
    }

    fun ingresar() {
        val actual = _uiState.value

        val mensajeError: String? = when {
            actual.usuario.isBlank() || actual.password.isBlank() ->
                "Debe ingresar usuario y contraseña"
            !esEmailValido(actual.usuario) ->
                "El formato de email NO es válido"
            !actual.passwordValida ->
                "La contraseña debe tener al menos $MIN_PASSWORD caracteres."
            else -> null
        }
        if (mensajeError != null) {
            _uiState.update { it.copy(error = mensajeError) }
            return
        }

        // La consulta a Room es suspend: se lanza en viewModelScope, que se cancela
        // sola cuando el ViewModel se destruye (no hay corrutinas "huérfanas").
        viewModelScope.launch {
            val email = actual.usuario.trim()
            if (usuarioRepository.autenticar(email, actual.password)) {
                sesionRepository.iniciar(email)
                _uiState.update { it.copy(error = "") }
                _eventos.send(LoginEvento.LoginExitoso(email))
            } else {
                _uiState.update { it.copy(error = "Usuario y/o Contraseña incorrectos") }
            }
        }
    }
}
