class Taller {
    fun recibir(auto: Automovil) = println("Taller recibe: ${auto.marca} ${auto.modelo}")
}

fun main() {
    val taller = Taller()

    val ferrari = Deportivo("Ferrari", "488 GTB", 330)
        .apply { describir() }                              // configurar/usar el objeto
        .also { println("LOG -> Se creó: ${it.marca} ${it.modelo}") } // efecto secundario

    val mensaje = ferrari.let { "Listo para pista: ${it.marca} ${it.modelo}" }
    println(mensaje)

    taller.recibir(ferrari)
}