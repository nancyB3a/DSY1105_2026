package com.myapp.data.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

// El DAO (Data Access Object) declara QUÉ consultas SQL existen.
// Room genera automáticamente el código que las ejecuta.
@Dao
interface UsuarioDao {

    // IGNORE: si ya existe un usuario con ese email (clave primaria), Room no lanza
    // error y devuelve -1. Así "registrar" es una sola operación atómica en la base
    // de datos, sin tener que consultar primero y luego insertar.
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertar(usuario: UsuarioEntity): Long

    @Query("SELECT * FROM usuarios WHERE email = :email LIMIT 1")
    suspend fun buscarPorEmail(email: String): UsuarioEntity?
}
