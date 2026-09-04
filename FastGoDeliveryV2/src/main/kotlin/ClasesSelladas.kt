package cl.DSY1105

// Clases selladas para los estados del pedido y del repartidor
sealed class EstadoPedido {
    object Pendiente : EstadoPedido()
    object EnCamino : EstadoPedido()
    object Entregado : EstadoPedido()
    object Cancelado : EstadoPedido()
}

sealed class EstadoRepartidor {
    object Disponible : EstadoRepartidor()
    object Ocupado : EstadoRepartidor()
    object Procesando : EstadoRepartidor()
    object FueraDeServicio : EstadoRepartidor()
}

