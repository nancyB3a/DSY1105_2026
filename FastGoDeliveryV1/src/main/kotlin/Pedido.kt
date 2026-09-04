package cl.DSY1105

/* Acá creo las clases relacionadas al PEDIDO */
open class Pedido( // constructor PRIMARIO
    val codigo: String,
    val distanciaKm: Double
) {
    open fun calcularCosto(): Double = distanciaKm * 400.0 // tarifa genérica, cada hijo la sobrescribe
    open fun detalle(): String = "$codigo - ${distanciaKm} km"
}

class PedidoBicicleta(
    codigo: String,
    distanciaKm: Double
) : Pedido(codigo, distanciaKm) {

    override fun calcularCosto(): Double {
        // Monto mínimo fijo para trayectos muy cortos.
        if (distanciaKm < 1.0) return 500.0
        return distanciaKm * 300.0
    }

    override fun detalle(): String = "${super.detalle()} - Bicicleta"
}

class PedidoMoto(
    codigo: String,
    distanciaKm: Double,
    val horarioPeak: Boolean
) : Pedido(codigo, distanciaKm) {

    override fun calcularCosto(): Double {
        var costo = distanciaKm * 500.0
        if (horarioPeak) costo *= 1.25 // recargo del 25% en horario peak
        return costo
    }

    override fun detalle(): String {
        val extra = if (horarioPeak) " (horario peak)" else ""
        return "${super.detalle()} - Moto$extra"
    }
}

class PedidoAuto(
    codigo: String,
    distanciaKm: Double,
    val refrigerado: Boolean
) : Pedido(codigo, distanciaKm) {

    override fun calcularCosto(): Double {
        var costo = distanciaKm * 700.0
        if (refrigerado) costo *= 1.20 // recargo del 20% por cadena de frío
        return costo
    }

    override fun detalle(): String {
        val extra = if (refrigerado) " refrigerado" else ""
        return "${super.detalle()} - Auto$extra"
    }
}
