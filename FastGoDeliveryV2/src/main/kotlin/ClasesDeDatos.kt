package cl.DSY1105

import kotlin.random.Random

// Data classes para cliente y pedido
data class Cliente(val id: Int, val nombre: String, val tipoCliente: String) // "nuevo", "frecuente", "VIP"
data class Direccion(val calle: String, val numero: String, val ciudad: String)

data class Pedido(
    val codigo: String,
    val cliente: Cliente,
    val direccion: Direccion,
    val distanciaKm: Double,
    val fechaHora: String,
    val pedidoId: Int = Random.nextInt(1000, 9999),
    var repartidorAsignado: Repartidor? = null,
    var estado: EstadoPedido = EstadoPedido.Pendiente,
    val refrigerado: Boolean = false,
    val peakHour: Boolean = false
)
