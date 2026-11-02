package cl.duoc.recetasapi.data.repository

import cl.duoc.recetasapi.data.model.Receta
import cl.duoc.recetasapi.data.remote.ApiService
import cl.duoc.recetasapi.data.remote.RetrofitClient

/*
 * ⭐ PASO 4: REPOSITORIO
 *
 * Intermediario entre el ViewModel y la fuente de datos.
 * El ViewModel no sabe si los datos vienen de una API, de Room o de ambos.
 * Aquí también se "limpia" la respuesta (null -> lista vacía).
 */
class RecetaRepository(
    private val api: ApiService = RetrofitClient.api
) {
    suspend fun buscarRecetas(texto: String): List<Receta> {
        val respuesta = api.buscarRecetas(texto)
        return respuesta.recetas ?: emptyList()
    }
}
