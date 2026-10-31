<<<<<<< HEAD
# Que Cocino App — versión nativa (Kotlin + Jetpack Compose)

Migración del proyecto Ionic **EjComidas** a Android nativo.

**Stack:** Kotlin · Jetpack Compose (Material 3) · MVVM · Retrofit + Gson · Room (SQLite) con KSP · Navigation Compose · Coil

## Cómo abrirlo
1. Descomprime el zip y en Android Studio usa **File → Open** sobre la carpeta `EjComidasNativo`.
2. Espera a que termine el *Gradle Sync* (descarga las dependencias la primera vez).
3. Ejecuta en un emulador o dispositivo con internet.

> Si Android Studio ofrece actualizar AGP o las dependencias, puedes aceptar, pero no es necesario.

## Estructura (MVVM)
```
cl.duoc.ejcomidas
├── EjComidasApp.kt            → crea el repositorio una vez (inyección manual)
├── MainActivity.kt
├── data
│   ├── model/                 → Categoria, Comida (modelos de dominio)
│   ├── remote/                → MealApiService (Retrofit), RetrofitClient, dto/
│   ├── local/                 → Room: FavoritoEntity, FavoritoDao, AppDatabase
│   └── repository/            → ComidasRepository (API + Room)
├── ui
│   ├── viewmodel/             → CategoriasVM, PorCategoriaVM, FavoritosVM, UiState
│   ├── screens/               → una pantalla por tab
│   ├── components/            → tarjetas y vistas de carga/error
│   ├── navigation/            → barra inferior + NavHost
│   └── theme/                 → colores tomados de variables.scss
└── util/                      → abrir y compartir receta
```

## Equivalencias Ionic → Nativo
| Ionic | Android nativo |
|---|---|
| `comidas.service.ts` (HttpClient) | `MealApiService` + `RetrofitClient` |
| `icomidas.ts` (interfaces) | `dto/MealDbDtos.kt` (JSON) + `model/Modelos.kt` |
| `data-local.service.ts` (Ionic Storage) | Room: `FavoritoEntity` + `FavoritoDao` + `AppDatabase`, usados por `ComidasRepository` |
| `tab1.page` + `comidas`/`comida.component` | `CategoriasScreen` + `CategoriaCard` + `CategoriasViewModel` |
| `tab2.page` (`ion-segment`) + `meals`/`meal.component` | `PorCategoriaScreen` (`ScrollableTabRow`) + `ComidaCard` + `PorCategoriaViewModel` |
| `tab3.page` | `FavoritosScreen` + `FavoritosViewModel` |
| `tabs.page` (`ion-tabs`) | `AppNavigation` (`NavigationBar` + `NavHost`) |
| `ActionSheetController` | `DropdownMenu` del botón ⋮ |
| `InAppBrowser` | `LocalUriHandler.openUri()` |
| `SocialSharing` ("Compartir") | **Compartir en…**: `Intent.ACTION_SEND` + `createChooser` (WhatsApp, Telegram, Bluetooth, etc.) |
| — (nuevo) | **Enviar por email**: `Intent.ACTION_SENDTO` con `mailto:` (solo apps de correo) |
| `ToastController` | `Snackbar` (mensajes desde el ViewModel) |
| `animate.css` | `Modifier.animateItem()` |

## Mejoras respecto al original
- Estados de **carga y error con botón Reintentar** (el original no manejaba errores de red).
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
=======
# Repositorio DSY1105
>>>>>>> 4c1d2032e3f2cde8d46839bae353021cefa98053
