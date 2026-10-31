package cl.duoc.ejcomidas.data.remote.dto

import cl.duoc.ejcomidas.data.model.Categoria
import cl.duoc.ejcomidas.data.model.Comida
import com.google.gson.annotations.SerializedName

/*
 * DTOs: reflejan exactamente el JSON que devuelve https://www.themealdb.com
 * (en Ionic eran RespuestaComidas / RespuestaMeal).
 */

data class CategoriasResponse(
    @SerializedName("categories") val categories: List<CategoryDto>?
)

data class CategoryDto(
    @SerializedName("idCategory") val idCategory: String,
    @SerializedName("strCategory") val strCategory: String,
    @SerializedName("strCategoryThumb") val strCategoryThumb: String?,
    @SerializedName("strCategoryDescription") val strCategoryDescription: String?
)

// La API devuelve {"meals": null} cuando no hay resultados, por eso es nullable
data class ComidasResponse(
    @SerializedName("meals") val meals: List<MealDto>?
)

data class MealDto(
    @SerializedName("idMeal") val idMeal: String,
    @SerializedName("strMeal") val strMeal: String,
    @SerializedName("strMealThumb") val strMealThumb: String?
)

// ---- Mapeo DTO -> modelo de dominio ----

fun CategoryDto.toModel() = Categoria(
    id = idCategory,
    nombre = strCategory,
    imagenUrl = strCategoryThumb.orEmpty(),
    descripcion = strCategoryDescription.orEmpty().trim()
)

fun MealDto.toModel() = Comida(
    id = idMeal,
    nombre = strMeal,
    imagenUrl = strMealThumb.orEmpty()
)
