package com.myapp.utils

private val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

// Largo mínimo de contraseña: se define UNA sola vez y lo usan las validaciones
// de Login y Registro, y también el efecto visual rojo/verde del campo.
const val MIN_PASSWORD = 3

fun esEmailValido(email : String) : Boolean{
    return EMAIL_REGEX.matches(email.trim())
}

fun esPasswordValida(password: String): Boolean {
    return password.length >= MIN_PASSWORD
}
