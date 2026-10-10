package cl.dsy1105.misnotasapp.data.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface NotaDao {
    // Devuelve el id autogenerado de la fila insertada; lo usamos para
    // poder borrar esa nota puntual más adelante.
    @Insert
    suspend fun insertar(nota: NotaEntity) : Long

    @Query("SELECT * FROM notas WHERE emailUsuario = :email ORDER BY id")
    suspend fun obtenerPorUsuario(email: String) : List<NotaEntity>

    // @Delete identifica la fila a borrar por su clave primaria (id),
    // tomándola del objeto NotaEntity que le pasemos.
    @Delete
    suspend fun eliminar(nota: NotaEntity)
}