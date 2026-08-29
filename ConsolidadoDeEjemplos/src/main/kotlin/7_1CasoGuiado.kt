import kotlinx.coroutines.*

// Clase base
open class Vehiculo(val marca: String, val modelo: String) {    open fun presentarse() {
    println("Soy un vehículo de marca $marca, modelo $modelo.")
}
}

// Clase derivada
class Auto(marca: String, modelo: String, val tipo: String) : Vehiculo(marca, modelo) {
    override fun presentarse() {
        println("Soy un auto $tipo de marca $marca, modelo $modelo.")
    }
}

// Data class
data class Conductor(val nombre: String, val licencia: String)

// Sealed class (estados posibles del vehículo)
sealed class EstadoVehiculo {
    object Disponible : EstadoVehiculo()
    object EnUso : EstadoVehiculo()
    data class EnMantenimiento(val motivo: String) : EstadoVehiculo()

    override fun toString(): String =
        when (this) {
            Disponible -> "Disponible"
            EnUso -> "En uso"
            is EnMantenimiento -> "En mantenimiento: $motivo"
        }
}
// Main con corrutinas y funciones de ámbito
fun main() = runBlocking {
    val auto = Auto("Toyota", "Corolla", "Sedán").apply {
        presentarse()
    }

    val conductor = Conductor("Juan", "B12345").also {
        println("Conductor asignado: $it")
    }

    var estado: EstadoVehiculo = EstadoVehiculo.Disponible
    println("➡️ Estado inicial: $estado")

    // Corrutina que simula el uso del auto
    launch {
        estado = EstadoVehiculo.EnUso
        println("🚗 Estado cambiado a: $estado")
        delay(2000) // Simula que el auto está en uso
        estado = EstadoVehiculo.EnMantenimiento("Cambio de aceite")
        println("🛠️ Estado cambiado a: $estado")
        delay(2000) // Simula tiempo en taller
        estado = EstadoVehiculo.Disponible
        println("✅ Estado cambiado a: $estado")
    }

    println("⌛ Mientras tanto, el sistema sigue respondiendo...")
    delay(5000) // Esperamos que todas las corrutinas terminen
    println("🏁 Simulación finalizada")
}