import kotlinx.coroutines.*

open class AutomovilV2(val marca: String, val modelo: String) {
    open suspend fun arrancar() {
        println("[$marca $modelo] Arrancando...")
        delay(4000) // simula trabajo no bloqueante
        println("[$marca $modelo] Motor encendido.")
    }
}

class DeportivoV2(marca: String, modelo: String, private val velocidadMaxima: Int) :
    AutomovilV2(marca, modelo) {

    suspend fun acelerarHastaObjetivo(objetivo: Int) {
        require(objetivo in 1..velocidadMaxima) { "Objetivo inválido" }
        println("Acelerando a $objetivo km/h...")
        delay(6000)
        println("¡Objetivo alcanzado!")
    }

    override suspend fun arrancar() {
        println("[$marca $modelo] Secuencia sport...")
        delay(2000)
        super.arrancar()
    }
}

fun main() = runBlocking {
    val ferrari = DeportivoV2("Ferrari", "488 GTB", 330)

    // Structured concurrency: todo vive dentro de runBlocking
    launch { ferrari.arrancar() }.join()
    launch { ferrari.acelerarHastaObjetivo(330) }.join()
}