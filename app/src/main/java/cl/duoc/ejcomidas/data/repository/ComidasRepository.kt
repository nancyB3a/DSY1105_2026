package cl.duoc.ejcomidas.data.repository

import cl.duoc.ejcomidas.data.local.FavoritoDao
import cl.duoc.ejcomidas.data.local.toEntity
import cl.duoc.ejcomidas.data.local.toModel
import cl.duoc.ejcomidas.data.model.Categoria
import cl.duoc.ejcomidas.data.model.Comida
import cl.duoc.ejcomidas.data.remote.MealApiService
import cl.duoc.ejcomidas.data.remote.dto.toModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Única fuente de datos para los ViewModels.
 * Junta la API remota (Retrofit) y la base local (Room).
 */
class ComidasRepository(
    private val api: MealApiService,
    private val favoritoDao: FavoritoDao
) {

    // ---------- Remoto (Retrofit) ----------

    suspend fun obtenerCategorias(): List<Categoria> =
        api.getCategorias().categories.orEmpty().map { it.toModel() }

    suspend fun obtenerComidasPorCategoria(categoria: String): List<Comida> =
        api.getComidasPorCategoria(categoria).meals.orEmpty().map { it.toModel() }

    // ---------- Local (Room) ----------

    /** Se actualiza solo cuando se agrega o elimina un favorito. */
    val favoritos: Flow<List<Comida>> =
        favoritoDao.observarTodos().map { lista -> lista.map { it.toModel() } }

    /** Las funciones suspend de Room ya corren fuera del hilo principal. */
    suspend fun agregarFavorito(comida: Comida): Boolean =
        favoritoDao.insertar(comida.toEntity()) != -1L

    suspend fun quitarFavorito(comida: Comida): Boolean =
        favoritoDao.eliminarPorId(comida.id) > 0
}
