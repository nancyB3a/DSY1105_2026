package cl.DSY1105

import kotlinx.coroutines.delay

data class Cliente(
    val tipo: String,
    val descuento: Double
){
    companion object {
        fun fromString(tipo: String) : Cliente? = when(tipo.lowercase()){
            "regular" -> Cliente("Regular", 0.05)
            "vip" -> Cliente("VIP", 0.10)
            "premium" -> Cliente("Premium", 0.15)
            else -> null
        }
    }
}

class BookFast(
    private val catalogo: List<Libro>
){
    private val carrito = mutableListOf<Libro>()

    //función para mostrar el catálogo disponible
    fun mostrarCatalogo(){
        println("Catálogo Disponible")
        catalogo.forEachIndexed { index, libro ->
            println("${index+1}.- ${libro.detalle()}")
        }
    }

    fun agregarCarrito(indices: List<Int>) : Boolean{
        try {
            for(i in indices){
                if(i in 1..catalogo.size){
                    carrito.add(catalogo[i - 1])
                }else{
                    throw IllegalArgumentException("Selección fuera del catálogo")
                }
            }
        }catch (e: Exception){
            var estado : EstadoPedido
            estado = EstadoPedido.Error(e.message ?: "Error Desconocido")
            println("UPSS!! Ocurrió un Error: ${(estado as EstadoPedido.Error).mensaje}")
            return false
        }
        return true
    }

    private fun calcularSubTotal() : Int =
        carrito.sumOf { if(it is LibroFisico) it.precioTotal() else it.precio }

    suspend fun procesarPedido(cliente: Cliente){
        var estado: EstadoPedido = EstadoPedido.Pendiente
        println("\nProcesando $estado....")
        estado = EstadoPedido.EnProceso
        delay(2000)
        println("Estado: $estado...")
        delay(2000)

        try {
            val subTotal = calcularSubTotal()
            val descuento = (subTotal * cliente.descuento).toInt()
            val iva = ((subTotal - descuento) * 0.19).toInt()
            val total = subTotal - descuento + iva

            estado = EstadoPedido.Listo

            println("\n=== RESUMEN DEL PEDIDO ===")
            carrito.forEach { println("- ${it.detalle()}") }
            println("Sbtotal: $subTotal")
            println("Descuento ${cliente.tipo}: $$descuento")
            println("IVA (19%): $$iva")
            println("Total: $$total")
            println("Estado Final: $estado")
        }catch (e: Exception){
            estado = EstadoPedido.Error(e.message ?: "Error Desconocido")
            println("Ocurrió un Error: ${(estado as EstadoPedido.Error).mensaje}")
        }
    }
}





