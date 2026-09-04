package cl.DSY1105

import kotlinx.coroutines.runBlocking

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
fun main() = runBlocking {
    println("******* Probando con ReadLine")
    println("===   SISTEMA FASTGO DELIVERY   ===")

    val pedidosDisponibles1 = listOf(
        PedidoBicicleta("DG100001", 0.8),
        PedidoMoto("DG100002", 5.4, horarioPeak = true),
        PedidoMoto("DG100003", 3.0, horarioPeak = false),
        PedidoAuto("DG100004", 12.5, refrigerado = true),
        PedidoAuto("DG100005", 7.0, refrigerado = false)
    )
    val sistema1 = FastGoDelivery(pedidosDisponibles1)

    sistema1.mostrarPedidosDisponibles()

    try {
        var seleccion: List<Int>
        do {
            println("\nIngrese los pedidos a despachar [números separados por coma (,)]:")
            seleccion = readLine()
                ?.split(",")
                ?.map { it.trim().toInt() } ?: emptyList()
        } while (!sistema1.agregarADespacho(seleccion))

        println("Cliente tipo [nuevo/frecuente/vip]: ")
        val cliente = Cliente.fromString(readLine() ?: "") ?: Cliente("Nuevo", 0.0)

        sistema1.procesarDespacho(cliente)

        // Ejemplo de uso de las consultas con filter/map tras el despacho.
        println("\nCódigos despachados: ${sistema1.codigosDespacho()}")
        println("Pedidos en auto: ${sistema1.pedidosAuto().map { it.codigo }}")
    } catch (e: Exception) {
        println("Error: ${e.message} - Se esperaba un número!!")
    }

    //region PROBANDO COMO ENUNCIADO
    println("\n\n************************************************************************************************")
    println("******* Probando como pide el Enunciado")
    println("===   SISTEMA FASTGO DELIVERY   ===")

    // Pedidos de prueba sugeridos (índice 1 a 6 en pedidosDisponibles).
    val pedidosDisponibles = listOf(
        PedidoBicicleta("DG100001", 0.8),                        // 1
        PedidoMoto("DG100002", 5.4, horarioPeak = true),         // 2
        PedidoMoto("DG100003", 3.0, horarioPeak = false),        // 3
        PedidoAuto("DG100004", 12.5, refrigerado = true),        // 4
        PedidoAuto("DG100005", 7.0, refrigerado = false),        // 5
        PedidoBicicleta("12AB3456", 2.0)                          // 6 - código con formato inválido
    )
    val sistema = FastGoDelivery(pedidosDisponibles)
    sistema.mostrarPedidosDisponibles()

    // Cada caso de prueba es (índice del pedido, cliente asociado), según el enunciado.
    val casosDePrueba = listOf(
        1 to Cliente("Nuevo", 0.0),        // DG100001 - cliente nuevo
        2 to Cliente("Frecuente", 0.15),   // DG100002 - cliente frecuente
        3 to Cliente("VIP", 0.40),         // DG100003 - cliente VIP
        4 to Cliente("Nuevo", 0.0),        // DG100004 - cliente nuevo
        5 to Cliente("Frecuente", 0.15),   // DG100005 - cliente frecuente
        6 to Cliente("Nuevo", 0.0)         // 12AB3456 - prueba de error de código
    )

    for ((indice, cliente) in casosDePrueba) {
        println("\n-----------------------------------------------------")
        println("Procesando pedido N°$indice (${pedidosDisponibles[indice - 1].detalle()})")
        println("-----------------------------------------------------")

        // Si el pedido no pasa la validación (ej. código inválido), agregarADespacho
        // ya informa el error internamente y no se llama a procesarDespacho.
        if (sistema.agregarADespacho(listOf(indice))) {
            sistema.procesarDespacho(cliente)
        }
    }

    println("\n=== Consultas finales de ejemplo ===")
    println("Códigos del último despacho procesado: ${sistema.codigosDespacho()}")
    println("Pedidos en auto del último despacho: ${sistema.pedidosAuto().map { it.codigo }}")

    //endregion
}
