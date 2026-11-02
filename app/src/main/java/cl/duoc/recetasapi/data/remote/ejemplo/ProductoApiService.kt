package cl.duoc.recetasapi.data.remote.ejemplo

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

/*
 * 📚 ARCHIVO DE REFERENCIA (la app no lo usa)
 *
 * TheMealDB solo permite LEER (GET). Su backend en la nube permitirá además
 * CREAR, ACTUALIZAR y ELIMINAR. Así se vería un CRUD completo con Retrofit
 * contra una API REST propia, por ejemplo:
 *
 *   BASE_URL = "https://mi-backend.onrender.com/api/"
 *
 *   GET    /productos        -> lista todos
 *   GET    /productos/5      -> obtiene uno
 *   POST   /productos        -> crea   (envía JSON en el body)
 *   PUT    /productos/5      -> actualiza
 *   DELETE /productos/5      -> elimina
 */

/** Debe calzar con el JSON que devuelve SU backend (nombres de campos). */
data class Producto(
    @SerializedName("id") val id: Int? = null,      // null al crear: lo asigna la BD
    @SerializedName("nombre") val nombre: String,
    @SerializedName("precio") val precio: Double,
    @SerializedName("stock") val stock: Int
)

interface ProductoApiService {

    @GET("productos")
    suspend fun listar(): List<Producto>

    // @Path reemplaza {id} en la ruta: productos/5
    @GET("productos/{id}")
    suspend fun obtener(@Path("id") id: Int): Producto

    // @Body convierte el objeto Kotlin a JSON y lo envía
    @POST("productos")
    suspend fun crear(@Body producto: Producto): Producto

    @PUT("productos/{id}")
    suspend fun actualizar(@Path("id") id: Int, @Body producto: Producto): Producto

    // Response<Unit> permite revisar el código HTTP (200, 204, 404...)
    @DELETE("productos/{id}")
    suspend fun eliminar(@Path("id") id: Int): Response<Unit>
}

/*
 * Uso desde un repositorio o ViewModel:
 *
 *   val nuevo = api.crear(Producto(nombre = "Café", precio = 2500.0, stock = 10))
 *
 *   val respuesta = api.eliminar(5)
 *   if (respuesta.isSuccessful) { ... } else { val codigo = respuesta.code() }
 *
 * Si la API exige token (login), se envía en un header:
 *
 *   @GET("productos")
 *   suspend fun listar(@Header("Authorization") token: String): List<Producto>
 *   // api.listar("Bearer $token")
 */
