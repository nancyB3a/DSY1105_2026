package cl.DSY1105

import kotlinx.coroutines.*

fun main() = runBlocking {
    val sistema = SistemaDelivery()

    // Crear algunos pedidos ejemplo
    val cliente1 = Cliente(1, "Ana", "VIP")
    val pedido1 = Pedido("AB123456", cliente1, Direccion("Calle 1", "123", "Ciudad"), 0.8, "2023-10-01 10:00")
    val cliente2 = Cliente(2, "Luis", "nuevos")
    val pedido2 = Pedido("CD654321", cliente2, Direccion("Calle 2", "456", "Ciudad"), 5.4, "2023-10-01 10:10", peakHour = true)
    val cliente3 = Cliente(3, "Carlos", "frec")
    val pedido3 = Pedido("EF112233", cliente3, Direccion("Calle 3", "789", "Ciudad"), 3.0, "2023-10-01 10:20")
    val cliente4 = Cliente(4, "Marta", "VIP")
    val pedido4 = Pedido("GH445566", cliente4, Direccion("Calle 4", "101", "Ciudad"), 12.5, "2023-10-01 10:30", refrigerado = true)

    // Registrar pedidos
    sistema.registrarPedido(pedido1)
    sistema.registrarPedido(pedido2)
    sistema.registrarPedido(pedido3)
    sistema.registrarPedido(pedido4)

    // Asignar pedidos
    sistema.asignarPedido(pedido1)
    sistema.asignarPedido(pedido2)
    sistema.asignarPedido(pedido3)
    sistema.asignarPedido(pedido4)

    // Procesar entregas
    delay(3000L)
    sistema.confirmarEntrega(pedido1.codigo)
    delay(3000L)
    sistema.confirmarEntrega(pedido2.codigo)
    delay(3000L)
    sistema.confirmarEntrega(pedido3.codigo)
    delay(3000L)
    sistema.confirmarEntrega(pedido4.codigo)

    // Reporte final
    sistema.reporteTurno()

}