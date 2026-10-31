package cl.duoc.ejcomidas.data.remote

import cl.duoc.ejcomidas.data.remote.dto.CategoriasResponse
import cl.duoc.ejcomidas.data.remote.dto.ComidasResponse
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Endpoints de TheMealDB (equivale a comidas.service.ts).
 * Las funciones son suspend: Retrofit las ejecuta fuera del hilo principal.
 */
interface MealApiService {

    // https://www.themealdb.com/api/json/v1/1/categories.php
    @GET("categories.php")
    suspend fun getCategorias(): CategoriasResponse

    // https://www.themealdb.com/api/json/v1/1/filter.php?c=Beef
    @GET("filter.php")
    suspend fun getComidasPorCategoria(@Query("c") categoria: String): ComidasResponse
}
