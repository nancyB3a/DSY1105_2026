data class Persona(val nombre: String, val edad: Int)

fun main() {
    val nancy = Persona("Nancy", 30)
    println(nancy) // Persona(nombre=Nancy, edad=30)

    val copia = nancy.copy(edad = 31)
    println(copia) // Persona(nombre=Nancy, edad=31)

    val (nombre, edad) = nancy
    println("Nombre: $nombre, Edad: $edad")
}
