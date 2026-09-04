package cl.DSY1105


// Clase base y clases hijas para repartidores
open class Repartidor(
    val id: Int,
    val nombre: String,
    val tipoVehiculo: String,
    var estado: EstadoRepartidor = EstadoRepartidor.Disponible,
    var pedidoActual: Pedido? = null
)

class RepartidorAuto(id: Int, nombre: String, val refrigerado: Boolean = false) :
    Repartidor(id, nombre, "Auto")

class RepartidorMoto(id: Int, nombre: String, val peakHour: Boolean = false) :
    Repartidor(id, nombre, "Moto")

class RepartidorBicicleta(id: Int, nombre: String) :
    Repartidor(id, nombre, "Bicicleta")