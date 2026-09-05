package cl.DSY1105

/**
 * Estados posibles de un repartidor (R2). Se usa sealed class porque dos de
 * los estados necesitan un dato propio (Ocupado guarda el pedido asignado;
 * Procesando y FueraDeServicio guardan el motivo), y porque el compilador
 * obliga a cubrir los cuatro casos en cualquier "when" sobre el estado
 * (ver FastGoDelivery.mostrarEstado), evitando asumir que un repartidor
 * siempre estará disponible u ocupado.
 */
sealed class EstadoRepartidor {
    object Disponible : EstadoRepartidor() {
        override fun toString() = "Disponible"
    }
    data class Ocupado(val pedido: Pedido) : EstadoRepartidor() {
        override fun toString() = "Ocupado (pedido ${pedido.codigo})"
    }
    data class Procesando(val motivo: String) : EstadoRepartidor() {
        override fun toString() = "Procesando: $motivo"
    }
    data class FueraDeServicio(val motivo: String) : EstadoRepartidor() {
        override fun toString() = "Fuera de servicio: $motivo"
    }
}