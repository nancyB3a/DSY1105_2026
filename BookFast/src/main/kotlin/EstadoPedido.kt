package cl.DSY1105

sealed class EstadoPedido {
    object Pendiente : EstadoPedido(){
        override fun toString(): String {
            return "Pendiente"
        }
    }
    object EnProceso : EstadoPedido(){
        override fun toString() = "En Proceso"
    }

    object Listo : EstadoPedido(){
        override fun toString() = "Listo"
    }

    data class Error(val mensaje: String) : EstadoPedido(){
        override fun toString() = "Error: $mensaje"
    }

}

/*
otra forma

sealed class EstadoPedido {
    object Pendiente : EstadoPedido()
    object EnProceso : EstadoPedido()
    object Listo : EstadoPedido()
    data class Error(val mensaje: String) : EstadoPedido()
}

sealed class EstadoPedido {
    data class Pendiente(val info: String = "En espera") : EstadoPedido()
    data class EnProceso(val info: String = "Preparando pedido") : EstadoPedido()
    data class Listo(val info: String = "Finalizado") : EstadoPedido()
    data class Error(val mensaje: String) : EstadoPedido()
}

//y se usaría así:

var estado: EstadoPedido = EstadoPedido.Pendiente()
estado = EstadoPedido.EnProceso()
estado = EstadoPedido.Listo()
estado = EstadoPedido.Error("Precio inválido")

* */