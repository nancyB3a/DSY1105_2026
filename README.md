# Dependencias para integrar Room (SQLite) en Jetpack Compose

Este documento explica, una por una, las piezas que se agregaran a `build.gradle.kts` y `libs.versions.toml` para que el proyecto pueda usar **Room** (la librería de Android para trabajar con SQLite de forma segura y sin SQL manual repetitivo).

---

## 1. El plugin KSP — la pieza que casi nadie explica bien

```kotlin
// build.gradle.kts (raíz)
plugins {
    alias(libs.plugins.ksp) apply false
}

// app/build.gradle.kts
plugins {
    alias(libs.plugins.ksp)
}
```

```toml
# libs.versions.toml
[versions]
ksp = "2.0.21-1.0.28"

[plugins]
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
```

### ¿Qué es KSP y por qué Room lo necesita?

Room funciona gracias a **generación de código en tiempo de compilación**: tú escribes una interfaz con anotaciones (`@Dao`, `@Query`, etc.) y, antes de que tu app se compile a bytecode, una herramienta externa **lee esas anotaciones y escribe por ti** la clase real que ejecuta el SQL. Tú nunca ves ese código generado a mano, pero existe (se puede inspeccionar en `app/build/generated/ksp/`).

**KSP (Kotlin Symbol Processing)** es el motor que hace esa lectura y generación. Es el reemplazo moderno de una herramienta más antigua llamada **kapt** (Kotlin Annotation Processing Tool):

| | kapt (antiguo) | KSP (actual) |
|---|---|---|
| Cómo entiende tu código | Lo convierte primero a "stubs" estilo Java | Lee directamente la estructura de Kotlin |
| Velocidad de compilación | Más lento | 2x a 3x más rápido en general |
| Quién lo recomienda | — | JetBrains y Google, para todo proyecto nuevo |

### ¿Por qué la versión "2.0.21-1.0.28" tiene ese formato tan raro?

No es un error de tipeo. El formato es: `[versión de Kotlin]-[versión interna de KSP]`. Esto es porque KSP está muy acoplado al compilador de Kotlin — necesita una versión de KSP compilada específicamente contra la misma versión de Kotlin que usa tu proyecto. Si tu proyecto usa Kotlin `2.0.21` (revisa `libs.versions.toml`, clave `kotlin`), el plugin KSP **debe** empezar con `2.0.21-`, o Gradle fallará al sincronizar.

> 💡 **Importante para tus estudiantes**: si en el futuro actualizan Kotlin (por ejemplo a 2.1.0), **tienen que actualizar también la versión de KSP** a una que empiece con `2.1.0-`. Son dos números que viajan juntos, no se actualizan por separado.

---

## 2. Las 3 dependencias de Room

```kotlin
// app/build.gradle.kts
dependencies {
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
}
```

```toml
# libs.versions.toml
[versions]
room = "2.6.1"

[libraries]
androidx-room-runtime  = { group = "androidx.room", name = "room-runtime",  version.ref = "room" }
androidx-room-ktx      = { group = "androidx.room", name = "room-ktx",      version.ref = "room" }
androidx-room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }
```

Room en realidad **no es una sola librería**, son tres piezas con responsabilidades distintas:

### a) `room-runtime` — el motor en tiempo de ejecución
Contiene las clases reales que corren **dentro del teléfono** cuando la app ya está instalada: `RoomDatabase`, el builder (`Room.databaseBuilder(...)`), el manejo interno de SQLite, etc. Sin esta dependencia, ni siquiera existirían las clases `@Database` o `RoomDatabase` para heredar.

### b) `room-ktx` — las extensiones para Kotlin
Agrega soporte para que los métodos de los DAO puedan ser funciones `suspend` (corrutinas) y, si se quisiera, devolver `Flow<T>` para observar cambios en tiempo real. **Sin esta dependencia, los DAO tendrían que ser 100% síncronos** (bloqueando el hilo que los llame), algo que en Android nunca se debe hacer en el hilo principal.

### c) `room-compiler` — el generador de código (usado distinto a los otros dos)
Fíjate que este NO se agrega con `implementation(...)` sino con:
```kotlin
ksp(libs.androidx.room.compiler)
```
La palabra clave `ksp(...)` (en vez de `implementation(...)`) le dice a Gradle: *"esta dependencia no va dentro de mi app final, solo se usa durante la compilación para generar código"*. Es `room-compiler` quien efectivamente lee tus interfaces `@Dao` y genera las clases reales — y para hacerlo necesita el plugin KSP que agregamos en el paso 1. Por eso **KSP y `room-compiler` siempre van juntos**: uno es el motor genérico de generación de código, el otro es el "programa" específico de Room que corre sobre ese motor.

> Si alguna vez un estudiante agrega `room-compiler` con `implementation(...)` en vez de `ksp(...)` por error, el proyecto compilará pero Room **no generará ningún código**, y les aparecerán errores como "Cannot find implementation for AppDatabase" al ejecutar la app.

---

## 3. Cómo se conecta todo esto con el código Kotlin

Un resumen visual de qué dependencia habilita qué anotación/clase:

| Dependencia | Qué te da |
|---|---|
| `room-runtime` | `@Database`, `RoomDatabase`, `Room.databaseBuilder(...)` |
| `room-ktx` | Que tus funciones de DAO puedan ser `suspend fun` |
| `room-compiler` (+ KSP) | Que `@Entity`, `@Dao`, `@Insert`, `@Query`, `@Delete` realmente **hagan algo** en vez de ser solo anotaciones decorativas |

```kotlin
@Entity(tableName = "notas")           // ← viene de room-runtime
data class NotaEntity(...)

@Dao                                    // ← viene de room-runtime
interface NotaDao {
    @Insert                             // ← anotación de room-runtime,
    suspend fun insertar(nota: NotaEntity): Long   // "suspend" habilitado por room-ktx,
                                         //   implementación generada por room-compiler/KSP
}
```

---

## 4. Checklist para asegurarse de que "les funcione a la primera"

1. Agregar el plugin `ksp` tanto en el `build.gradle.kts` raíz (con `apply false`) como en el de `app` (sin `apply false`).
2. Verificar que la versión de KSP **empiece exactamente igual** que la versión de Kotlin del proyecto (revisar `libs.versions.toml`).
3. Agregar las 3 dependencias de Room: `room-runtime` e `room-ktx` con `implementation(...)`, y `room-compiler` con `ksp(...)` — nunca con `implementation(...)`.
4. Sincronizar el proyecto (**File > Sync Project with Gradle Files**). La primera sincronización después de agregar KSP suele tardar un poco más de lo normal, es esperable.
5. Si Android Studio no reconoce `@Entity`, `@Dao`, etc. después de sincronizar: **File > Invalidate Caches / Restart**, igual que con cualquier otra dependencia nueva.

---

*Material de apoyo — DSY1105, Desarrollo de Aplicaciones Móviles.*
