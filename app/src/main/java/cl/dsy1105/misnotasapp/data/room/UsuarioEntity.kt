package cl.dsy1105.misnotasapp.data.room

import android.R
import android.provider.ContactsContract
import androidx.room.Entity
import androidx.room.PrimaryKey

// Cada fila de la tabla "usuarios". El email es la clave primaria porque
// no puede haber dos usuarios con el mismo email (misma regla que antes
// aplicábamos "a mano" con usuarios.any { it.email == email }).

@Entity(tableName = "usuarios")
data class UsuarioEntity(
    @PrimaryKey val email: String,
    val password: String
)