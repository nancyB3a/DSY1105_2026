package cl.dsy1105.misnotasapp.data.room

import androidx.room.Entity
import androidx.room.PrimaryKey

// Cada fila de la tabla "notas". "id" se autogenera (1, 2, 3...) y
// "emailUsuario" nos dice a qué usuario pertenece cada nota — es el
// equivalente relacional al Map<String, List<String>> que usábamos con DataStore.
@Entity(tableName = "notas")
data class NotaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val emailUsuario: String,
    val texto: String
)