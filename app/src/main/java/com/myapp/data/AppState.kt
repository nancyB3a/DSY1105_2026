package com.myapp.data

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class Usuario(val email: String, val password: String)

class AppState(private val dataStore: DataStoreManager) {
    val usuarios = mutableStateListOf<Usuario>()
    var usuarioActual: Usuario? = null
    //val notasPorUsuario = mutableMapOf<String, MutableList<String>>()
    val notasPorUsuario = mutableStateMapOf<String, SnapshotStateList<String>>()

    private val scope = CoroutineScope(Dispatchers.IO)

    // Cargar datos iniciales
    fun cargarDatos() {
        scope.launch {
            val users = dataStore.getUsers().first()
            val notes = dataStore.getNotes().first()

            usuarios.clear()
            usuarios.addAll(users)

            notasPorUsuario.clear()
            notes.forEach { (k, v) ->
                notasPorUsuario[k] = v.toMutableStateList()//v.toMutableList()

            }
        }
    }

    fun registrarUsuario(email: String, password: String): Boolean {
        if (usuarios.any { it.email == email }) return false
        val nuevo = Usuario(email, password)
        usuarios.add(nuevo)
        guardarUsuarios()
        return true
    }

    fun login(email: String, password: String): Boolean {
        val user = usuarios.find { it.email == email && it.password == password }
        return if (user != null) {
            usuarioActual = user
            true
        } else false
    }

    fun logout() {
        usuarioActual = null
    }

    fun agregarNota(nota: String) {
        val email = usuarioActual?.email ?: return
        //val notas = notasPorUsuario.getOrPut(email) { mutableListOf() }
        val notas = notasPorUsuario.getOrPut(email) { mutableStateListOf() }
        notas.add(nota)
        guardarNotas()
    }

    fun obtenerNotas(): List<String> {
        val email = usuarioActual?.email ?: return emptyList()
        return notasPorUsuario[email] ?: mutableStateListOf()//emptyList()
    }

    fun borrarNota(index: Int) {
        val email = usuarioActual?.email ?: return
        notasPorUsuario[email]?.let {
            if (index in it.indices) {
                it.removeAt(index)
                guardarNotas()
            }
        }
    }

    //Guardar en DataStore
    private fun guardarUsuarios() {
        scope.launch {
            dataStore.saveUsers(usuarios)
        }
    }
    private fun guardarNotas() {
        scope.launch {
            dataStore.saveNotes(notasPorUsuario)
        }
    }
}
