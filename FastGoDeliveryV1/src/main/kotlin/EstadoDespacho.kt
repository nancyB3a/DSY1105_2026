package cl.DSY1105

/**
 * Estados posibles de un despacho asíncrono. Se usa sealed class (y no un
 * enum) porque el estado Error necesita llevar un dato propio (el mensaje),
 * y porque una sealed class obliga a cubrir todos los casos en cualquier
 * "when" que dependa del estado (ver FastGoDelivery.mostrarEstado).
 */
sealed class EstadoDespacho {
    object Pendiente : EstadoDespacho() {
        override fun toString() = "Pendiente"
    }
    object EnProceso : EstadoDespacho() {
        override fun toString() = "En Proceso"
    }
    object Entregado : EstadoDespacho() {
        override fun toString() = "Entregado"
    }
    data class Error(val mensaje: String) : EstadoDespacho() {
        override fun toString() = "Error: $mensaje"
    }
}