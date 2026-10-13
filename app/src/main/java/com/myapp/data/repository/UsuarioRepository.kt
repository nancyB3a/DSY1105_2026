package com.myapp.data.repository

import com.myapp.data.room.UsuarioDao
import com.myapp.data.room.UsuarioEntity

/**
 * REPOSITORIO = única puerta de entrada a los datos de usuarios.
 *
 * Los ViewModels NO conocen Room ni los DAO: solo hablan con este repositorio.
 * Si mañana los usuarios vinieran de internet en vez de SQLite, solo cambiaría esta
 * clase; los ViewModels y las pantallas quedarían intactos.
 */
class UsuarioRepository(private val dao: UsuarioDao) {

    /** Devuelve true si se registró, false si ya existía un usuario con ese email. */
    suspend fun registrar(email: String, password: String): Boolean {
        // insertar() devuelve -1 cuando el email ya existe (onConflict = IGNORE)
        return dao.insertar(UsuarioEntity(email, password)) != -1L
    }

    /** Devuelve true si el email existe y la contraseña coincide. */
    suspend fun autenticar(email: String, password: String): Boolean {
        val usuario = dao.buscarPorEmail(email) ?: return false
        return usuario.password == password
    }
}
