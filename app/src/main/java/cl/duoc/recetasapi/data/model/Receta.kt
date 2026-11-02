package cl.duoc.recetasapi.data.model

import com.google.gson.annotations.SerializedName

/*
 * ⭐ PASO 1: MODELO (data class)
 *
 * Representa el JSON que responde la API. Ejemplo real de
 * https://www.themealdb.com/api/json/v1/1/search.php?s=chicken
 *
 *  {
 *    "meals": [
 *      {
 *        "idMeal": "52795",
 *        "strMeal": "Chicken Handi",
 *        "strCategory": "Chicken",
 *        "strArea": "Indian",
 *        "strMealThumb": "https://www.themealdb.com/images/media/meals/....jpg",
 *        ...
 *      }
 *    ]
 *  }
 *
 * Solo declaramos los campos que nos interesan; Gson ignora el resto.
 * @SerializedName("nombreEnElJson") permite usar nombres propios en Kotlin.
 */

/** El objeto "raíz" del JSON: contiene la lista. Es null si no hay resultados. */
data class RecetasResponse(
    @SerializedName("meals") val recetas: List<Receta>?
)

/** Cada elemento de la lista "meals". */
data class Receta(
    @SerializedName("idMeal") val id: String,
    @SerializedName("strMeal") val nombre: String,
    @SerializedName("strCategory") val categoria: String?,
    @SerializedName("strArea") val origen: String?,
    @SerializedName("strMealThumb") val imagenUrl: String?
)
