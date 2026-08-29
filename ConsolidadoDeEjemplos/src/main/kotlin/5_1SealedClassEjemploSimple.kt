sealed class Estado {
    object Apagado : Estado()
    object Encendido : Estado()
    data class EnMovimiento(val velocidad: Int) : Estado()
}

fun mensaje(estado: Estado) = when (estado) {
    Estado.Apagado -> "Auto apagado"
    Estado.Encendido -> "Auto encendido"
    is Estado.EnMovimiento -> "Velocidad: ${estado.velocidad}"
    // no es necesario 'else' porque cubrimos todas las variantes
}

fun main() {
    println(mensaje(Estado.Apagado))
    println(mensaje(Estado.Encendido))
    println(mensaje(Estado.EnMovimiento(30)))
}