# FastGo Delivery — Sistema de gestión de despachos en Kotlin

Este documento explica **cómo se construyó** la solución, paso a paso, poniendo énfasis en los conceptos de Programación Orientada a Objetos (POO) de Kotlin que se van aplicando en cada etapa.

## Idea general del sistema

FastGo Delivery gestiona una flota de 8 repartidores. Cada pedido (bicicleta, moto o auto) trae su propio código, distancia, fecha de creación y cliente. El sistema busca un repartidor disponible, se lo asigna, simula —de forma asíncrona— la comunicación con su app durante la asignación y la entrega, calcula el monto a cobrar, y libera al repartidor una vez confirmada la entrega.

---

## Paso 1 — Modelar los estados del repartidor con `sealed class`

**Archivo:** `EstadoRepartidor.kt`

Antes de escribir cualquier lógica de negocio, conviene definir **qué estados puede tener** un repartidor: `Disponible`, `Ocupado`, `Procesando` o `Fuera de servicio` (R2).

```kotlin
sealed class EstadoRepartidor {
    object Disponible : EstadoRepartidor() { override fun toString() = "Disponible" }
    data class Ocupado(val pedido: Pedido) : EstadoRepartidor() { override fun toString() = "Ocupado (pedido ${pedido.codigo})" }
    data class Procesando(val motivo: String) : EstadoRepartidor() { override fun toString() = "Procesando: $motivo" }
    data class FueraDeServicio(val motivo: String) : EstadoRepartidor() { override fun toString() = "Fuera de servicio: $motivo" }
}
```

### Concepto de POO: clases selladas (`sealed class`)

Una `sealed class` es una clase abstracta cuyas subclases están **todas conocidas y declaradas dentro del mismo archivo**. Le dice al compilador: "estos son *todos* los casos posibles, no hay más".

Se eligió sobre un `enum` porque `Ocupado`, `Procesando` y `FueraDeServicio` necesitan llevar un dato adicional (el pedido asignado o el motivo), algo que un `enum` tradicional no puede hacer con la misma naturalidad. Y se eligió sobre un simple `String` ("disponible", "ocupado", etc.) porque un `String` no impide errores de tipeo ni obliga a cubrir todos los casos.

**Ventaja concreta:** en `FastGoDelivery.kt`, la función `mostrarEstado()` usa un `when (estado) { ... }` sobre esta sealed class. Si en el futuro se agrega un quinto estado, el compilador **marcará error** en cualquier `when` que no lo contemple, evitando que se nos olvide manejar el caso nuevo. Esto es justo lo que pide R2: "toda lógica que dependa del estado debe contemplar los cuatro estados posibles".

---

## Paso 2 — Modelar los pedidos con herencia y polimorfismo

**Archivo:** `Pedido.kt`

El sistema debe reconocer tres tipos de repartidor, cada uno con su propia forma de calcular el costo. En vez de escribir una sola clase con un campo `tipo: String` y un gigante `if/else` por todos lados, se modela con herencia. La clase base incluye los cuatro datos comunes que exige R1: código, distancia, fecha de creación y cliente.

```kotlin
open class Pedido(
    val codigo: String,
    val distanciaKm: Double,
    val fechaCreacion: LocalDateTime,
    val cliente: Cliente
) {
    open fun calcularCosto(): Double = distanciaKm * 400.0
    open fun detalle(): String = "$codigo - ${distanciaKm} km - Cliente: ${cliente.tipo}"
}

class PedidoBicicleta(
    codigo: String,
    distanciaKm: Double,
    fechaCreacion: LocalDateTime,
    cliente: Cliente
) : Pedido(codigo, distanciaKm, fechaCreacion, cliente) {
    override fun calcularCosto(): Double {
        if (distanciaKm < 1.0) return 500.0
        return distanciaKm * 300.0
    }
    override fun detalle(): String = "${super.detalle()} - Bicicleta"
}
```

(Y de forma análoga, `PedidoMoto` con `horarioPeak` y `PedidoAuto` con `refrigerado`, cada una agregando solo su propio dato particular.)

### Conceptos de POO: herencia, `open`, `override` y polimorfismo

- **`open class`**: en Kotlin, las clases son `final` por defecto (no se pueden heredar). Hay que marcar explícitamente `open` tanto la clase como los métodos que se quieran sobrescribir. Heredar de una clase debe ser una elección consciente, no un accidente.
- **Herencia**: cada subclase reutiliza los cuatro datos comunes de `Pedido` y le pasa los parámetros al constructor de la superclase, agregando únicamente lo que le es propio (`horarioPeak`, `refrigerado`).
- **`override`**: cada subclase reemplaza `calcularCosto()` y `detalle()` con su propia regla de negocio.
- **Polimorfismo**: en `FastGoDelivery.kt`, cuando se llama a `pedido.calcularCosto()`, el código **no sabe ni le importa** si `pedido` es una bicicleta, una moto o un auto — Kotlin ejecuta automáticamente la versión correcta según el tipo real del objeto en tiempo de ejecución.

**Ventaja concreta:** si mañana aparece un cuarto tipo de repartidor (por ejemplo, un dron), basta con crear `PedidoDron : Pedido(...)` con su propio `calcularCosto()`. No hay que tocar ni una línea de `FastGoDelivery.kt` ni de `Main.kt`.

---

## Paso 3 — Modelar el cliente con `data class` (sin `enum`)

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

- **`data class`**: se usa para clases cuyo propósito principal es *transportar datos*. El compilador genera automáticamente `equals()`, `hashCode()`, `toString()` y `copy()`.
- **`companion object`**: permite tener una función asociada a la *clase* (`Cliente.fromString(...)`) en vez de a una *instancia* — el equivalente en Kotlin a un método estático de Java.
- El `when` con `else -> null` valida el dato: si el texto no corresponde a ninguno de los tres tipos válidos, se retorna `null` y quien lo llama decide qué hacer.

Además de `Cliente`, en este mismo archivo se definieron dos `data class` más siguiendo la misma idea: `Repartidor(val numero: Int, var estado: EstadoRepartidor)` para representar a cada repartidor de la flota, y `Comprobante(val numero, val pedido, val monto)` para el documento que se emite al confirmar una entrega.

**Ventaja concreta:** se logra el mismo objetivo que tendría un `enum` para el cliente (un conjunto cerrado de tipos válidos), pero con más flexibilidad, y sin necesidad de declarar tipos enumerados para conceptos que son, en esencia, simples portadores de datos.

---

## Paso 4 — Construir la flota de repartidores y la asignación asíncrona (R2 + R5)

**Archivo:** `FastGoDelivery.kt`

```kotlin
class FastGoDelivery(cantidadRepartidores: Int = 8) {
    val repartidores: List<Repartidor> = (1..cantidadRepartidores).map { Repartidor(it) }

    suspend fun asignarPedido(pedido: Pedido): Boolean {
        try {
            if (!codigoValido(pedido.codigo)) {
                throw IllegalArgumentException("Código de pedido inválido: '${pedido.codigo}'")
            }
            val libre = repartidores.firstOrNull { it.estado is EstadoRepartidor.Disponible }
                ?: throw IllegalStateException("Sin repartidores disponibles")

            libre.estado = EstadoRepartidor.Procesando("Asignando pedido ${pedido.codigo}")
            delay(2500) // simula comunicación con la app del repartidor

            libre.estado = EstadoRepartidor.Ocupado(pedido)
            return true
        } catch (e: Exception) {
            println("[ERROR] ${e.message}")
            return false
        }
    }
    // ...
}
```

### Conceptos de POO / concurrencia en Kotlin

- **Encapsulamiento**: `repartidores` es una `List` pública de solo lectura desde afuera; el `var estado` de cada `Repartidor` solo lo modifica la propia clase `FastGoDelivery`, controlando en qué momento y bajo qué condiciones cambia.
- **`firstOrNull { }`**: función de orden superior que recorre la lista y retorna el primer elemento que cumple la condición (o `null` si ninguno la cumple) — evita escribir un `for` manual con una bandera para saber si se encontró algo.
- **`suspend fun` + `delay()`**: marca una función que puede "pausarse" sin bloquear el hilo. `delay(2500)` simula la espera de 2,5 segundos de la comunicación con la app del repartidor durante la asignación (R5), y `delay(5000)` simula los 5 segundos de la confirmación de entrega, en `confirmarEntrega()`.
- **`runBlocking { }`** (en `Main.kt`): crea el puente entre el mundo síncrono de `fun main()` y las funciones `suspend` de `FastGoDelivery`.

**Ventaja concreta:** el repartidor pasa correctamente por sus cuatro estados posibles (`Disponible → Procesando → Ocupado → Procesando → Disponible`) sin bloquear el programa mientras "espera" a la app, y sin permitir asignar un pedido a alguien que ya está ocupado.

---

## Paso 5 — Manejo de errores sin detener el programa (R6)

**Archivo:** `FastGoDelivery.kt`

```kotlin
suspend fun confirmarEntrega(codigoPedido: String): Boolean {
    try {
        val repartidor = repartidores.firstOrNull { r ->
            (r.estado as? EstadoRepartidor.Ocupado)?.pedido?.codigo == codigoPedido
        } ?: throw IllegalStateException("Pedido no encontrado: '$codigoPedido' no está asignado a ningún repartidor")
        // ...
    } catch (e: Exception) {
        println("[ERROR] ${e.message}")
        return false
    }
}
```

### Concepto de POO: manejo de excepciones (`try-catch`)

En vez de dejar que un dato inválido (código mal formado, pedido inexistente, flota completa, monto inválido) provoque una excepción que **detenga todo el programa**, se usa `try-catch` para capturarla, informarla en pantalla y devolver `false`, dejando que `Main.kt` siga con el siguiente pedido.

**Ventaja concreta:** el sistema puede procesar decenas de pedidos seguidos y, si uno de ellos falla por cualquiera de los cuatro motivos de R6 (código inválido, tarifa inválida, pedido no encontrado, sin repartidores disponibles), solo ese pedido se ve afectado — el resto sigue funcionando con normalidad.

---

## Paso 6 — Integrar todo en `Main.kt`

**Archivo:** `Main.kt`

```kotlin
fun main() = runBlocking {
    val sistema = FastGoDelivery(cantidadRepartidores = 8)
    val pedidosValidos = listOf(
        PedidoBicicleta("DG100001", 0.8, ahora, Cliente("Nuevo", 0.0)),
        // ...
    )
    for (pedido in pedidosValidos) {
        if (sistema.asignarPedido(pedido)) {
            sistema.confirmarEntrega(pedido.codigo)
        }
    }
    sistema.imprimirReporteCierre()
}
```

`Main.kt` es el único lugar donde se **instancian objetos concretos** (`PedidoBicicleta`, `PedidoMoto`, `PedidoAuto`, `Cliente`) y se orquesta el flujo completo: asigna cada pedido, confirma su entrega, y luego ejercita los cuatro casos de error de R6 antes de imprimir el reporte de cierre de turno (R4). El resto de las clases (`Pedido`, `EstadoRepartidor`, `FastGoDelivery`) no conocen los datos de prueba específicos — solo definen comportamiento genérico. Esta separación entre "quién crea los datos" y "quién define el comportamiento" es, en esencia, el objetivo de la Programación Orientada a Objetos.

---

## Resumen de conceptos POO por archivo

| Archivo | Conceptos de POO aplicados |
|---|---|
| `EstadoRepartidor.kt` | `sealed class`, `object`, `data class` anidada, `override toString()` |
| `Pedido.kt` | `open class`, herencia, `override`, polimorfismo |
| `FastGoDelivery.kt` | `data class` (`Cliente`, `Repartidor`, `Comprobante`), `companion object`, encapsulamiento (`private`), `firstOrNull`/`filter`/`map`/`maxByOrNull`, `suspend fun`, `try-catch`, `when` sobre sealed class |
| `Main.kt` | Instanciación de objetos, `runBlocking`, orquestación del flujo, pruebas de los 4 errores de R6 |

| Archivo | Conceptos de POO aplicados |
|---|---|
| `EstadoDespacho.kt` | `sealed class`, `object`, `data class` anidada, `override toString()` |
| `Pedido.kt` | `open class`, herencia, `override`, polimorfismo |
| `FastGoDelivery.kt` | `data class`, `companion object`, encapsulamiento (`private`), funciones de orden superior, `suspend fun`, `try-catch` |
| `Main.kt` | Instanciación de objetos, `runBlocking`, orquestación del flujo |
