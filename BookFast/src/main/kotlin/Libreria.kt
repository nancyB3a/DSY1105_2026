package cl.DSY1105
/*Acá crearé las clases relacionadas al LIBRO*/
open class Libro(//constructor PRIMARIO
    val titulo: String,
    val precio: Int,
    val autor: String
){
    init {
        require(precio >= 0) {"El precio NO puede ser Negativo (-)"}
    }
    open fun detalle(): String = "$titulo de $autor - $${precio}."
}

class LibroFisico(
    titulo: String,
    precio: Int,
    autor: String,
    val costoEnvio: Int
) : Libro(titulo, precio,autor){
    override fun detalle(): String {
        return super.detalle() + " - Físico - Costo de Envío: $$costoEnvio"
    }

    fun precioTotal(): Int = precio + costoEnvio
}

class LibroDigital(
    titulo: String,
    precio: Int,
    autor: String,
    val formato: String //pdf o ePub
) : Libro(titulo, precio,autor){
    override fun detalle(): String {
        return "$titulo del autor $autor - Digital en formato: $formato."
    }
}








