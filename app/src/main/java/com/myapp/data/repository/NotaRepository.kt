package com.myapp.data.repository

import com.myapp.data.room.NotaDao
import com.myapp.data.room.NotaEntity
import kotlinx.coroutines.flow.Flow

/** Única puerta de entrada a los datos de notas (ver UsuarioRepository). */
class NotaRepository(private val dao: NotaDao) {

    /** Flujo que emite la lista de notas del usuario cada vez que ésta cambia en la base de datos. */
    fun observarNotas(email: String): Flow<List<NotaEntity>> = dao.observarPorUsuario(email)

    suspend fun agregar(email: String, texto: String) {
        dao.insertar(NotaEntity(emailUsuario = email, texto = texto))
    }

    suspend fun eliminar(nota: NotaEntity) {
        dao.eliminar(nota)
    }
}
