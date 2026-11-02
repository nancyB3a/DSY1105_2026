package cl.duoc.recetasapi.data.remote

import cl.duoc.recetasapi.data.model.RecetasResponse
import retrofit2.http.GET
import retrofit2.http.Query

/*
 * ⭐ PASO 2: INTERFAZ DE LA API (los "endpoints")
 *
 * Cada función = una petición HTTP. Retrofit escribe el código por nosotros.
 *
 *   @GET("search.php")  -> la ruta que se suma a la BASE_URL
 *   @Query("s")         -> agrega ?s=valor a la URL
 *   suspend             -> se llama desde una corrutina, no bloquea la pantalla
 *
 * Resultado: https://www.themealdb.com/api/json/v1/1/search.php?s=chicken
 */
interface ApiService {

    @GET("search.php")
    suspend fun buscarRecetas(@Query("s") texto: String): RecetasResponse
}
