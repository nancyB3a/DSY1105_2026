package cl.dsy1105.misnotasapp.data

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import cl.dsy1105.misnotasapp.data.room.AppDatabase
import cl.dsy1105.misnotasapp.data.room.NotaEntity
import cl.dsy1105.misnotasapp.data.room.UsuarioEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

data class Usuario(val email: String, val password: String)

/**
 * Misma responsabilidad y misma API pública que la versión con DataStore:
 * login, registrarUsuario, logout, agregarNota, obtenerNotas, borrarNota,
 * cargarDatos. Lo único que cambió es CÓMO se guardan los datos por dentro
 * (ahora en SQLite a través de Room, en vez de un JSON en Preferences).
 *
 * Por eso LoginScreen, RegistroScreen y NotasScreen no necesitan ningún
 * cambio: no les importa de dónde vienen los datos, solo cómo pedirlos.
 */
class AppState(private val db: AppDatabase) {
    // Cache reactivo en memoria: Compose observa estas colecciones.
    // Los cambios se reflejan en la UI de inmediato; el guardado en SQLite
    // ocurre en segundo plano con corrutinas, igual que antes con DataStore.
    val usuarios = mutableStateListOf<Usuario>()
    var usuarioActual: Usuario ? = null
        private set
    // email -> notas de ESE usuario (guardamos la entidad completa, con su
    // id de Room, para poder borrar la fila correcta más adelante).
    val notasPorUsuario = mutableStateMapOf<String, SnapshotStateList<NotaEntity>>()



    private val scope = CoroutineScope(Dispatchers.IO)

    // Carga inicial: trae todos los usuarios a memoria. Las notas se cargan
    // recién al hacer login (más eficiente que traer las de todo el mundo).
    fun cargarDatos(){
        scope.launch {
            val usuariosDb = db.usuarioDao().obtenerTodos()

            usuarios.clear()
            usuarios.addAll(usuariosDb.map { Usuario(it.email, it.password) })
        }
    }

    //registrar nuevos usuario
    fun registrarUsuario(email: String, password: String): Boolean{
        if (usuarios.any{ it.email == email}) return false
        usuarios.add(Usuario(email,password))//refleja el cambio en la UI al instante
        scope.launch {
            db.usuarioDao().insertar(UsuarioEntity(email,password))// lo persiste en SQLite
        }
        return true
    }
    //función para login
    fun login(email: String, password: String): Boolean{
        val user = usuarios.find { it.email == email && it.password == password } ?: return false
        usuarioActual = user
        //cargar Notas
        return true
    }

    fun logout(){
        usuarioActual = null
    }

    private fun cargarNotasPorUser(email: String){
        if (notasPorUsuario.containsKey(email)) return //ya está en memoria, no repite la consulta
        scope.launch {
            val notasDb = db.notaDao().obtenerPorUsuario(email)
            notasPorUsuario[email] =
                mutableStateListOf<NotaEntity>().apply { addAll(notasDb) }
        }
    }
    fun agregarNota(nota : String){
        val email = usuarioActual?.email ?: return
        val entidad = NotaEntity(emailUsuario = email, texto = nota)
        scope.launch {
            val idGenerado = db.notaDao().insertar(entidad)
            val listaActual = notasPorUsuario.getOrPut(email){mutableStateListOf()}
            listaActual.add(entidad.copy(id = idGenerado))
        }
    }

    fun obtenerNotas(): List<String>{
        val email = usuarioActual?.email ?: return emptyList()
        return  notasPorUsuario[email]?.map {it.texto} ?: emptyList()
    }

    fun borrarNota(index: Int){
        val email = usuarioActual?.email ?: return
        val lista = notasPorUsuario[email]?: return
        if (index !in lista.indices) return
        val nota = lista[index]
        scope.launch {
            db.notaDao().eliminar(nota)
            lista.remove(nota)
        }
    }
}