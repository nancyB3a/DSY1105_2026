# 📱 Desarrollo Móvil — Resumen de Kotlin: POO, Corrutinas y Modelado de Estados

Este documento resume los temas vistos hasta ahora en el curso, con ejemplos prácticos usando un dominio común: **automóviles**.

## 📚 Contenidos

1. [Herencia y Polimorfismo](#1-herencia-y-polimorfismo)
2. [Funciones de Ámbito (`apply`, `also`, `let`, `run`, `with`)](#2-funciones-de-ámbito)
3. [Corrutinas (Asincronía Simple)](#3-corrutinas-asincronía-simple)
4. [Data Classes (Modelo Inmutable)](#4-data-classes-modelo-inmutable)
5. [Sealed Classes (Estados Exhaustivos)](#5-sealed-classes-estados-exhaustivos)
6. [Ejemplo Integrado](#6-ejemplo-integrado)
7. [Ejercicios Propuestos](#7-ejercicios-propuestos)

---

## 1) Herencia y Polimorfismo

Una clase marcada con `open` puede heredarse y sus métodos pueden sobrescribirse con `override`.

```kotlin
open class Automovil(val marca: String, val modelo: String) {
    open fun describir() = println("Automóvil $marca $modelo.")
}

class Deportivo(marca: String, modelo: String, private val velocidadMaxima: Int) :
    Automovil(marca, modelo) {

    override fun describir() =
        println("Deportivo $marca $modelo (máx: $velocidadMaxima km/h).")
}

fun main() {
    val a: Automovil = Automovil("Toyota", "Corolla")
    val b: Automovil = Deportivo("Ferrari", "488 GTB", 330) // polimorfismo

    a.describir()  // -> Automóvil Toyota Corolla.
    b.describir()  // -> Deportivo Ferrari 488 GTB (máx: 330 km/h).
}
```

**Ideas clave:**
- `open` permite heredar y sobrescribir.
- El **polimorfismo** hace que la misma llamada (`describir()`) se comporte distinto según el tipo real del objeto.

---

## 2) Funciones de Ámbito

Las funciones de ámbito (*scope functions*) ejecutan un bloque de código dentro del contexto de un objeto, haciendo el código más conciso y seguro.

| Función | Contexto (`this`/`it`) | Retorna | Uso común |
|---|---|---|---|
| `let` | `it` | Resultado del bloque | Operar sobre el resultado de una expresión / evitar nulls |
| `run` | `this` | Resultado del bloque | Ejecutar varias operaciones y retornar un valor |
| `with` | `this` | Resultado del bloque | Similar a `run`, pero no es una extensión |
| `apply` | `this` | El objeto receptor | Inicialización/configuración de objetos |
| `also` | `it` | El objeto receptor | Efectos secundarios (logging, etc.) sin romper el encadenamiento |

### 2.1) Ejemplos rápidos

```kotlin
fun main() {
    // let: evitar nulls y transformar
    val nombre: String? = "Nancy"
    print("Ejecución -> let -> ")
    nombre?.let { println("El nombre tiene ${it.length} caracteres") }
    
    // run: ejecutar y retornar un valor
    val resultado = "Kotlin".run { length + 10 }
    println("Ejecución -> run -> $resultado")//16
    
    // with: operar sobre un objeto existente
    val lista = mutableListOf("A", "B", "C")
    val total = with(lista) { add("D"); size } // 4
    println("Ejecución -> with -> $total")
    
    // apply: configurar un objeto (retorna el mismo objeto)
    val numero = 10.apply {
        println("Ejecución -> apply -> $this")
    }
    
    // also: efecto secundario (retorna el mismo objeto)
    print("Ejecución -> also -> ")
    val lista2 = mutableListOf("A", "B").also { print("Original: $it"); it.add("C") }
    print(" - Modificada: $lista2")
}
```

### 2.2) Encadenando `apply`, `also` y `let`

```kotlin
class Taller {
    fun recibir(auto: Automovil) = println("Taller recibe: ${auto.marca} ${auto.modelo}")
}

fun main() {
    val taller = Taller()

    val ferrari = Deportivo("Ferrari", "488 GTB", 330)
        .apply { describir() }                              // configurar/usar el objeto
        .also { println("LOG -> Se creó: ${it.marca} ${it.modelo}") } // efecto secundario

    val mensaje = ferrari.let { "Listo para pista: ${it.marca} ${it.modelo}" }
    println(mensaje)

    taller.recibir(ferrari)
}
```

**Cuándo usar cada una:**
- `apply` → inicialización fluida (retorna el mismo objeto).
- `also` → efectos secundarios sin romper el encadenamiento (retorna el mismo objeto).
- `let` → transforma el valor (retorna el resultado del bloque).

---

## 3) Corrutinas (Asincronía Simple)

Las corrutinas permiten manejar operaciones asíncronas y concurrentes **sin bloquear el hilo principal**, escribiendo código que parece secuencial.

**Conceptos clave:**
- **Suspensión**: una función `suspend` puede pausar su ejecución sin bloquear el hilo.
- **Reanudación**: la corrutina continúa donde se suspendió cuando la operación termina.
- **Scope**: dónde se lanza la corrutina (`GlobalScope`, `CoroutineScope`, `viewModelScope` en Android).
- **Dispatcher**: en qué hilo se ejecuta (`Dispatchers.IO`, `Dispatchers.Main`, `Dispatchers.Default`).

### Dependencia (Maven)

```kotlin
<dependency>
    <groupId>org.jetbrains.kotlinx</groupId>
    <artifactId>kotlinx-coroutines-core</artifactId>
    <version>última versión</version>
</dependency>
```

### 3.1) Ejemplo básico

```kotlin
import kotlinx.coroutines.*

fun main() = runBlocking {
    launch {
        delay(1000L)
        println("¡Hola desde la corrutina!")
    }
    println("Hola desde el hilo principal")
}
```

### Builders principales

- `launch`: no devuelve resultado; ideal para tareas "fire and forget".
- `async`: devuelve un `Deferred`; se obtiene el resultado con `.await()`.
- `runBlocking`: bloquea el hilo actual; útil para demos, pruebas o `main`.

### 3.2) Ejemplo con herencia y `suspend`

```kotlin
import kotlinx.coroutines.*

open class AutomovilV2(val marca: String, val modelo: String) {
    open suspend fun arrancar() {
        println("[$marca $modelo] Arrancando...")
        delay(4000) // simula trabajo no bloqueante
        println("[$marca $modelo] Motor encendido.")
    }
}

class DeportivoV2(marca: String, modelo: String, private val velocidadMaxima: Int) :
    AutomovilV2(marca, modelo) {

    suspend fun acelerarHastaObjetivo(objetivo: Int) {
        require(objetivo in 1..velocidadMaxima) { "Objetivo inválido" }
        println("Acelerando a $objetivo km/h...")
        delay(6000)
        println("¡Objetivo alcanzado!")
    }

    override suspend fun arrancar() {
        println("[$marca $modelo] Secuencia sport...")
        delay(2000)
        super.arrancar()
    }
}

fun main() = runBlocking {
    val ferrari = DeportivoV2("Ferrari", "488 GTB", 330)

    // Structured concurrency: todo vive dentro de runBlocking
    launch { ferrari.arrancar() }.join()
    launch { ferrari.acelerarHastaObjetivo(330) }.join()
}
```

**Ideas clave:**
- `suspend` + `delay()` simulan operaciones que toman tiempo sin bloquear el hilo.
- `runBlocking` es para demos/CLI; en apps reales se usa `viewModelScope`, `lifecycleScope`, etc.

---

## 4) Data Classes (Modelo Inmutable)

Una `data class` está diseñada para almacenar datos. Kotlin genera automáticamente:

- `equals()` y `hashCode()` — comparar objetos.
- `toString()` — representación legible.
- `copy()` — copia modificada del objeto.
- Desestructuración (`component1()`, `component2()`, ...).

### 4.1) Sintaxis Data Class

```kotlin
data class Persona(val nombre: String, val edad: Int)

val nancy = Persona("Nancy", 30)
println(nancy) // Persona(nombre=Nancy, edad=30)

val copia = nancy.copy(edad = 31)
println(copia) // Persona(nombre=Nancy, edad=31)

val (nombre, edad) = nancy
println("Nombre: $nombre, Edad: $edad")
```

### Reglas

- Debe tener al menos un parámetro en el constructor primario.
- Todos los parámetros deben ser `val` o `var`.
- No puede ser `abstract`, `open`, `sealed` ni `inner`.

### 4.2) Ejemplo de dominio

```kotlin
data class Mantenimiento(
    val fecha: String,
    val descripcion: String,
    val costo: Double
)

fun main() {
    val registro = Mantenimiento("2025-08-24", "Cambio de aceite", 150.0)
    println(registro)

    val registroConDescuento = registro.copy(costo = 120.0)
    println(registroConDescuento)
}
```

**¿Cuándo usarla?** Modelos de datos (Retrofit, Room), transferencia entre capas, estados y eventos.

---

## 5) Sealed Classes (Estados Exhaustivos)

Una **clase sellada** tiene un conjunto de subclases cerrado y conocido en tiempo de compilación: solo se pueden declarar en el mismo archivo. Sirve para modelar un conjunto limitado de variantes (estados, resultados, eventos).

**Propiedades clave:**
- No se puede instanciar directamente (abstracta implícitamente).
- Sus subclases deben estar en el mismo archivo.
- Habilita `when` **exhaustivo** sin necesidad de `else`.
- Las subclases pueden ser `object`, `class` o `data class`.

### 5.1) Ejemplo simple

```kotlin
sealed class Estado {
    object Apagado : Estado()
    object Encendido : Estado()
    data class EnMovimiento(val velocidad: Int) : Estado()
}

fun mensaje(estado: Estado) = when (estado) {
    Estado.Apagado -> "Auto apagado"
    Estado.Encendido -> "Auto encendido"
    is Estado.EnMovimiento -> "Velocidad: ${estado.velocidad}"
    // no es necesario 'else' porque cubrimos todas las variantes
}

fun main() {
    println(mensaje(Estado.Apagado))
    println(mensaje(Estado.Encendido))
    println(mensaje(Estado.EnMovimiento(30)))
}
```

### 5.2) Ejemplo práctico: éxito/error

```kotlin
sealed class Resultado<out T> {
    data class Exito<T>(val data: T) : Resultado<T>()
    data class Error(val mensaje: String) : Resultado<Nothing>()
}

fun <T> procesar(r: Resultado<T>) = when (r) {
    is Resultado.Exito -> println("OK: ${r.data}")
    is Resultado.Error -> println("Fallo: ${r.mensaje}")
}

fun main() {
    procesar(Resultado.Exito(200))
    procesar(Resultado.Error("Fallo de exito"))
}
```

### `sealed class` vs `enum class`

| Característica | `enum class` 🚦 | `sealed class` 🚦 |
|---|---|---|
| Conjunto fijo de valores | ✅ Sí | ✅ Sí (herencia limitada) |
| Sencillez | ✅ Más simple | ❌ Un poco más verboso |
| Permite almacenar datos | ⚠️ Limitado | ✅ Flexible (objetos, data classes con campos) |
| Jerarquía de tipos | ❌ No | ✅ Sí (polimorfismo) |
| Ideal para... | Estados simples (días, colores) | Estados complejos con datos (respuestas de servidor, resultados) |

**Regla práctica:** si solo necesitas constantes simples → `enum class`. Si cada estado necesita datos o comportamiento distinto → `sealed class`.

### Buenas prácticas

- Declara las subclases en el mismo archivo.
- Usa `object` para variantes sin datos (singleton).
- Usa `data class` para variantes que llevan información.
- Considera `sealed interface` para mayor flexibilidad (Kotlin moderno).

---

## 6) Ejemplo Integrado

Programa que combina **herencia**, **funciones de ámbito**, **corrutinas**, **data class** y **sealed class**:

```kotlin
import kotlinx.coroutines.*

// --- Dominio ---
open class AutomovilVF(val marca: String, val modelo: String) {
    open fun describir() = println("Automóvil $marca $modelo.")

    open suspend fun arrancar() {
        println("[$marca $modelo] Arrancando...")
        delay(300)
        println("[$marca $modelo] Motor encendido.")
    }
}

class DeportivoVF(marca: String, modelo: String, private val velocidadMaxima: Int) :
    AutomovilVF(marca, modelo) {

    override fun describir() =
        println("Deportivo $marca $modelo (máx: $velocidadMaxima km/h).")

    override suspend fun arrancar() {
        println("[$marca $modelo] Modo sport activado.")
        delay(200)
        super.arrancar()
    }

    suspend fun acelerarHasta(vel: Int) {
        require(vel in 1..velocidadMaxima) { "Velocidad objetivo fuera de rango" }
        println("[$marca $modelo] Acelerando a $vel km/h...")
        delay(500)
        println("[$marca $modelo] Velocidad estabilizada en $vel km/h.")
    }
}

// --- Estados (sealed) ---
sealed class EstadoAutomovil {
    data object Apagado : EstadoAutomovil()
    data object Encendido : EstadoAutomovil()
    data class EnMovimiento(val velocidad: Int) : EstadoAutomovil()
}

fun mostrarEstado(estado: EstadoAutomovil) = when (estado) {
    EstadoAutomovil.Apagado      -> println("Estado: Apagado")
    EstadoAutomovil.Encendido    -> println("Estado: Encendido")
    is EstadoAutomovil.EnMovimiento -> println("Estado: En movimiento (${estado.velocidad} km/h)")
}

// --- Registro (data class) ---
data class MantenimientoVF(val fecha: String, val descripcion: String, val costo: Double)

// --- Taller de servicio ---
class TallerVF {
    fun recibir(auto: AutomovilVF) = println("Taller recibe: ${auto.marca} ${auto.modelo}")
    fun registrar(m: MantenimientoVF) = println("Registro mantenimiento -> $m")
}

fun main() = runBlocking {
    val taller = TallerVF()

    val ferrari = DeportivoVF("Ferrari", "488 GTB", 330)
        .apply { describir() }
        .also { println("LOG: creado ${it.marca} ${it.modelo}") }

    taller.recibir(ferrari)

    ferrari.arrancar()
    ferrari.acelerarHasta(280)

    listOf(
        EstadoAutomovil.Encendido,
        EstadoAutomovil.EnMovimiento(120),
        EstadoAutomovil.Apagado
    ).forEach(::mostrarEstado)

    val mantBase = MantenimientoVF("2025-08-24", "Cambio de aceite", 150.0)
    val mantPromo = mantBase.copy(costo = mantBase.costo * 0.8) // 20% off

    val resumen = mantPromo.let { "Mantenimiento: ${it.descripcion} por $${it.costo} el ${it.fecha}" }
    println(resumen)
    taller.registrar(mantPromo)
}
```

### 📝 Resumen de aprendizaje

- **Herencia/polimorfismo**: `open`/`override` y comportamiento que cambia según el tipo real.
- **Funciones de ámbito**: `apply`/`also`/`let` hacen el código más expresivo y seguro.
- **Corrutinas**: `suspend`, `delay`, `runBlocking`/`launch` para asincronía sin bloquear.
- **Data class**: modelos inmutables con utilidades (`copy`, `toString`, `equals`).
- **Sealed class**: estados exhaustivos y `when` seguro en tiempo de compilación.

---

## 7) Ejercicios Propuestos

### Ejercicio 1 — Estado del semáforo

Crea una `sealed class Semaforo` con `Rojo`, `Amarillo` y `Verde` (todos `object`). Implementa `accion(semaforo: Semaforo)` que imprima "Detente", "Precaución" o "Avanza" según corresponda.

<details>
<summary>✅ Ver solución</summary>

```kotlin
sealed class Semaforo {
    object Rojo : Semaforo()
    object Amarillo : Semaforo()
    object Verde : Semaforo()
}

fun accion(semaforo: Semaforo) = when (semaforo) {
    Semaforo.Rojo -> println("Detente")
    Semaforo.Amarillo -> println("Precaución")
    Semaforo.Verde -> println("Avanza")
}
```
</details>

### Ejercicio 2 — Resultado de un examen

Crea `sealed class ResultadoExamen` con `Aprobado(nota: Int)` y `Reprobado(nota: Int)`. Muestra el mensaje correspondiente según el resultado.

<details>
<summary>✅ Ver solución</summary>

```kotlin
sealed class ResultadoExamen {
    data class Aprobado(val nota: Int) : ResultadoExamen()
    data class Reprobado(val nota: Int) : ResultadoExamen()
}

fun mostrarResultado(r: ResultadoExamen) = when (r) {
    is ResultadoExamen.Aprobado -> println("Felicidades, aprobaste con ${r.nota}")
    is ResultadoExamen.Reprobado -> println("Lo siento, reprobaste con ${r.nota}")
}
```
</details>

### Ejercicio 3 — Respuesta de un servidor

Crea `sealed class RespuestaServidor` con `Exito(datos: String)`, `Error(codigo: Int, mensaje: String)` y `Cargando` (object). Implementa una función que muestre un mensaje distinto para cada caso.

<details>
<summary>✅ Ver solución</summary>

```kotlin
sealed class RespuestaServidor {
    data class Exito(val datos: String) : RespuestaServidor()
    data class Error(val codigo: Int, val mensaje: String) : RespuestaServidor()
    object Cargando : RespuestaServidor()
}

fun manejarRespuesta(r: RespuestaServidor) = when (r) {
    is RespuestaServidor.Exito -> println("Datos recibidos: ${r.datos}")
    is RespuestaServidor.Error -> println("Error ${r.codigo}: ${r.mensaje}")
    RespuestaServidor.Cargando -> println("Cargando, por favor espera...")
}
```
</details>

Estos tres ejercicios cubren: estados simples sin datos (`object`), variantes con datos (`data class`) y una mezcla de ambos.

---

## 🚗 7.1) Caso guiado adicional: `Vehiculo` + `Auto` + `Conductor`

Como práctica complementaria, se desarrolló paso a paso otro ejemplo con la misma lógica:

1. **Herencia**: `Vehiculo` (base) y `Auto` (hija) que sobrescribe `presentarse()`.
2. **Data class**: `Conductor(nombre, licencia)`.
3. **Sealed class**: `EstadoVehiculo` con `Disponible`, `EnUso` y `EnMantenimiento(motivo)`.
4. **Funciones de ámbito**: `apply` para configurar el `Auto`, `also` para loguear al `Conductor`.
5. **Corrutinas**: una `launch` cambia el estado del vehículo con `delay()` mientras el hilo principal sigue respondiendo, demostrando que **las corrutinas no bloquean el flujo del programa**.

Este ejemplo refuerza que todos los conceptos (POO, data/sealed classes, scope functions y corrutinas) trabajan juntos de forma natural al modelar un dominio real.

<details>
<summary>✅ Ver solución</summary>

```kotlin
import kotlinx.coroutines.*

// Clase base
open class Vehiculo(val marca: String, val modelo: String) {    open fun presentarse() {
    println("Soy un vehículo de marca $marca, modelo $modelo.")
}
}

// Clase derivada
class Auto(marca: String, modelo: String, val tipo: String) : Vehiculo(marca, modelo) {
    override fun presentarse() {
        println("Soy un auto $tipo de marca $marca, modelo $modelo.")
    }
}

// Data class
data class Conductor(val nombre: String, val licencia: String)

// Sealed class (estados posibles del vehículo)
sealed class EstadoVehiculo {
    object Disponible : EstadoVehiculo()
    object EnUso : EstadoVehiculo()
    data class EnMantenimiento(val motivo: String) : EstadoVehiculo()

    override fun toString(): String =
        when (this) {
            Disponible -> "Disponible"
            EnUso -> "En uso"
            is EnMantenimiento -> "En mantenimiento: $motivo"
        }
}
// Main con corrutinas y funciones de ámbito
fun main() = runBlocking {
    val auto = Auto("Toyota", "Corolla", "Sedán").apply {
        presentarse()
    }

    val conductor = Conductor("Juan", "B12345").also {
        println("Conductor asignado: $it")
    }

    var estado: EstadoVehiculo = EstadoVehiculo.Disponible
    println("➡️ Estado inicial: $estado")

    // Corrutina que simula el uso del auto
    launch {
        estado = EstadoVehiculo.EnUso
        println("🚗 Estado cambiado a: $estado")
        delay(2000) // Simula que el auto está en uso
        estado = EstadoVehiculo.EnMantenimiento("Cambio de aceite")
        println("🛠️ Estado cambiado a: $estado")
        delay(2000) // Simula tiempo en taller
        estado = EstadoVehiculo.Disponible
        println("✅ Estado cambiado a: $estado")
    }

    println("⌛ Mientras tanto, el sistema sigue respondiendo...")
    delay(5000) // Esperamos que todas las corrutinas terminen
    println("🏁 Simulación finalizada")
}
```
</details>
