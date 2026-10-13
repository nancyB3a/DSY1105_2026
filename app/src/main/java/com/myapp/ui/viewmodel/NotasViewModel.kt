package com.myapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myapp.data.repository.NotaRepository
import com.myapp.data.repository.SesionRepository
import com.myapp.data.room.NotaEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Tiempo que dura la animación de salida de una nota antes de borrarla de la base de datos. */
private const val DURACION_ANIMACION_SALIDA_MS = 350L

data class NotasUiState(
    val notas: List<NotaEntity> = emptyList(),   // viene de Room (siempre actualizada)
    val textoNota: String = "",                  // lo que se está escribiendo
    val notaPorBorrar: NotaEntity? = null,       // != null -> se muestra el AlertDialog de borrado
    val mostrarDialogoSalir: Boolean = false,    // true -> se muestra el AlertDialog de cerrar sesión
    val idsSaliendo: Set<Long> = emptySet()      // notas que están ejecutando su animación de salida
)

sealed interface NotasEvento {
    data class Mensaje(val texto: String) : NotasEvento   // para mostrar un Toast
    data object SesionCerrada : NotasEvento               // para volver al Login
}

class NotasViewModel(
    private val notaRepository: NotaRepository,
    private val sesionRepository: SesionRepository
) : ViewModel() {

    // Parte del estado que controla el propio ViewModel (texto, diálogos, animaciones)
    private val estadoLocal = MutableStateFlow(NotasUiState())

    // Notas del usuario con sesión iniciada, directo desde Room.
    // flatMapLatest: cada vez que cambia el usuario de la sesión, deja de escuchar las
    // notas del anterior y empieza a escuchar las del nuevo.
    // Si no hay sesión (email == null) no emite nada: así, al cerrar sesión, la lista no
    // "parpadea" vacía mientras la pantalla se está yendo.
    @OptIn(ExperimentalCoroutinesApi::class)
    private val notasDelUsuario: Flow<List<NotaEntity>> =
        sesionRepository.emailActual.flatMapLatest { email ->
            if (email == null) emptyFlow() else notaRepository.observarNotas(email)
        }

    // Estado FINAL que ve la pantalla = estado local + notas de Room, combinados.
    // stateIn convierte el Flow en un StateFlow; WhileSubscribed(5_000) mantiene la
    // consulta activa mientras la pantalla observe (y 5 s extra, para sobrevivir a rotaciones).
    val uiState: StateFlow<NotasUiState> =
        combine(estadoLocal, notasDelUsuario) { local, notas -> local.copy(notas = notas) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), NotasUiState())

    private val _eventos = Channel<NotasEvento>(Channel.BUFFERED)
    val eventos: Flow<NotasEvento> = _eventos.receiveAsFlow()

    // ---------- Nueva nota ----------
    fun onTextoNotaChange(valor: String) {
        estadoLocal.update { it.copy(textoNota = valor) }
    }

    fun guardarNota() {
        val texto = estadoLocal.value.textoNota
        val email = sesionRepository.emailActual.value ?: return

        if (texto.isBlank()) {
            enviar(NotasEvento.Mensaje("Escribe algo antes de guardar"))
            return
        }
        viewModelScope.launch {
            notaRepository.agregar(email, texto)   // Room emite la lista nueva sola
            estadoLocal.update { it.copy(textoNota = "") }
            _eventos.send(NotasEvento.Mensaje("Nota guardada"))
        }
    }

    // ---------- Borrar nota (con confirmación) ----------
    fun pedirBorrarNota(nota: NotaEntity) {
        estadoLocal.update { it.copy(notaPorBorrar = nota) }
    }

    fun cancelarBorrado() {
        estadoLocal.update { it.copy(notaPorBorrar = null) }
    }

    fun confirmarBorrado() {
        val nota = estadoLocal.value.notaPorBorrar ?: return
        // 1) cierra el diálogo y marca la nota como "saliendo" -> la pantalla anima su salida
        estadoLocal.update { it.copy(notaPorBorrar = null, idsSaliendo = it.idsSaliendo + nota.id) }
        viewModelScope.launch {
            delay(DURACION_ANIMACION_SALIDA_MS)     // 2) espera a que termine la animación
            notaRepository.eliminar(nota)           // 3) recién ahí la borra de Room
            estadoLocal.update { it.copy(idsSaliendo = it.idsSaliendo - nota.id) }
            _eventos.send(NotasEvento.Mensaje("Nota eliminada"))
        }
    }

    // ---------- Cerrar sesión (con confirmación) ----------
    fun pedirCerrarSesion() {
        estadoLocal.update { it.copy(mostrarDialogoSalir = true) }
    }

    fun cancelarCerrarSesion() {
        estadoLocal.update { it.copy(mostrarDialogoSalir = false) }
    }

    fun confirmarCerrarSesion() {
        estadoLocal.update { it.copy(mostrarDialogoSalir = false) }
        sesionRepository.cerrar()
        enviar(NotasEvento.SesionCerrada)   // la pantalla recoge este evento y navega al Login
    }

    private fun enviar(evento: NotasEvento) {
        viewModelScope.launch { _eventos.send(evento) }
    }
}
