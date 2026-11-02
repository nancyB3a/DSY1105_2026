# Recetas API: ejemplo básico de Retrofit

App mínima para **explicar cómo se consume una API REST** desde Android con
Kotlin, Jetpack Compose, MVVM y Retrofit. Busca recetas en
[TheMealDB](https://www.themealdb.com/api.php) y las muestra en una lista.

## Requisitos
- **Gradle JDK 21** (o 17): *File → Settings → Build, Execution, Deployment → Build Tools → Gradle → Gradle JDK*.
  El Java 25 que trae Android Studio no es compatible con Gradle 8.7.
- Emulador con **API 34 o 35** y conexión a internet.

## Recorrido de una petición
```
RecetasScreen ──(buscar)──▶ RecetaViewModel ──▶ RecetaRepository ──▶ ApiService (Retrofit)
      ▲                          │                                        │
      └──── StateFlow<UiState> ◀─┘◀──────── List<Receta> ◀──── JSON ◀────┘  HTTP GET
```

## Los pasos (marcados con ⭐ en el código)
| Paso | Archivo | Qué hace |
|---|---|---|
| 0 | `AndroidManifest.xml` | Permiso `INTERNET` |
| 1 | `data/model/Receta.kt` | `data class` que calza con el JSON (`@SerializedName`) |
| 2 | `data/remote/ApiService.kt` | Endpoints con `@GET` y `@Query` |
| 3 | `data/remote/RetrofitClient.kt` | `BASE_URL` + convertidor Gson (singleton) |
| 4 | `data/repository/RecetaRepository.kt` | Llama a la API y limpia la respuesta |
| 5 | `ui/viewmodel/RecetaViewModel.kt` | Corrutina + estados Cargando / Éxito / Error |
| 6 | `ui/screen/RecetasScreen.kt` | Observa el estado y dibuja la UI |

📚 `data/remote/ejemplo/ProductoApiService.kt`: **CRUD completo** (`GET`, `POST`, `PUT`, `DELETE`,
`@Path`, `@Body`, `Response<T>`, header con token) para adaptarlo al backend propio.

## Llevarlo a su propio backend en la nube
1. **Probar la API primero** en el navegador o en Postman: ver la URL exacta y el JSON que responde.
2. Cambiar `BASE_URL` en `RetrofitClient` por la de su servidor. **Siempre debe terminar en `/`**.
3. Crear las `data class` con los **mismos nombres de campos** del JSON (o usar `@SerializedName`).
4. Declarar los endpoints en la interfaz, siguiendo `ProductoApiService.kt` como guía.
5. El repositorio, el ViewModel y la pantalla siguen el mismo patrón de este ejemplo.

### Errores frecuentes
| Síntoma | Causa |
|---|---|
| `Unable to resolve host` | Falta el permiso `INTERNET` o el emulador no tiene red |
| `CLEARTEXT communication not permitted` | La URL es `http://` en vez de `https://` (Android bloquea http por defecto) |
| `Expected BEGIN_OBJECT but was BEGIN_ARRAY` | La API devuelve una lista `[...]` y se declaró un objeto, o al revés |
| Campos siempre `null` | El nombre del campo en Kotlin no coincide con el del JSON |
| `baseUrl must end in /` | Falta la `/` final en `BASE_URL` |
| No conecta a un backend local | Desde el emulador, `localhost` del PC es `http://10.0.2.2:PUERTO/` |

## Ejercicios propuestos
1. Agregar un endpoint `lookup.php?i={id}` y una pantalla de detalle con las instrucciones.
2. Cambiar la búsqueda para listar por categoría (`filter.php?c=Seafood`).
3. Agregar `HttpLoggingInterceptor` (OkHttp) para ver las peticiones en Logcat.
4. Reemplazar TheMealDB por el backend propio usando `ProductoApiService` como base.
