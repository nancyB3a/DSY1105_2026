# Explicación detallada — MisNotesApp: UserCard con Jetpack Compose

Este documento analiza, elemento por elemento, el código de tu primer ejemplo en Jetpack Compose. La idea es que sirva como material de apoyo, mostrando **qué es**, **para qué sirve** y **qué variaciones** son posibles en cada pieza.

---

## 1. Estructura general del archivo

```kotlin
package com.example.ejemplo02

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
...
```

Antes de entrar en el código, vale la pena que noten el cambio de paradigma respecto a la Experiencia 1:

- En la Experiencia 1 trabajaron con **clases** y **funciones** en aplicaciones de consola (POO clásica).
- Ahora, en Compose, la UI se describe con **funciones que devuelven UI** (composables), no con clases que heredan de `View`
- como en el sistema antiguo de Android (XML + `Activity`). Es un paradigma **declarativo**: describes *cómo se ve* la pantalla en función del estado,
- en vez de dar instrucciones paso a paso de *cómo construirla*.

---

## 2. `MainActivity`

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApp()
        }
    }
}
```

### `ComponentActivity`
Es la clase base mínima que provee Android Jetpack para actividades que usan Compose (o combinan Compose con Views). Es más liviana que `AppCompatActivity`, 
aunque esta última también se puede usar si necesitas compatibilidad con componentes del sistema de Views clásico.

- **Relación con la Experiencia 1**: aquí sí hay herencia real de framework (`MainActivity : ComponentActivity()`), muy parecido a como en Kotlin de consola
- heredaban de una clase abierta con `class Hijo : Padre()`.

### `onCreate(savedInstanceState: Bundle?)`
Método del ciclo de vida de la Activity. Se ejecuta cuando el sistema crea la pantalla. `savedInstanceState` permite restaurar el estado si la Activity fue 
destruida y recreada (por ejemplo, al rotar la pantalla).

### `enableEdgeToEdge()`
Función de Jetpack que permite que el contenido de la app se dibuje "de borde a borde", detrás de la barra de estado y la barra de navegación del sistema, 
dando una apariencia más moderna e inmersiva. Es opcional, pero recomendada en apps nuevas.

**Variación**: se puede omitir si se quiere el comportamiento clásico (contenido respetando automáticamente los márgenes del sistema).

### `setContent { ... }`
Es el puente entre el mundo de las Activities (basado en Views) y el mundo Compose. Todo lo que va dentro de este bloque lambda es un **árbol de composables**. 
Aquí es donde "empieza" la UI declarativa.

### `MyApp { ... }`
Es el tema de Material Design generado automáticamente por Android Studio al crear el proyecto (se encuentra en `ui/theme/Theme.kt`). Envuelve toda la UI para 
propagar colores, tipografía y formas (shapes) consistentes a todos los composables hijos, mediante `MaterialTheme`.

**Variación**: se puede personalizar el archivo `Theme.kt` para cambiar la paleta de colores, tipografía, soporte de modo oscuro, etc. 
Todo composable dentro de `Ejemplo02Theme` puede acceder a esos valores vía `MaterialTheme.colorScheme`, `MaterialTheme.typography`, etc.

---

## 3. La anotación `@Composable`

```kotlin
@Composable
fun UserCard(...)
```

Toda función que quiera describir parte de la interfaz debe llevar `@Composable`. Esto le indica al compilador de Compose que:

1. La función puede llamar a otras funciones `@Composable`.
2. Puede "recomponerse" (volver a ejecutarse) automáticamente cuando cambian los datos de los que depende.
3. No devuelve una vista tradicional (`View`), sino que **emite** UI directamente al árbol de composición.

Es el equivalente conceptual, en términos de "bloque reutilizable de UI", a lo que en XML clásico sería un layout reutilizable con `<include>`, 
pero mucho más flexible porque es código Kotlin normal (puede tener lógica, condicionales, bucles, parámetros, valores por defecto, etc.).

---

## 4. `UserCard`: composable reutilizable con parámetros

```kotlin
@Composable
fun UserCard(nombre: String,
             descripcion: String,
             image: Int,
             onFollowClick: () -> Unit){
```

Este es un buen ejemplo para conectar con lo visto en la Experiencia 1:

- Los parámetros `nombre: String`, `descripcion: String`, `image: Int` son igual que los parámetros de cualquier función Kotlin.
- `onFollowClick: () -> Unit` es un **parámetro de tipo función** (higher-order function): recibe una lambda sin argumentos que no devuelve nada.
- Esto es la forma estándar en Compose de manejar **eventos** (clics, etc.), delegando la responsabilidad de "qué hacer" a quien use el composable,
- sin acoplar `UserCard` a una lógica de negocio específica.

**Variación pedagógica**: podrías comparar esto con los parámetros de función que usaron en las corrutinas o en callbacks de la Experiencia 1, 
reforzando que "una función que recibe otra función" no es exclusivo de Compose.

**Otras variaciones posibles**:
- Agregar valores por defecto: `descripcion: String = "Sin descripción"`.
- Usar una **data class** en vez de varios parámetros sueltos: `data class Usuario(val nombre: String, val descripcion: String, val imagen: Int)` y
- luego `UserCard(usuario: Usuario, onFollowClick: () -> Unit)`. Esto conecta directamente con lo que vieron en la Experiencia 1 sobre `data class`.

---

## 5. `Card` y su Modifier

```kotlin
Card(
    elevation = CardDefaults.cardElevation(8.dp),
    modifier = Modifier
        .padding(16.dp)
        .fillMaxWidth()
) { ... }
```

### `Card`
Componente de Material 3 que dibuja un contenedor con fondo, esquinas redondeadas y sombra (elevación), típico para agrupar visualmente información relacionada 
(en este caso, los datos de un usuario).

### `CardDefaults.cardElevation(8.dp)`
`CardDefaults` provee configuraciones por defecto para distintos estados de la `Card` (elevación en reposo, presionada, deshabilitada, etc.). 
Aquí se define una elevación de `8.dp`, lo que se traduce visualmente en una sombra más marcada, dando sensación de "flotar" sobre el fondo.

### `Modifier`
Es uno de los conceptos **más importantes** de Compose. Un `Modifier` es una cadena de instrucciones que decoran o alteran el comportamiento/apariencia de un composable: 
tamaño, padding, márgenes, clics, formas, fondo, etc. Se encadenan con el operador `.` y se aplican en **orden** (el orden importa, porque cada modificador envuelve al 
resultado del anterior).

- `.padding(16.dp)`: agrega espacio interno alrededor del `Card` (16 *density-independent pixels*, unidad estándar en Android para que el tamaño se vea consistente en
- distintas densidades de pantalla).
- `.fillMaxWidth()`: hace que la `Card` ocupe todo el ancho disponible del contenedor padre.

**Variaciones**:
- `.fillMaxWidth(0.9f)` → ocupa el 90% del ancho disponible.
- Cambiar el orden, por ejemplo `.fillMaxWidth().padding(16.dp)` vs `.padding(16.dp).fillMaxWidth()`, puede dar resultados visuales distintos:
- en el primero el padding se aplica después de ocupar todo el ancho del padre (el padding "come" espacio del ancho ya ocupado); es un buen ejercicio para que
- experimenten y comparen.
- Se puede agregar `.clickable { }` al `Modifier` de la `Card` completa para que toda la tarjeta sea clicable, no solo el botón.

---

## 6. `Row`: organización horizontal

```kotlin
Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier.padding(32.dp)
) { ... }
```

`Row` es un contenedor (*layout*) que ubica a sus hijos **uno al lado del otro**, horizontalmente. Es el equivalente conceptual a un `LinearLayout` con 
`orientation="horizontal"` del sistema de Views clásico.

- `verticalAlignment = Alignment.CenterVertically`: alinea verticalmente todos los hijos al centro del `Row` (útil porque la imagen, el texto y el botón
- pueden tener alturas distintas).
- `modifier = Modifier.padding(32.dp)`: espacio interno del `Row` respecto al borde de la `Card`.

**Variaciones**:
- `horizontalArrangement` (no usado aquí) permite controlar la distribución horizontal entre los hijos: `Arrangement.SpaceBetween`, `Arrangement.SpaceEvenly`,
- `Arrangement.Center`, etc.
- Cambiar `Row` por `Column` invertiría la disposición a vertical — buen ejercicio para mostrar el paralelismo entre ambos.

---

## 7. `Image`, `painterResource` y recorte circular

```kotlin
Image(
    painter = painterResource(id = image),
    contentDescription = "Foto de perfil de $nombre",
    modifier = Modifier
        .size(80.dp)
        .clip(CircleShape)
)
```

### `Image`
Composable para mostrar gráficos (drawables, bitmaps, vectores).

### `painterResource(id = image)`
Convierte un recurso de tipo `Int` (el ID generado automáticamente por Android para cada recurso en `res/drawable`, ej. `R.drawable.perfil1`) en un objeto 
`Painter` que `Image` puede dibujar.

### `contentDescription`
**Muy importante para accesibilidad**: es el texto que leerán los lectores de pantalla (TalkBack) para describir la imagen a personas con discapacidad visual. 
Aquí se usa un *template string* de Kotlin (`"Foto de perfil de $nombre"`) para generar una descripción dinámica y significativa — buena práctica que vale la pena 
resaltar, en vez de dejarlo en `null` o con un texto genérico.

### `Modifier.size(80.dp)`
Define un tamaño fijo (ancho y alto iguales) para la imagen: 80dp × 80dp.

### `Modifier.clip(CircleShape)`
Recorta el composable con una forma determinada; en este caso, `CircleShape` lo convierte en un círculo perfecto (funciona porque el tamaño es cuadrado: 80dp × 80dp). 
Es el equivalente visual a poner una imagen de perfil circular, como en la mayoría de redes sociales.

**Variaciones**:
- `RoundedCornerShape(12.dp)` en vez de `CircleShape` → esquinas redondeadas en vez de círculo completo.
- Cambiar `.size(80.dp)` por `.width()` y `.height()` distintos rompería el efecto circular perfecto (quedaría una elipse), buen punto para que entiendan la relación
- entre tamaño y forma de recorte.
- `contentScale = ContentScale.Crop` (parámetro adicional de `Image`) para asegurar que la imagen llene el espacio sin deformarse, recortando el sobrante.

---

## 8. `Spacer`

```kotlin
Spacer(modifier = Modifier.width(18.dp))
```

Composable "invisible" cuyo único propósito es ocupar espacio, muy usado dentro de `Row` o `Column` para separar elementos sin necesidad de márgenes en cada uno. 
Aquí separa la imagen de la columna de texto.

**Variación**: `Modifier.height(18.dp)` se usaría dentro de un `Column` en vez de un `Row`, ya que ahí interesa separar verticalmente.

---

## 9. `Column` con `weight`

```kotlin
Column(
    modifier = Modifier.weight(1f)
) {
    Text(text = nombre, style = MaterialTheme.typography.titleMedium)
    Text(text = descripcion, style = MaterialTheme.typography.bodyMedium)
}
```

### `Column`
Organiza a sus hijos verticalmente (aquí, el nombre arriba y la descripción abajo).

### `Modifier.weight(1f)`
**Solo tiene efecto dentro de un `Row` (o `Column`, según corresponda)**. Le indica a este hijo que debe ocupar todo el espacio horizontal restante dentro del `Row`, 
después de que la imagen, los `Spacer` y el botón ya tomaron el espacio que necesitan. Esto es clave para que el botón "Seguir" no quede empujado fuera de la pantalla y 
el texto no se desborde: el `weight` fuerza a que el texto se ajuste (haciendo *wrap* o truncando) al espacio disponible.

**Variación**: si hubiera dos elementos con `weight(1f)` cada uno dentro del mismo `Row`, ambos ocuparían partes iguales del espacio restante. Con `weight(2f)` y 
`weight(1f)`, el primero ocuparía el doble de espacio que el segundo — buen ejercicio numérico para mostrar cómo se reparte proporcionalmente.

### `Text` y `MaterialTheme.typography`
`Text` es el composable básico para mostrar texto. En vez de definir tamaños de fuente "a mano", se usa la escala tipográfica del tema (`MaterialTheme.typography`), 
que ya trae estilos predefinidos y coherentes con Material Design 3: `titleMedium`, `bodyMedium`, `headlineSmall`, `labelLarge`, etc. Esto asegura consistencia visual 
en toda la app y facilita el soporte de temas (claro/oscuro, tipografías personalizadas).

**Variación**: se pueden sobrescribir propiedades puntuales, por ejemplo:
```kotlin
Text(
    text = nombre,
    style = MaterialTheme.typography.titleMedium,
    color = MaterialTheme.colorScheme.primary,
    maxLines = 1
)
```

---

## 10. `Button` y manejo de eventos

```kotlin
Button(onClick = onFollowClick) {
    Text("Seguir")
}
```

`Button` es un composable que dibuja un botón de Material Design y expone un parámetro `onClick: () -> Unit`. Aquí simplemente se le pasa la lambda `onFollowClick` que 
`UserCard` recibió como parámetro — es decir, **`UserCard` no decide qué pasa al presionar "Seguir"**, solo delega esa decisión a quien lo use (en este caso, `MainScreen`). 
Este patrón (elevar el evento hacia arriba) es central en Compose y se conoce como **"state hoisting"** cuando se aplica también al estado, no solo a los eventos.

El contenido del `Button` (`{ Text("Seguir") }`) es otro ejemplo de **lambda con receptor**: `Button` internamente define un `RowScope` para su contenido, permitiendo poner 
texto, íconos, o ambos dentro.

**Variaciones**:
- `OutlinedButton`, `TextButton`, `ElevatedButton` → mismas ideas de Material 3 con distintos estilos visuales.
- Agregar un ícono: `Button(onClick = onFollowClick) { Icon(...); Spacer(...); Text("Seguir") }`.
- Deshabilitar el botón condicionalmente: `Button(onClick = onFollowClick, enabled = condicion) { ... }`.

---

## 11. `MainScreen`: composición y reutilización

```kotlin
@Composable
fun MainScreen(){
    Column {
        UserCard(
            nombre = "Penélope",
            descripcion = "Docente de TI y entusiasta del Desarrollo Móvil",
            image = R.drawable.perfil1,
            onFollowClick = { }
        )
        UserCard(
            nombre = "Paola",
            descripcion = "Diseñador UX | Innovadora Digital",
            image = R.drawable.perfil2,
            onFollowClick = { }
        )
    }
}
```

Este composable demuestra el valor central de Compose: **componer pantallas combinando piezas reutilizables**. `MainScreen` no sabe (ni le importa) cómo está construida 
internamente una `UserCard`; solo la usa dos veces con datos distintos, tal como reutilizarían una función o clase de la Experiencia 1.

`R.drawable.perfil1` y `R.drawable.perfil2` son referencias a imágenes ubicadas en `res/drawable`, generadas automáticamente por el sistema de recursos de Android (clase `R`).

**Variación clave para mostrar escalabilidad**: en vez de llamar a `UserCard` de forma manual y repetitiva, se podría usar una **lista de datos** (por ejemplo, una 
`List<Usuario>` con la data class mencionada antes) y recorrerla con un bucle `forEach` dentro del `Column`, o mejor aún, usar `LazyColumn` (la versión "perezosa"/eficiente 
de `Column`, ideal cuando la lista puede ser larga):

```kotlin
LazyColumn {
    items(listaUsuarios) { usuario ->
        UserCard(usuario = usuario, onFollowClick = { })
    }
}
```

Esto conecta muy bien con lo que vieron en manejo de colecciones en Kotlin durante la Experiencia 1.

---

## 12. `@Preview`

```kotlin
@Preview(showBackground = true)
@Composable
fun PreviewMainScreeen() {
    MainScreen()
}
```

`@Preview` permite visualizar un composable directamente en el panel de diseño de Android Studio **sin necesidad de ejecutar la app en un emulador o dispositivo físico**. 
Es una de las grandes ventajas de Compose para iterar rápido sobre la UI.

- `showBackground = true`: agrega un fondo (blanco por defecto) al preview, para que no se vea transparente.

**Variaciones útiles para mostrar a tus estudiantes**:
```kotlin
@Preview(showBackground = true, name = "Modo claro")
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Modo oscuro")
@Preview(showBackground = true, widthDp = 320, name = "Pantalla pequeña")
```
Se pueden tener **múltiples previews** de la misma función, cada una con configuraciones distintas (modo oscuro, distintos tamaños de pantalla, distintos idiomas), 
todo sin ejecutar la app.

---

## 13. Ideas para reforzar

1. **De POO clásica a Compose declarativo**: la Experiencia 1 trabajó clases con estado mutable interno; en Compose, la UI es función del estado (`UI = f(estado)`),
y todavía no se ha introducido `remember`/`mutableStateOf` en este ejemplo — buen gancho para la siguiente clase sobre estado y recomposición.
2. **Reutilización sin herencia**: noten que `UserCard` no hereda de nada — se reutiliza por composición de funciones, no por herencia de clases, a diferencia de lo que
   vieron con herencia en Kotlin puro.
3. **Data class + Compose**: excelente puente para reforzar `data class` (visto en la Experiencia 1) refactorizando los parámetros sueltos de `UserCard` en un objeto
`Usuario`.
---

*Documento de apoyo para la Experiencia de Aprendizaje N°2 — Desarrollo Móvil con Kotlin y Jetpack Compose.*
