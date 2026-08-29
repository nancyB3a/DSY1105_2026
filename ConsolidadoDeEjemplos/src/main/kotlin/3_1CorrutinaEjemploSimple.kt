import kotlinx.coroutines.*

fun main() = runBlocking {
    launch {
        delay(1000L)
        println("¡Hola desde la corrutina!")
    }
    println("Hola desde el hilo principal")
}