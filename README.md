# FastGo Delivery V1 — Sistema de gestión de despachos en Kotlin

Este documento explica **cómo se construyó** la solución **FastGo Delivery V1**, paso a paso, poniendo énfasis en los conceptos de Programación Orientada a Objetos (POO) de Kotlin que se van aplicando en cada etapa.
Además dejo una versión alternativa **FastGo Delivery V2** un poco distinta a lo visto en clases, pero para que puedan revisar otras formas de hacer lo mismo.

## Idea general del sistema

FastGo Delivery recibe una lista de pedidos (bicicleta, moto o auto), cada uno con un código, una distancia y un cliente asociado. Por cada pedido, el sistema calcula el costo según el tipo de repartidor, aplica el descuento del cliente y el IVA, y simula —de forma asíncrona— la comunicación con la app del repartidor antes de confirmar la entrega.

---

## Paso 1 — Modelar los estados del proceso con `sealed class`

**Archivo:** `EstadoDespacho.kt`

Antes de escribir cualquier lógica de negocio, conviene definir **qué estados puede tener** un despacho: `Pendiente`, `EnProceso`, `Entregado` o `Error`.

```kotlin
sealed class EstadoDespacho {
    object Pendiente : EstadoDespacho() { override fun toString() = "Pendiente" }
    object EnProceso : EstadoDespacho() { override fun toString() = "En Proceso" }
    object Entregado : EstadoDespacho() { override fun toString() = "Entregado" }
    data class Error(val mensaje: String) : EstadoDespacho() { override fun toString() = "Error: $mensaje" }
}
```

### Concepto de POO: clases selladas (`sealed class`)

Una `sealed class` es una clase abstracta cuyas subclases están **todas conocidas y declaradas dentro del mismo archivo**. A diferencia de una jerarquía abierta (donde cualquiera podría crear una subclase nueva en otro archivo), una `sealed class` le dice al compilador: "estos son *todos* los casos posibles, no hay más".

Se eligió sobre un `enum` porque `Error` necesita llevar un dato adicional (el `mensaje`), algo que un `enum` tradicional no puede hacer con la misma naturalidad. Y se eligió sobre un simple `String` ("pendiente", "error", etc.) porque un `String` no impide errores de tipeo ni obliga a cubrir todos los casos.

**Ventaja concreta:** en `FastGoDelivery.kt`, la función `mostrarEstado()` usa un `when (estado) { ... }` sobre esta sealed class. Si en el futuro se agrega un quinto estado (por ejemplo, `Cancelado`), el compilador **marcará error** en cualquier `when` que no lo contemple, evitando que se nos olvide manejar el caso nuevo.

---

## Paso 2 — Modelar los pedidos con herencia y polimorfismo

**Archivo:** `Pedido.kt`

El sistema debe reconocer tres tipos de repartidor, cada uno con su propia forma de calcular el costo. En vez de escribir una sola clase con un campo `tipo: String` y un gigante `if/else` por todos lados, se modela con herencia:

```kotlin
open class Pedido(
    val codigo: String,
    val distanciaKm: Double
) {
    open fun calcularCosto(): Double = distanciaKm * 400.0
    open fun detalle(): String = "$codigo - ${distanciaKm} km"
}

class PedidoBicicleta(codigo: String, distanciaKm: Double) : Pedido(codigo, distanciaKm) {
    override fun calcularCosto(): Double {
        if (distanciaKm < 1.0) return 500.0
        return distanciaKm * 300.0
    }
    override fun detalle(): String = "${super.detalle()} - Bicicleta"
}
```

(Y de forma análoga, `PedidoMoto` y `PedidoAuto`, cada una con su propio recargo.)

### Conceptos de POO: herencia, `open`, `override` y polimorfismo

- **`open class`**: en Kotlin, las clases son `final` por defecto (no se pueden heredar). Hay que marcar explícitamente `open` tanto la clase como los métodos que se quieran sobrescribir. Esto es una decisión de diseño de Kotlin: heredar de una clase debe ser una elección consciente, no un accidente.
- **Herencia** (`: Pedido(codigo, distanciaKm)`): cada subclase reutiliza los datos y el comportamiento común de `Pedido`, y le pasa los parámetros al constructor de la superclase.
- **`override`**: cada subclase reemplaza la implementación de `calcularCosto()` y `detalle()` con su propia regla de negocio.
- **Polimorfismo**: en `FastGoDelivery.kt`, cuando se hace `despacho.sumOf { it.calcularCosto() }`, el código **no sabe ni le importa** si cada `it` es una bicicleta, una moto o un auto — simplemente llama a `calcularCosto()` y Kotlin ejecuta automáticamente la versión correcta según el tipo real del objeto en tiempo de ejecución.

**Ventaja concreta:** si mañana aparece un cuarto tipo de repartidor (por ejemplo, un dron), basta con crear `PedidoDron : Pedido(...)` con su propio `calcularCosto()`. No hay que tocar ni una línea de `FastGoDelivery.kt` ni de `Main.kt`.

---

## Paso 3 — Modelar el cliente con `data class`

**Archivo:** `FastGoDelivery.kt`

```kotlin
data class Cliente(
    val tipo: String,
    val descuento: Double
) {
    companion object {
        fun fromString(tipo: String): Cliente? = when (tipo.trim().lowercase()) {
            "nuevo" -> Cliente("Nuevo", 0.0)
            "frecuente" -> Cliente("Frecuente", 0.15)
            "vip" -> Cliente("VIP", 0.40)
            else -> null
        }
    }
}
```

### Conceptos de POO: `data class` y `companion object`

- **`data class`**: se usa para clases cuyo propósito principal es *transportar datos*. El compilador genera automáticamente `equals()`, `hashCode()`, `toString()` y `copy()`, evitando escribir ese código repetitivo a mano.
- **`companion object`**: permite tener una función asociada a la *clase* (`Cliente.fromString(...)`) en vez de a una *instancia*. Es el equivalente en Kotlin a un método estático (`static`) de Java.
- Se usa `when` con `else -> null` como validación: si el texto ingresado no corresponde a ninguno de los tres tipos válidos, `fromString` retorna `null`, y quien lo llama decide qué hacer (por ejemplo, usar un valor por defecto).

**Ventaja concreta:** se logra el mismo objetivo que tendría un `enum` (un conjunto cerrado de tipos válidos), pero con más flexibilidad: `Cliente` es un objeto normal que se puede copiar (`cliente.copy(descuento = 0.5)`) o comparar por su contenido, cosas que un `enum` no ofrece tan naturalmente.

---

## Paso 4 — Construir el gestor de negocio con listas y funciones de orden superior

**Archivo:** `FastGoDelivery.kt`

```kotlin
class FastGoDelivery(private val pedidosDisponibles: List<Pedido>) {
    private val despacho = mutableListOf<Pedido>()

    private fun calcularSubtotal(): Double = despacho.sumOf { it.calcularCosto() }
    fun pedidosAuto(): List<Pedido> = despacho.filter { it is PedidoAuto }
    fun codigosDespacho(): List<String> = despacho.map { it.codigo }
    // ...
}
```

### Conceptos de POO / programación funcional en Kotlin

- **`List` vs `MutableList`**: `pedidosDisponibles` es una `List` (solo lectura desde afuera de la clase) y `despacho` es una `MutableList` privada, que solo la propia clase puede modificar. Esto es **encapsulamiento**: el estado interno del objeto no queda expuesto para que cualquiera lo modifique desde fuera.
- **Funciones de orden superior** (`sumOf`, `filter`, `map`): reciben otra función (una lambda, como `{ it.calcularCosto() }`) como parámetro. Permiten expresar "qué" se quiere hacer con la colección (sumar los costos, quedarse solo con los autos, transformar a códigos) sin escribir manualmente el `for` y el acumulador.

**Ventaja concreta:** el código queda más corto y más fácil de leer en una sola línea, y reduce la posibilidad de errores típicos de los bucles manuales (por ejemplo, olvidar inicializar el acumulador o desbordar el índice).

---

## Paso 5 — Implementar la tarea asíncrona con corrutinas

**Archivo:** `FastGoDelivery.kt` / `Main.kt`

```kotlin
suspend fun procesarDespacho(cliente: Cliente) {
    // ...
    delay(2500) // simula comunicación con la app del repartidor
    // ...
}

fun main() = runBlocking {
    // ...
    sistema.procesarDespacho(cliente)
}
```

### Conceptos de POO / concurrencia en Kotlin

- **`suspend fun`**: marca una función que puede "pausarse" sin bloquear el hilo que la ejecuta. Solo se puede llamar desde otra función `suspend` o desde un bloque de corrutina.
- **`delay(ms)`**: es el equivalente asíncrono de `Thread.sleep()`, pero sin bloquear el hilo — libera el hilo para que pueda hacer otras cosas mientras "espera".
- **`runBlocking { }`**: crea un puente entre el mundo síncrono (`fun main()`, que no es `suspend`) y el mundo asíncrono (funciones `suspend` como `procesarDespacho`). Es la forma estándar de arrancar corrutinas desde un punto de entrada normal.

**Ventaja concreta:** en un sistema real, mientras se espera la respuesta de la app del repartidor (que podría tardar segundos), el programa no queda completamente congelado — podría, por ejemplo, seguir aceptando nuevos pedidos en paralelo. Aquí se simula ese comportamiento de forma controlada, cumpliendo con el requisito de "tarea asíncrona simple utilizando corrutinas".

---

## Paso 6 — Manejo de errores sin detener el programa

**Archivo:** `FastGoDelivery.kt`

```kotlin
fun agregarADespacho(indices: List<Int>): Boolean {
    try {
        for (i in indices) {
            if (i !in 1..pedidosDisponibles.size) {
                throw IllegalArgumentException("Selección fuera del listado de pedidos")
            }
            val pedido = pedidosDisponibles[i - 1]
            if (!codigoValido(pedido.codigo)) {
                throw IllegalArgumentException("Código de pedido inválido: '${pedido.codigo}'")
            }
            despacho.add(pedido)
        }
    } catch (e: Exception) {
        val estado = EstadoDespacho.Error(e.message ?: "Error desconocido")
        mostrarEstado(estado)
        return false
    }
    return true
}
```

### Concepto de POO: manejo de excepciones (`try-catch`)

En vez de dejar que un dato inválido (un código mal formado, un monto negativo) provoque una excepción que **detenga todo el programa**, se usa `try-catch` para capturarla, convertirla en un `EstadoDespacho.Error` y seguir funcionando.

**Ventaja concreta:** el sistema puede procesar 10 pedidos seguidos y, si uno de ellos tiene datos inválidos, solo ese pedido falla — los demás se siguen procesando con normalidad. Esto es justamente el comportamiento "robusto ante errores" que se espera de un sistema que opera de forma continua.

---

## Paso 7 — Integrar todo en `Main.kt`

**Archivo:** `Main.kt`

```kotlin
fun main() = runBlocking {
    val pedidosDisponibles = listOf(
        PedidoBicicleta("DG100001", 0.8),
        PedidoMoto("DG100002", 5.4, horarioPeak = true),
        // ...
    )
    val sistema = FastGoDelivery(pedidosDisponibles)

    for ((indice, cliente) in casosDePrueba) {
        if (sistema.agregarADespacho(listOf(indice))) {
            sistema.procesarDespacho(cliente)
        }
    }
}
```

`Main.kt` es el único lugar donde se **instancian objetos concretos** (`PedidoBicicleta`, `PedidoMoto`, `PedidoAuto`, `Cliente`) y se orquesta el flujo completo. El resto de las clases (`Pedido`, `EstadoDespacho`, `FastGoDelivery`) no conocen los datos de prueba específicos — solo definen comportamiento genérico. Esta separación entre "quién crea los datos" y "quién define el comportamiento" es, en esencia, el objetivo de la Programación Orientada a Objetos: modelar el problema mediante clases que colaboran entre sí, cada una con una responsabilidad clara.

---

## Resumen de conceptos POO por archivo

| Archivo | Conceptos de POO aplicados |
|---|---|
| `EstadoDespacho.kt` | `sealed class`, `object`, `data class` anidada, `override toString()` |
| `Pedido.kt` | `open class`, herencia, `override`, polimorfismo |
| `FastGoDelivery.kt` | `data class`, `companion object`, encapsulamiento (`private`), funciones de orden superior, `suspend fun`, `try-catch` |
| `Main.kt` | Instanciación de objetos, `runBlocking`, orquestación del flujo |
