package cl.DSY1105

import kotlinx.coroutines.*

fun main() = runBlocking {
    println("===   SISTEMA BOOKFAST   ===")

    val catalogo = listOf(
        LibroFisico("El Principito",
            8000,
            "Antoine de Saint-Exupéry",
            2000),
        LibroDigital("1984",
            5000,
            "George Orwell",
            "PDF"),
        LibroFisico("Cien Años de Soledad",
            15000,
            "Gabriel García Márquez",
            3000),
        LibroDigital("Clean Code",
            15000,
            "Robert C. Martin",
            "ePub")
    )
    val sistema = BookFast(catalogo)
    sistema.mostrarCatalogo()
    try {
        do {
            println("\nIngrese su selección [números separados por coma (,)]: ")
            val seleccion = readLine()?.split(",")?.
            map { it.trim().toInt() } ?: emptyList()
        }while (!sistema.agregarCarrito(seleccion))
        println("Cliente tipo [regular/vip/premium]: ")
        val cliente =
            Cliente.fromString(readLine()!!) ?: Cliente("Regular", 0.05)

        sistema.procesarPedido(cliente)
    }catch (e: Exception){
        println("Error en el ingreso de libros a comprar!!")
    }
}