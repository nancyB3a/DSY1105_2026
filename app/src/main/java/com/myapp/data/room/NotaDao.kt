package com.myapp.data.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NotaDao {

    @Insert
    suspend fun insertar(nota: NotaEntity): Long

    // Devuelve un Flow (NO es suspend): Room lo mantiene "vivo" y vuelve a emitir la
    // lista completa cada vez que la tabla "notas" cambia (insertar, borrar...).
    // Gracias a esto la pantalla se actualiza sola, sin copiar datos a mano en memoria.
    @Query("SELECT * FROM notas WHERE emailUsuario = :email ORDER BY id")
    fun observarPorUsuario(email: String): Flow<List<NotaEntity>>

    // @Delete identifica la fila a borrar por su clave primaria (id),
    // tomándola del objeto NotaEntity que le pasemos.
    @Delete
    suspend fun eliminar(nota: NotaEntity)
}
