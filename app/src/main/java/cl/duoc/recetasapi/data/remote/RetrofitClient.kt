package cl.duoc.recetasapi.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/*
 * ⭐ PASO 3: CLIENTE RETROFIT
 *
 * Se configura una sola vez (por eso es un 'object' = singleton):
 *   - baseUrl: la dirección del servidor. SIEMPRE debe terminar en "/"
 *   - GsonConverterFactory: convierte el JSON en nuestras data class
 *
 * 👉 Para su proyecto: cambiar BASE_URL por la URL de SU backend en la nube,
 *    por ejemplo "https://mi-api.onrender.com/api/"
 */
object RetrofitClient {

    private const val BASE_URL = "https://www.themealdb.com/api/json/v1/1/"

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
