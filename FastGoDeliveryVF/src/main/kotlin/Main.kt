package cl.DSY1105

import kotlinx.coroutines.runBlocking
import java.time.LocalDateTime


fun main() = runBlocking {

    println("===   SISTEMA FASTGO DELIVERY   ===\n")

    val ahora = LocalDateTime.now()
    val sistema = FastGoDelivery(cantidadRepartidores = 8)

    val pedidosValidos = listOf(
        PedidoBicicleta("DG100001", 0.8, ahora, Cliente("Nuevo", 0.0)),
        PedidoMoto("DG100002", 5.4, ahora, Cliente("Frecuente", 0.15), horarioPeak = true),
        PedidoMoto("DG100003", 3.0, ahora, Cliente("VIP", 0.40), horarioPeak = false),
        PedidoAuto("DG100004", 12.5, ahora, Cliente("Nuevo", 0.0), refrigerado = true),
        PedidoAuto("DG100005", 7.0, ahora, Cliente("Frecuente", 0.15), refrigerado = false)
    )

    println(">>> Asignando y entregando los pedidos de prueba <<<")
    for (pedido in pedidosValidos) {
        if (sistema.asignarPedido(pedido)) {
            sistema.confirmarEntrega(pedido.codigo)
        }
    }

    // R6 - Prueba de error: código de pedido inválido.
    println("\n>>> Prueba de error: código de pedido inválido <<<")
    val pedidoInvalido = PedidoBicicleta("12AB3456", 2.0, ahora, Cliente("Nuevo", 0.0))
    sistema.asignarPedido(pedidoInvalido)

    // R6 - Prueba de error: pedido no encontrado (código que nunca fue asignado).
    println("\n>>> Prueba de error: pedido no encontrado <<<")
    sistema.confirmarEntrega("ZZ999999")

    // R6 - Prueba de error: sin repartidores disponibles.
    // La flota tiene 8 repartidores; ya se liberaron los 5 usados arriba, así
    // que se ocupan los 8 con pedidos de prueba y se intenta un noveno.
    println("\n>>> Prueba de error: sin repartidores disponibles <<<")
    repeat(8) { i ->
        val codigo = "LL00000${i + 1}" // formato válido: dos letras + seis dígitos
        sistema.asignarPedido(PedidoBicicleta(codigo, 2.0, ahora, Cliente("Nuevo", 0.0)))
    }
    sistema.asignarPedido(PedidoBicicleta("MM000001", 2.0, ahora, Cliente("Nuevo", 0.0)))

    // Se liberan los repartidores usados en la prueba anterior.
    repeat(8) { i -> sistema.confirmarEntrega("LL00000${i + 1}") }

    println("\n>>> Consultas de negocio <<<")
    println("Pedidos VIP entregados: ${sistema.pedidosVipDelHistorial().map { it.pedido.codigo }}")
    println("Códigos entregados: ${sistema.codigosEntregados()}")
    sistema.pedidoMayorDistancia()?.let {
        println("Pedido con mayor distancia: ${it.pedido.codigo} (${it.pedido.distanciaKm} km)")
    }

    sistema.imprimirReporteCierre()
}