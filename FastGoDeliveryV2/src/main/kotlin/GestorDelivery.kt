package cl.DSY1105
import kotlinx.coroutines.*

// Clase para gestionar la operación del sistema
class SistemaDelivery {
    private val repartidores = mutableListOf<Repartidor>()
    private val pedidos = mutableListOf<Pedido>()
    private val entregados = mutableListOf<Pedido>()
    private var totalRecaudado = 0.0

    init {
        // Crear 8 repartidores de ejemplo
        for (i in 1..3) repartidores.add(RepartidorBicicleta(i, "Bici $i"))
        for (i in 4..5) repartidores.add(RepartidorMoto(i, "Moto $i", peakHour = i % 2 == 0))
        for (i in 6..8) repartidores.add(RepartidorAuto(i, "Auto $i", refrigerado = i % 2 == 0))
    }

    // Validar código pedido
    private fun validarCodigo(codigo: String): Boolean {
        return codigo.matches(Regex("^[A-Z]{2}\\d{6}$"))
    }

    // Registrar pedido
    fun registrarPedido(pedido: Pedido) {
        if (!validarCodigo(pedido.codigo)) {
            println("Código inválido: ${pedido.codigo}")
            return
        }
        if (pedido.distanciaKm <= 0) {
            println("La distancia debe ser mayor a cero.")
            return
        }
        pedidos.add(pedido)
        println("Pedido registrado: ${pedido.codigo}")
    }

    // Buscar repartidor disponible
    private fun buscarRepartidorDisponible(): Repartidor? {
        return repartidores.find { it.estado is EstadoRepartidor.Disponible }
    }

    // Asignar pedido a repartidor (simulación asíncrona)
    suspend fun asignarPedido(pedido: Pedido) {
        val repartidor = buscarRepartidorDisponible()
        if (repartidor == null) {
            println("No hay repartidores disponibles.")
            return
        }

        // Cambiar estado a procesando
        repartidor.estado = EstadoRepartidor.Procesando
        println("Asignando pedido ${pedido.codigo} a ${repartidor.nombre}...")

        delay(2500L) // simula comunicación

        // Asignar pedido
        repartidor.estado = EstadoRepartidor.Ocupado
        repartidor.pedidoActual = pedido
        pedido.repartidorAsignado = repartidor
        pedido.estado = EstadoPedido.EnCamino

        println("Pedido ${pedido.codigo} asignado a ${repartidor.nombre}.")
    }

    // Confirmar entrega (asíncrono)
    suspend fun confirmarEntrega(codigoPedido: String) {
        val pedido = pedidos.find { it.codigo == codigoPedido && it.estado != EstadoPedido.Entregado }
        if (pedido == null) {
            println("Pedido no encontrado o ya entregado: $codigoPedido")
            return
        }
        val repartidor = pedido.repartidorAsignado
        if (repartidor == null || repartidor.estado != EstadoRepartidor.Ocupado) {
            println("Repartidor no disponible para este pedido.")
            return
        }

        // Estado en proceso de entrega
        repartidor.estado = EstadoRepartidor.Procesando
        println("Confirmando entrega de ${pedido.codigo}...")

        delay(5000L) // simula proceso de entrega

        // Actualizar estados
        pedido.estado = EstadoPedido.Entregado
        repartidor.estado = EstadoRepartidor.Disponible
        repartidor.pedidoActual = null

        // calcular tarifa
        val monto = calcularTarifa(pedido)
        if (monto <= 0) {
            println("Error en cálculo de tarifa.")
            return
        }
        totalRecaudado += monto
        entregados.add(pedido)
        pedidos.remove(pedido)

        println("Pedido ${pedido.codigo} entregado. Monto cobrado: \$${"%.2f".format(monto)}")
    }

    // Calcular tarifa según reglas
    private fun calcularTarifa(pedido: Pedido): Double {
        val repartidor = pedido.repartidorAsignado ?: return 0.0
        val km = pedido.distanciaKm
        var tarifa = 0.0
        when (repartidor) {
            is RepartidorBicicleta -> {
                tarifa = if (km < 1) 500.0 else 300.0 * km
            }
            is RepartidorMoto -> {
                tarifa = 500.0 * km
                if (pedido.peakHour) tarifa *= 1.25
            }
            is RepartidorAuto -> {
                tarifa = 700.0 * km
                if (repartidor.refrigerado) tarifa *= 1.20
            }
        }
        // IVA 19%
        tarifa *= 1.19
        // Descuento VIP
        if (pedido.cliente.tipoCliente == "VIP") {
            tarifa *= 0.60
        }
        return tarifa
    }

    // Consultas
    fun reporteTurno() {
        println("----- Reporte de Turno -----")
        println("Pedidos entregados: ${entregados.size}")
        var totalIngresos = 0.0
        val ingresosPorRepartidor = mutableMapOf<String, Double>()
        entregados.forEach {
            val monto = calcularTarifa(it)
            totalIngresos += monto
            val tipo = it.repartidorAsignado?.tipoVehiculo ?: "Desconocido"
            ingresosPorRepartidor[tipo] = (ingresosPorRepartidor[tipo] ?: 0.0) + monto
        }
        println("Total recaudado: \$${"%.2f".format(totalRecaudado)}")
        println("Ingreso promedio por pedido: \$${"%.2f".format(if (entregados.isNotEmpty()) totalRecaudado / entregados.size else 0.0)}")
        val maxIngreso = ingresosPorRepartidor.maxByOrNull { it.value }
        println("Repartidor que más ingresos generó: ${maxIngreso?.key} con \$${"%.2f".format(maxIngreso?.value ?: 0.0)}")
        val disponibles = repartidores.count { it.estado is EstadoRepartidor.Disponible }
        println("Repartidores disponibles al cierre: $disponibles")
        println("Pedidos entregados: ${entregados.joinToString { it.codigo }}")
        println("----------------------------")
    }
}
