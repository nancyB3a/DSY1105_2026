# Que Cocino App

Consume la API pública de [TheMealDB](https://www.themealdb.com/api.php) y guarda las recetas favoritas en una base de datos local.

## 1. Estructura App

| Tecnología | Para qué se usa |
|---|---|
| **Kotlin** | Lenguaje de la app |
| **Jetpack Compose** (Material 3) | Interfaz declarativa |
| **MVVM** | Separa la UI, la lógica y los datos |
| **Retrofit + Gson** | Consumo de la API REST |
| **Room** (sobre SQLite) | Persistencia local de favoritos |
| **Navigation Compose** | Navegación entre las 3 pestañas |
| **Coil** | Carga de imágenes desde URL |
| **Corrutinas + StateFlow** | Tareas asíncronas y estado observable |

---

## Funciones de la App: EjComida
- Estados de **carga y error con botón Reintentar**.
- En "Por categoría" el corazón indica si la comida ya es favorita y el menú permite quitarla desde ahí.
- Al cambiar rápido de categoría se cancela la petición anterior (no se mezclan resultados).
- Menú con **Compartir en…** y **Enviar por email** (asunto y cuerpo con nombre, enlace y foto de la receta).
- Los favoritos se observan con `Flow` de Room: la pestaña Favoritas se actualiza sola.
- Los favoritos se ordenan del más reciente al más antiguo (como `unshift()`).

## Tabla Room (`FavoritoEntity`)
Room genera esta tabla SQLite:
```sql
CREATE TABLE favoritos (
    id_meal        TEXT PRIMARY KEY,
    nombre         TEXT NOT NULL,
    imagen_url     TEXT,
    fecha_agregado INTEGER NOT NULL
);
```
Se puede inspeccionar en Android Studio con **View → Tool Windows → App Inspection → Database Inspector**.

## 2. Qué hace la app

| Pestaña | Contenido | Origen de los datos |
|---|---|---|
| **Tipos** | Tarjetas con todas las categorías, su foto y su descripción | API: `categories.php` |
| **Por Categoría** | Pestañas desplazables (Beef, Chicken, Dessert…) y la grilla de comidas de la categoría elegida | API: `filter.php?c=` |
| **Favoritas** | Comidas guardadas por el usuario | Room (local) |

Acciones sobre cada comida:
- Tocar el **nombre o la imagen** abre la receta en el navegador.
- El botón **⋮** abre un menú con:
  - **Compartir en…**: WhatsApp, Telegram, Bluetooth, etc.
  - **Enviar por email**: solo aparecen apps de correo.
  - **Agregar a favoritos** o **Quitar de favoritos**.

---

## 3. Arquitectura MVVM

```
┌─────────────── UI (Compose) ───────────────┐
│  Screens + Components                       │  Solo dibujan y avisan eventos
└──────────────▲───────────────┬──────────────┘
     StateFlow │ (estado)      │ eventos (buscar, agregar…)
┌──────────────┴───────────────▼──────────────┐
│  ViewModels                                  │  Lógica de pantalla + estado
└──────────────▲───────────────┬──────────────┘
               │               │
┌──────────────┴───────────────▼──────────────┐
│  ComidasRepository                           │  Única fuente de datos
└───────▲──────────────────────────▲──────────┘
        │                          │
 ┌──────┴───────┐          ┌───────┴────────┐
 │ Retrofit     │          │ Room           │
 │ (TheMealDB)  │          │ (favoritos.db) │
 └──────────────┘          └────────────────┘
```

**Regla de oro:** cada capa conoce solo a la de abajo. La pantalla nunca llama a Retrofit ni a Room directamente.

### Estructura de carpetas
```
cl.duoc.ejcomidas
├── EjComidasApp.kt              Application: crea el repositorio una sola vez
├── MainActivity.kt              Punto de entrada: aplica el tema y la navegación
├── data
│   ├── model/Modelos.kt         Categoria, Comida (modelos que usa la UI)
│   ├── remote/
│   │   ├── MealApiService.kt    Endpoints de la API (Retrofit)
│   │   ├── RetrofitClient.kt    Configuración de Retrofit
│   │   └── dto/MealDbDtos.kt    Clases que calzan con el JSON + mapeo a modelos
│   ├── local/
│   │   ├── FavoritoEntity.kt    Tabla "favoritos" (@Entity)
│   │   ├── FavoritoDao.kt       Consultas (@Dao)
│   │   └── AppDatabase.kt       Base de datos Room (singleton)
│   └── repository/
│       └── ComidasRepository.kt Junta API + Room
├── ui
│   ├── viewmodel/               UiState, CategoriasVM, PorCategoriaVM, FavoritosVM
│   ├── screens/                 Una pantalla por pestaña + ListaComidas
│   ├── components/              CategoriaCard, ComidaCard, Cargando/Error
│   ├── navigation/              Barra inferior + NavHost
│   └── theme/                   Colores (tomados de variables.scss de Ionic)
└── util/AccionesReceta.kt       Abrir, compartir y enviar receta por email
```

---

## 4. Consumo de la API con Retrofit

Retrofit convierte una **interfaz de Kotlin** en llamadas HTTP reales. Nosotros declaramos *qué* pedir y Retrofit escribe el *cómo*.

### Paso 0: Permiso de internet
`AndroidManifest.xml`
```xml
<uses-permission android:name="android.permission.INTERNET" />
```

### Paso 1: Dependencias
`gradle/libs.versions.toml` y `app/build.gradle.kts`
```kotlin
implementation(libs.retrofit)                // cliente HTTP
implementation(libs.retrofit.converter.gson) // JSON <-> objetos Kotlin
implementation(libs.okhttp.logging)          // ver las peticiones en Logcat
```

### Paso 2: DTOs, las clases que calzan con el JSON
La API responde, por ejemplo, en `filter.php?c=Beef`:
```json
{
  "meals": [
    { "strMeal": "Beef and Mustard Pie", "strMealThumb": "https://...jpg", "idMeal": "52874" }
  ]
}
```
`data/remote/dto/MealDbDtos.kt`
```kotlin
data class ComidasResponse(
    @SerializedName("meals") val meals: List<MealDto>?   // null si no hay resultados
)

data class MealDto(
    @SerializedName("idMeal") val idMeal: String,
    @SerializedName("strMeal") val strMeal: String,
    @SerializedName("strMealThumb") val strMealThumb: String?
)
```
- `@SerializedName` indica el nombre exacto del campo en el JSON.
- Los campos que no declaramos se ignoran.
- Luego se **mapean** al modelo de la app (`Comida`) con nombres en español. Así, si la API cambia, solo se modifica el DTO:
```kotlin
fun MealDto.toModel() = Comida(id = idMeal, nombre = strMeal, imagenUrl = strMealThumb.orEmpty())
```

### Paso 3: Interfaz con los endpoints
`data/remote/MealApiService.kt`
```kotlin
interface MealApiService {

    @GET("categories.php")
    suspend fun getCategorias(): CategoriasResponse

    @GET("filter.php")
    suspend fun getComidasPorCategoria(@Query("c") categoria: String): ComidasResponse
}
```
| Anotación | Qué hace | Ejemplo |
|---|---|---|
| `@GET("ruta")` | Petición GET a `BASE_URL + ruta` | `.../categories.php` |
| `@Query("c")` | Agrega `?c=valor` a la URL | `filter.php?c=Beef` |
| `suspend` | Se ejecuta en una corrutina sin congelar la pantalla | |

### Paso 4: Cliente Retrofit (singleton)
`data/remote/RetrofitClient.kt`
```kotlin
object RetrofitClient {
    private const val BASE_URL = "https://www.themealdb.com/api/json/v1/1/"  // termina en "/"

    private val okHttp = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply { level = Level.BASIC })
        .connectTimeout(15, TimeUnit.SECONDS)
        .build()

    val api: MealApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttp)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MealApiService::class.java)
    }
}
```
> 💡 En **Logcat**, filtra por `okhttp` para ver cada petición, su código HTTP y el tiempo que tardó.

### Paso 5: Repositorio
`data/repository/ComidasRepository.kt`
```kotlin
suspend fun obtenerComidasPorCategoria(categoria: String): List<Comida> =
    api.getComidasPorCategoria(categoria).meals.orEmpty().map { it.toModel() }
```

### Paso 6: ViewModel con estados
`ui/viewmodel/UiState.kt`
```kotlin
sealed interface UiState<out T> {
    data object Cargando : UiState<Nothing>
    data class Exito<T>(val datos: T) : UiState<T>
    data class Error(val mensaje: String) : UiState<Nothing>
}
```
`ui/viewmodel/PorCategoriaViewModel.kt`
```kotlin
trabajoCarga?.cancel()                      // cancela la petición anterior
trabajoCarga = viewModelScope.launch {
    _estado.value = UiState.Cargando
    _estado.value = try {
        UiState.Exito(repository.obtenerComidasPorCategoria(categoria))
    } catch (e: CancellationException) {
        throw e                             // no tratar la cancelación como error
    } catch (e: Exception) {
        UiState.Error(e.aMensaje())         // "Sin conexión a internet", etc.
    }
}
```

### Paso 7: La pantalla observa el estado
`ui/screens/PorCategoriaScreen.kt`
```kotlin
val estado by viewModel.estado.collectAsStateWithLifecycle()

when (val e = estado) {
    is UiState.Cargando -> CargandoView()
    is UiState.Error    -> ErrorView(e.mensaje, viewModel::reintentar)
    is UiState.Exito    -> ListaComidas(comidas = e.datos, ...)
}
```

### Recorrido completo de una petición
```
Usuario toca "Pasta"
  → PorCategoriaScreen llama viewModel.seleccionarCategoria("Pasta")
  → ViewModel: estado = Cargando  (aparece el spinner)
  → Repository → MealApiService.getComidasPorCategoria("Pasta")
  → Retrofit: GET https://www.themealdb.com/api/json/v1/1/filter.php?c=Pasta
  → Gson convierte el JSON en ComidasResponse → se mapea a List<Comida>
  → ViewModel: estado = Exito(lista)
  → Compose redibuja la grilla automáticamente
```

---

## 5. Persistencia local con Room

Room es una capa sobre **SQLite** que genera el código SQL repetitivo con **KSP**.

| Pieza | Archivo | Qué es |
|---|---|---|
| `@Entity` | `FavoritoEntity.kt` | Una tabla. Cada propiedad es una columna |
| `@Dao` | `FavoritoDao.kt` | Las consultas (`@Query`, `@Insert`) |
| `@Database` | `AppDatabase.kt` | La base de datos. Se crea una sola vez (singleton) |

```kotlin
@Dao
interface FavoritoDao {
    @Query("SELECT * FROM favoritos ORDER BY fecha_agregado DESC")
    fun observarTodos(): Flow<List<FavoritoEntity>>      // se re-emite con cada cambio

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertar(favorito: FavoritoEntity): Long // -1 si ya existía

    @Query("DELETE FROM favoritos WHERE id_meal = :idMeal")
    suspend fun eliminarPorId(idMeal: String): Int
}
```
Como `observarTodos()` devuelve un **`Flow`**, cuando se agrega o quita un favorito, la pestaña *Favoritas* y los ❤️ de *Por Categoría* se actualizan solos, sin recargar.

> 💡 Para ver la tabla mientras la app corre, abre **View → Tool Windows → App Inspection → Database Inspector**.

---

## 6. Componentes (Compose y Android)

### Estructura y navegación
| Componente | Dónde | Para qué |
|---|---|---|
| `Scaffold` | `AppNavigation.kt` | Esqueleto de la pantalla: barra superior, barra inferior, snackbars y contenido |
| `TopAppBar` | `AppNavigation.kt` | Título que cambia según la pestaña |
| `NavigationBar` + `NavigationBarItem` | `AppNavigation.kt` | Barra de pestañas inferior (`ion-tab-bar`) |
| `NavHost` + `composable("ruta")` | `AppNavigation.kt` | Define qué pantalla se muestra en cada ruta |
| `rememberNavController()` | `AppNavigation.kt` | Controla la navegación. `popUpTo` + `saveState` conservan el estado de cada pestaña |
| `ScrollableTabRow` + `Tab` | `PorCategoriaScreen.kt` | Pestañas desplazables horizontalmente (`ion-segment scrollable`) |

### Listas y tarjetas
| Componente | Dónde | Para qué |
|---|---|---|
| `LazyVerticalGrid` + `GridCells.Adaptive(300.dp)` | `CategoriasScreen`, `ListaComidas` | Grilla que crea solo los ítems visibles. Las columnas se ajustan al ancho (`ion-grid` responsivo) |
| `items(lista, key = { it.id })` | idem | La `key` permite animar y no redibujar de más |
| `Modifier.animateItem()` | `ListaComidas.kt` | Anima la entrada y la salida de ítems (reemplaza `animate.css`) |
| `Card` | `CategoriaCard`, `ComidaCard` | Tarjeta (`ion-card`) |
| `AsyncImage` (Coil) | tarjetas | Descarga y muestra una imagen desde una URL (`ion-img`) |
| `DropdownMenu` + `DropdownMenuItem` | `ComidaCard.kt` | Menú del botón ⋮ (reemplaza el `ActionSheet`) |

### Estados y mensajes
| Componente | Dónde | Para qué |
|---|---|---|
| `CircularProgressIndicator` | `Estados.kt` | Spinner mientras carga |
| `ErrorView` (propio) | `Estados.kt` | Mensaje de error con botón **Reintentar** |
| `SnackbarHost` + `SnackbarHostState` | `AppNavigation.kt` | Mensajes "¡Agregado a favoritos!" (reemplaza `ToastController`) |
| `Channel` → `receiveAsFlow()` | `FavoritosViewModel.kt` | Eventos de una sola vez (mensajes) del ViewModel a la UI |
| `LaunchedEffect` | `AppNavigation.kt` | Ejecuta una corrutina ligada a la pantalla (escucha los mensajes) |

### Estado y ciclo de vida
| Concepto | Para qué |
|---|---|
| `ViewModel` + `viewModelScope` | Sobrevive a la rotación de pantalla. Sus corrutinas se cancelan solas al cerrar |
| `StateFlow` | Valor observable que siempre tiene un estado actual |
| `collectAsStateWithLifecycle()` | Convierte un `StateFlow` en estado de Compose y deja de escuchar cuando la app pasa a segundo plano |
| `stateIn(...)` | Convierte el `Flow` de Room en `StateFlow` para la UI |
| `remember { mutableStateOf() }` | Estado local de un composable (p. ej., si el menú ⋮ está abierto) |
| `viewModelFactory { initializer { } }` | Crea ViewModels que reciben el repositorio (inyección manual desde `EjComidasApp`) |

```kotlin
// "mailto:" hace que SOLO aparezcan apps de correo
val intent = Intent(Intent.ACTION_SENDTO).apply {
    data = Uri.parse("mailto:")
    putExtra(Intent.EXTRA_SUBJECT, "Receta: ${comida.nombre}")
    putExtra(Intent.EXTRA_TEXT, textoReceta(comida))
}
```
> En emuladores sin cuenta de correo aparece el aviso *"No hay una app de correo configurada"*. Hay que usar una imagen con Google Play o un teléfono físico.

---

## 7. Problemas frecuentes

| Síntoma | Solución |
|---|---|
| Error `25.0.3` al compilar | Cambiar **Gradle JDK** a 21 (ver sección 1) |
| `JAVA_HOME is not set` en la terminal | `$env:JAVA_HOME = "C:\Users\<usuario>\.jdks\<jdk-21>"` |
| `Timeout error waiting for 300000 ms` o se queda en *Installing APK* | Emulador bloqueado: **Wipe Data** + **Cold Boot**, o crear un emulador nuevo con API 34/35 |
| `Unable to delete directory ...\build\...` | Ejecutar `.\gradlew --stop`, borrar `app\build` y excluir el proyecto y `.gradle` del antivirus |
| "Sin conexión a internet" en la app | Revisar que el emulador tenga red (abrir Chrome dentro del emulador) |
| "No hay una app de correo configurada" | Usar un emulador con Google Play y una cuenta de Gmail, o un teléfono físico |

---

## 8. Ejercicios propuestos
1. **Detalle de receta:** agregar `@GET("lookup.php") suspend fun getDetalle(@Query("i") id: String)` y una pantalla con ingredientes e instrucciones.
2. **Buscador:** usar `search.php?s=` con un `OutlinedTextField` en la pestaña *Tipos*.
3. **Categorías dinámicas:** cargar las pestañas de *Por Categoría* desde `categories.php` en vez de la lista fija.
4. **Navegar desde Tipos:** al tocar una categoría, abrir *Por Categoría* con esa categoría seleccionada.
5. **Backend propio:** reemplazar `BASE_URL` por la API de su proyecto y agregar endpoints `@POST`, `@PUT` y `@DELETE` con `@Body` y `@Path`.
