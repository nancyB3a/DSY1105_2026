package cl.duoc.ejcomidas.data.model

/**
 * Modelos de dominio que usa la UI.
 * Equivalen a las interfaces Category y Meal de icomidas.ts en el proyecto Ionic,
 * pero con nombres en español y sin depender del formato JSON de la API.
 */
data class Categoria(
    val id: String,
    val nombre: String,
    val imagenUrl: String,
    val descripcion: String
)

data class Comida(
    val id: String,
    val nombre: String,
    val imagenUrl: String
)
