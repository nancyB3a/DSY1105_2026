data class Mantenimiento(
    val fecha: String,
    val descripcion: String,
    val costo: Double
)

fun main() {
    val registro = Mantenimiento("2025-08-24", "Cambio de aceite", 150.0)
    println(registro)

    val registroConDescuento = registro.copy(costo = 120.0)
    println(registroConDescuento)
}