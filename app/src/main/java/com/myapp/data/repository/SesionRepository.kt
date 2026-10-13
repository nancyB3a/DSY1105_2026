package com.myapp.data.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Guarda QUIÉN tiene la sesión iniciada. Lo comparten LoginViewModel (que la inicia)
 * y NotasViewModel (que la lee y la cierra), por eso es un repositorio aparte y
 * existe una sola instancia para toda la app (ver AppContainer).
 *
 * Es un StateFlow: NotasViewModel lo observa y cambia las notas que muestra
 * automáticamente si el usuario de la sesión cambia.
 *
 * Nota: vive en memoria, igual que antes. Si Android mata el proceso, hay que iniciar
 * sesión de nuevo (las notas y usuarios sí persisten, porque están en Room).
 */
class SesionRepository {
    private val _emailActual = MutableStateFlow<String?>(null)
    val emailActual: StateFlow<String?> = _emailActual.asStateFlow()

    fun iniciar(email: String) {
        _emailActual.value = email
    }

    fun cerrar() {
        _emailActual.value = null
    }
}
