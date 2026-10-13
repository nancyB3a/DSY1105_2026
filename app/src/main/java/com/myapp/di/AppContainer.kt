package com.myapp.di

import android.content.Context
import com.myapp.data.repository.NotaRepository
import com.myapp.data.repository.SesionRepository
import com.myapp.data.repository.UsuarioRepository
import com.myapp.data.room.AppDatabase

/**
 * "Caja de herramientas" de la app: crea UNA vez la base de datos y los repositorios,
 * y los entrega a quien los necesite (los ViewModels, a través de AppViewModelProvider).
 *
 * Esto es inyección de dependencias manual: en vez de que cada clase cree sus propias
 * dependencias, se las entregan ya armadas. (Hilt/Koin automatizan esto en proyectos grandes.)
 */
class AppContainer(context: Context) {
    private val database = AppDatabase.getInstance(context)

    val usuarioRepository = UsuarioRepository(database.usuarioDao())
    val notaRepository = NotaRepository(database.notaDao())
    val sesionRepository = SesionRepository()
}
