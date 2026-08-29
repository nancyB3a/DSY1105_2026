open class Automovil(//Constructor Primario
    val marca: String,
    val modelo: String) {

    open fun describir() = println("Automóvil $marca $modelo.")
}

class Deportivo(//Constructor Primario
    marca: String,
    modelo: String,
    private val velocidadMaxima: Int) :
    Automovil(marca, modelo) {

    override fun describir() =
        println("Deportivo $marca $modelo (máx: $velocidadMaxima km/h).")
}

fun main() {
    val a = Automovil("Toyota", "Corolla")
    val b: Automovil = Deportivo("Ferrari", "488 GTB", 330) // polimorfismo

    a.describir()  // -> Automóvil Toyota Corolla.
    b.describir()  // -> Deportivo Ferrari 488 GTB (máx: 330 km/h).
}