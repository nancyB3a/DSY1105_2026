package cl.duoc.ejcomidas.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/** Base de datos Room de la app (archivo SQLite "ejcomidas.db"). */
@Database(entities = [FavoritoEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun favoritoDao(): FavoritoDao

    companion object {
        @Volatile
        private var INSTANCIA: AppDatabase? = null

        /** Singleton: una sola conexión a la base para toda la app. */
        fun getDatabase(context: Context): AppDatabase =
            INSTANCIA ?: synchronized(this) {
                INSTANCIA ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ejcomidas.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCIA = it }
            }
    }
}
