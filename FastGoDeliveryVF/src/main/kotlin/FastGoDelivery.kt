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

/** Repartidor de la flota (R2). "estado" es var porque cambia a lo largo del ciclo de vida de un pedido. */
data class Repartidor(
    val numero: Int,
    var estado: EstadoRepartidor = EstadoRepartidor.Disponible
)

/** Comprobante emitido al confirmarse una entrega, usado en el historial y el reporte de cierre (R4). */
data class Comprobante(
    val numero: Int,
    val pedido: Pedido,
    val monto: Double
)

class FastGoDelivery(cantidadRepartidores: Int = 8) {

    val repartidores: List<Repartidor> = (1..cantidadRepartidores).map { Repartidor(it) }

    private val historial = mutableListOf<Comprobante>()
    private var siguienteNumeroComprobante = 1
    private var recaudacionTotal = 0.0
    private val recaudacionPorTipo = mutableMapOf<String, Double>()

    // Formato exigido para el código de pedido: dos letras, seis dígitos (ej. DG123456).
    //Regex() es MUY útil, ya que permite validaciones de formato
    private val regexCodigoPedido = Regex("^[A-Z]{2}\\d{6}$")
    private fun codigoValido(codigo: String): Boolean = regexCodigoPedido.matches(codigo)

    /**
     * Operación de asignación (R5): busca el primer repartidor disponible,
     * lo deja "Procesando" mientras "espera confirmación de la app" (delay),
     * y luego lo pasa a "Ocupado" con el pedido asignado.
     */
    suspend fun asignarPedido(pedido: Pedido): Boolean {
        try {
            if (!codigoValido(pedido.codigo)) {
                throw IllegalArgumentException("Código de pedido inválido: '${pedido.codigo}' (formato esperado: dos letras y seis dígitos, ej. DG123456)")
            }

            val libre = repartidores.firstOrNull { it.estado is EstadoRepartidor.Disponible }
                ?: throw IllegalStateException("Sin repartidores disponibles: no hay ningún repartidor libre en este momento")

            libre.estado = EstadoRepartidor.Procesando("Asignando pedido ${pedido.codigo}")
            mostrarEstado(libre)
            delay(2500) // simula comunicación con la app del repartidor

            libre.estado = EstadoRepartidor.Ocupado(pedido)
            mostrarEstado(libre)
            return true
        } catch (e: Exception) {
            println("[ERROR] ${e.message}")
            return false
        }
    }

    /**
     * Operación de entrega (R5): localiza al repartidor que tiene asignado el
     * pedido, lo deja "Procesando" mientras "confirma la entrega" (delay),
     * calcula el monto final, emite el comprobante y libera al repartidor.
     */
    suspend fun confirmarEntrega(codigoPedido: String): Boolean {
        try {
            val repartidor = repartidores.firstOrNull { r ->
                (r.estado as? EstadoRepartidor.Ocupado)?.pedido?.codigo == codigoPedido
            } ?: throw IllegalStateException("Pedido no encontrado: '$codigoPedido' no está asignado a ningún repartidor")

            val pedido = (repartidor.estado as EstadoRepartidor.Ocupado).pedido
            repartidor.estado = EstadoRepartidor.Procesando("Confirmando entrega de ${pedido.codigo}")
            mostrarEstado(repartidor)
            delay(5000) // simula comunicación con la app del repartidor

            val monto = calcularMontoPedido(pedido)
            if (monto <= 0.0) {
                // El repartidor igual se libera: el problema es de tarifa, no de ocupación.
                repartidor.estado = EstadoRepartidor.Disponible
                throw IllegalStateException("Resultado de tarifa inválido para el pedido '$codigoPedido' (monto = $monto)")
            }

            val comprobante = Comprobante(siguienteNumeroComprobante++, pedido, monto)
            historial.add(comprobante)
            recaudacionTotal += monto
            val tipo = tipoLegible(pedido)
            recaudacionPorTipo[tipo] = (recaudacionPorTipo[tipo] ?: 0.0) + monto

            repartidor.estado = EstadoRepartidor.Disponible
            println("[OK] Entrega confirmada - Comprobante N°${comprobante.numero} | ${pedido.detalle()} -> ${formatoPesos(monto)}")
            return true
        } catch (e: Exception) {
            println("[ERROR] ${e.message}")
            return false
        }
    }

    /** Costo final de un pedido aplicando el descuento de su propio cliente y el IVA (R3). */
    private fun calcularMontoPedido(pedido: Pedido): Double {
        val base = pedido.calcularCosto()
        val descuento = base * pedido.cliente.descuento
        val montoConDescuento = base - descuento
        val iva = montoConDescuento * 0.19
        return montoConDescuento + iva
    }

    private fun tipoLegible(pedido: Pedido): String = when (pedido) {
        is PedidoBicicleta -> "Bicicleta"
        is PedidoMoto -> "Moto"
        is PedidoAuto -> "Auto"
        else -> "Otro"
    }

    /** Gestiona el estado del repartidor mediante un "when" que cubre los 4 casos de EstadoRepartidor. */
    private fun mostrarEstado(repartidor: Repartidor) {
        when (val estado = repartidor.estado) {
            is EstadoRepartidor.Disponible -> println("Repartidor N°${repartidor.numero}: $estado")
            is EstadoRepartidor.Ocupado -> println("Repartidor N°${repartidor.numero}: $estado")
            is EstadoRepartidor.Procesando -> println("Repartidor N°${repartidor.numero}: $estado")
            is EstadoRepartidor.FueraDeServicio -> println("Repartidor N°${repartidor.numero}: $estado")
        }
    }

    // ---- R4: consultas de negocio ----
    fun repartidoresDisponibles(): Int = repartidores.count { it.estado is EstadoRepartidor.Disponible }
    fun pedidosVipDelHistorial(): List<Comprobante> = historial.filter { it.pedido.cliente.tipo == "VIP" }
    fun ingresoPromedio(): Double = if (historial.isEmpty()) 0.0 else recaudacionTotal / historial.size
    fun codigosEntregados(): List<String> = historial.map { it.pedido.codigo }
    fun pedidoMayorDistancia(): Comprobante? = historial.maxByOrNull { it.pedido.distanciaKm }
    fun tipoConMasIngresos(): String? = recaudacionPorTipo.maxByOrNull { it.value }?.key

    fun imprimirReporteCierre() {
        println("\n=== REPORTE DE CIERRE DE TURNO - FastGo Delivery ===")
        historial.forEach { c -> println("Comprobante N°${c.numero} | ${c.pedido.detalle()} | ${formatoPesos(c.monto)}") }
        println("-----------------------------------------------------")
        println("Total recaudado: ${formatoPesos(recaudacionTotal)}")
        println("Pedidos entregados: ${historial.size}")
        println("Ingreso promedio: ${formatoPesos(ingresoPromedio())}")
        println("Tipo de repartidor con más ingresos: ${tipoConMasIngresos() ?: "N/A"}")
        println("Repartidores disponibles al cierre: ${repartidoresDisponibles()}")
    }
}

/** Formatea un monto como pesos chilenos, ej. 12345.0 -> "$12.345". */
fun formatoPesos(monto: Double): String = "$" + "%,.0f".format(monto).replace(",", ".")
