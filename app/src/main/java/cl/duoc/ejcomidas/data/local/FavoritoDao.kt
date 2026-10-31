package cl.duoc.ejcomidas.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/** Consultas sobre la tabla de favoritos. Room genera la implementación con KSP. */
@Dao
interface FavoritoDao {

    /** Flow: Room vuelve a emitir la lista cada vez que la tabla cambia. */
    @Query("SELECT * FROM favoritos ORDER BY fecha_agregado DESC")
    fun observarTodos(): Flow<List<FavoritoEntity>>

    /** Devuelve -1 si la comida ya existía (IGNORE = no la duplica). */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertar(favorito: FavoritoEntity): Long

    /** Devuelve la cantidad de filas eliminadas. */
    @Query("DELETE FROM favoritos WHERE id_meal = :idMeal")
    suspend fun eliminarPorId(idMeal: String): Int
}
