sealed class Resultado<out T> {
    data class Exito<T>(val data: T) : Resultado<T>()
    data class Error(val mensaje: String) : Resultado<Nothing>()
}

fun <T> procesar(r: Resultado<T>) = when (r) {
    is Resultado.Exito -> println("OK: ${r.data}")
    is Resultado.Error -> println("Fallo: ${r.mensaje}")
}

fun main() {
    procesar(Resultado.Exito(200))
    procesar(Resultado.Error("Fallo de exito"))
}