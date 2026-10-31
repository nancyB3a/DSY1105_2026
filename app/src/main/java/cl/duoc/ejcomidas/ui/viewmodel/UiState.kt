package cl.duoc.ejcomidas.ui.viewmodel

/** Estados posibles de una pantalla que consume la API. */
sealed interface UiState<out T> {
    data object Cargando : UiState<Nothing>
    data class Exito<T>(val datos: T) : UiState<T>
    data class Error(val mensaje: String) : UiState<Nothing>
}

/** Traduce una excepción de red a un mensaje legible. */
fun Throwable.aMensaje(): String = when (this) {
    is java.net.UnknownHostException -> "Sin conexión a internet"
    is java.net.SocketTimeoutException -> "El servidor tardó demasiado en responder"
    is retrofit2.HttpException -> "Error del servidor (${code()})"
    else -> "Ocurrió un error inesperado"
}
