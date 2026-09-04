package cl.DSY1105
import kotlinx.coroutines.delay

/**
 * Representa el perfil de cliente sin usar enum: el tipo válido se resuelve
 * a través del companion object fromString, que retorna null ante cualquier
 * valor fuera de los tres definidos (nuevo/frecuente/vip).
 */
data class Cliente(
    val tipo: String,
    val descuento: Double
) {
    companion object {
        fun fromString(tipo: String): Cliente? = when (tipo.trim().lowercase()) {
            "nuevo" -> Cliente("Nuevo", 0.0)
            "frecuente" -> Cliente("Frecuente", 0.15)
            "vip" -> Cliente("VIP", 0.40)
            else -> null
        }
    }
}

class FastGoDelivery(
    private val pedidosDisponibles: List<Pedido>
) {
    private val despacho = mutableListOf<Pedido>()

    fun mostrarPedidosDisponibles() {
        println("=== Pedidos Disponibles para Despacho ===")
        pedidosDisponibles.forEachIndexed { index, pedido ->
            println("${index + 1}.- ${pedido.detalle()}")
        }
    }

    // Formato exigido para el código de pedido: dos letras, seis dígitos (ej. DG123456).
    private val regexCodigoPedido = Regex("^[A-Z]{2}\\d{6}$")

    private fun codigoValido(codigo: String): Boolean = regexCodigoPedido.matches(codigo)

    fun agregarADespacho(indices: List<Int>): Boolean {
        try {
            for (i in indices) {
                if (i !in 1..pedidosDisponibles.size) {
                    throw IllegalArgumentException("Selección fuera del listado de pedidos")
                }
                val pedido = pedidosDisponibles[i - 1]
                // Validación de datos: un código con formato inválido no se despacha.
                if (!codigoValido(pedido.codigo)) {
                    throw IllegalArgumentException("Código de pedido inválido: '${pedido.codigo}' (formato esperado: dos letras y seis dígitos, ej. DG123456)")
                }
                despacho.add(pedido)
            }
        } catch (e: Exception) {
            val estado = EstadoDespacho.Error(e.message ?: "Error desconocido")
            mostrarEstado(estado)
            return false
        }
        return true
    }

    // Función de orden superior (sumOf) para calcular el subtotal del despacho.
    private fun calcularSubtotal(): Double = despacho.sumOf { it.calcularCosto() }

    // Más funciones de orden superior: filter y map, para consultas de negocio.
    fun pedidosAuto(): List<Pedido> = despacho.filter { it is PedidoAuto }
    fun codigosDespacho(): List<String> = despacho.map { it.codigo }

    /**
     * Simula el procesamiento asíncrono del despacho (comunicación con la
     * app del repartidor). Usa delay() para representar la espera, sin
     * bloquear el hilo que la invoca gracias a "suspend".
     */
    suspend fun procesarDespacho(cliente: Cliente) {
        var estado: EstadoDespacho = EstadoDespacho.Pendiente
        mostrarEstado(estado)

        estado = EstadoDespacho.EnProceso
        mostrarEstado(estado)
        delay(2500) // simula comunicación con la app del repartidor (asignación)
        delay(2500) // simula comunicación con la app del repartidor (confirmación)

        try {
            val subtotal = calcularSubtotal()
            val descuento = subtotal * cliente.descuento
            val montoConDescuento = subtotal - descuento
            val iva = montoConDescuento * 0.19
            val total = montoConDescuento + iva

            // Control de datos inválidos: un total <= 0 no debería ocurrir nunca.
            if (total <= 0.0) {
                throw IllegalStateException("El monto total del despacho no puede ser cero o negativo")
            }

            estado = EstadoDespacho.Entregado
            println("\n=== RESUMEN DE DESPACHO ===")
            despacho.forEach { println("- ${it.detalle()} -> ${formatoPesos(it.calcularCosto())}") }
            println("Subtotal: ${formatoPesos(subtotal)}")
            println("Descuento cliente ${cliente.tipo}: -${formatoPesos(descuento)}")
            println("IVA (19%): ${formatoPesos(iva)}")
            println("Total a cobrar: ${formatoPesos(total)}")
            mostrarEstado(estado)
        } catch (e: Exception) {
            estado = EstadoDespacho.Error(e.message ?: "Error desconocido")
            mostrarEstado(estado)
        }
    }

    /**
     * Gestiona el estado del despacho mediante una expresión "when" que
     * cubre los cuatro casos posibles de la sealed class EstadoDespacho.
     */
    private fun mostrarEstado(estado: EstadoDespacho) {
        when (estado) {
            is EstadoDespacho.Pendiente -> println("Estado: $estado - esperando inicio del despacho.")
            is EstadoDespacho.EnProceso -> println("Estado: $estado - comunicándose con la app del repartidor...")
            is EstadoDespacho.Entregado -> println("Estado: $estado - despacho completado con éxito.")
            is EstadoDespacho.Error -> println("Estado: $estado")
        }
    }
}

/** Formatea un monto como pesos chilenos, ej. 12345.0 -> "$12.345". */
fun formatoPesos(monto: Double): String = "$" + "%,.0f".format(monto).replace(",", ".")
