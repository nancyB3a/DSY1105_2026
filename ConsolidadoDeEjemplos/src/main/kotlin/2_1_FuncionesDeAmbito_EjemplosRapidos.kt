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