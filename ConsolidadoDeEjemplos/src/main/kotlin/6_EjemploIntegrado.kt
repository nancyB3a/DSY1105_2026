import kotlinx.coroutines.*

// --- Dominio ---
open class AutomovilVF(val marca: String, val modelo: String) {
    open fun describir() = println("Automóvil $marca $modelo.")

    open suspend fun arrancar() {
        println("[$marca $modelo] Arrancando...")
        delay(300)
        println("[$marca $modelo] Motor encendido.")
    }
}

class DeportivoVF(marca: String, modelo: String, private val velocidadMaxima: Int) :
    AutomovilVF(marca, modelo) {

    override fun describir() =
        println("Deportivo $marca $modelo (máx: $velocidadMaxima km/h).")

    override suspend fun arrancar() {
        println("[$marca $modelo] Modo sport activado.")
        delay(200)
        super.arrancar()
    }

    suspend fun acelerarHasta(vel: Int) {
        require(vel in 1..velocidadMaxima) { "Velocidad objetivo fuera de rango" }
        println("[$marca $modelo] Acelerando a $vel km/h...")
        delay(500)
        println("[$marca $modelo] Velocidad estabilizada en $vel km/h.")
    }
}

// --- Estados (sealed) ---
sealed class EstadoAutomovil {
    data object Apagado : EstadoAutomovil()
    data object Encendido : EstadoAutomovil()
    data class EnMovimiento(val velocidad: Int) : EstadoAutomovil()
}

fun mostrarEstado(estado: EstadoAutomovil) = when (estado) {
    EstadoAutomovil.Apagado      -> println("Estado: Apagado")
    EstadoAutomovil.Encendido    -> println("Estado: Encendido")
    is EstadoAutomovil.EnMovimiento -> println("Estado: En movimiento (${estado.velocidad} km/h)")
}

// --- Registro (data class) ---
data class MantenimientoVF(val fecha: String, val descripcion: String, val costo: Double)

// --- Taller de servicio ---
class TallerVF {
    fun recibir(auto: AutomovilVF) = println("Taller recibe: ${auto.marca} ${auto.modelo}")
    fun registrar(m: MantenimientoVF) = println("Registro mantenimiento -> $m")
}

fun main() = runBlocking {
    val taller = TallerVF()

    val ferrari = DeportivoVF("Ferrari", "488 GTB", 330)
        .apply { describir() }
        .also { println("LOG: creado ${it.marca} ${it.modelo}") }

    taller.recibir(ferrari)

    ferrari.arrancar()
    ferrari.acelerarHasta(280)

    listOf(
        EstadoAutomovil.Encendido,
        EstadoAutomovil.EnMovimiento(120),
        EstadoAutomovil.Apagado
    ).forEach(::mostrarEstado)

    val mantBase = MantenimientoVF("2025-08-24", "Cambio de aceite", 150.0)
    val mantPromo = mantBase.copy(costo = mantBase.costo * 0.8) // 20% off

    val resumen = mantPromo.let { "Mantenimiento: ${it.descripcion} por $${it.costo} el ${it.fecha}" }
    println(resumen)
    taller.registrar(mantPromo)
}