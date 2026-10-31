package cl.duoc.ejcomidas.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import cl.duoc.ejcomidas.data.model.Comida

/** Tabla 'favoritos' (reemplaza la clave 'favoritos' del Ionic Storage). */
@Entity(tableName = "favoritos")
data class FavoritoEntity(
    @PrimaryKey
    @ColumnInfo(name = "id_meal") val idMeal: String,
    @ColumnInfo(name = "nombre") val nombre: String,
    @ColumnInfo(name = "imagen_url") val imagenUrl: String,
    @ColumnInfo(name = "fecha_agregado") val fechaAgregado: Long = System.currentTimeMillis()
)

// ---- Mapeo Entity <-> modelo de dominio ----

fun FavoritoEntity.toModel() = Comida(id = idMeal, nombre = nombre, imagenUrl = imagenUrl)

fun Comida.toEntity() = FavoritoEntity(idMeal = id, nombre = nombre, imagenUrl = imagenUrl)
