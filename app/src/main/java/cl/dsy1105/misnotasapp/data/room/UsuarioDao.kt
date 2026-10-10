package cl.dsy1105.misnotasapp.data.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

// El DAO (Data Access Object) declara QUÉ consultas SQL existen.
// Room genera automáticamente el código que las ejecuta.
@Dao
interface UsuarioDao {

    @Insert
    suspend fun insertar(usuario : UsuarioEntity)

    // Se usa una sola vez, al iniciar la app, para cargar todos los
    // usuarios a memoria (mismo patrón que el "cargarDatos()" original).
    @Query("SELECT * FROM usuarios")
    suspend fun obtenerTodos() : List<UsuarioEntity>
}