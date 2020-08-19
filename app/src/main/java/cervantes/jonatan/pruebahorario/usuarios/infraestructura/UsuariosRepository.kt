package cervantes.jonatan.pruebahorario.usuarios.infraestructura

import android.util.Log
import cervantes.jonatan.pruebahorario.notificaciones.FirebaseService
import cervantes.jonatan.pruebahorario.usuarios.dominio.Usuario
import cervantes.jonatan.pruebahorario.usuarios.dominio.TiposUsuario
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.lang.Exception

object UsuariosRepository {

    private val usuariosCollectionRef = Firebase.firestore.collection("usuarios")
    private var usuarioActual: Usuario?= null
    private val authInstance = FirebaseAuth.getInstance()
    private const val  TAG = "UsuariosRepository"

    fun verificarUsuarioNoRegistradoAsync(uid:String) = CoroutineScope(Dispatchers.IO).async {
        verificarUsuarioNoRegistrado(
            uid
        )
    }

    private suspend fun verificarUsuarioNoRegistrado(uid:String) : Boolean{
        var usuarioNoRegistrado = true
            try {
                val querySnapshot = usuariosCollectionRef.whereEqualTo("idUsuario", uid).get().await()
                usuarioNoRegistrado = querySnapshot.isEmpty
            } catch (e: Exception) {
                Log.d("LoginActivity", e.message)
            }
        return usuarioNoRegistrado
    }

    suspend fun registrarUsuario(account: GoogleSignInAccount, uid: String, empleadoVerificado:Boolean) {
        val nombre = account.displayName
        val email = account.email

        var tipoUsuario:String = if(empleadoVerificado) {
            TiposUsuario.EMPLEADO.name
        } else {
            TiposUsuario.CLIENTE.name
        }

        var usuario = Usuario(
            nombre!!,
            email!!,
            tipoUsuario,
            uid!!
        )
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val documento = usuariosCollectionRef.add(usuario).await()
                documento.update("idDocumento", documento.id)
                FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener {
                    documento.update("token", it.token)
                }
                Log.d("LoginActivity", "Usuario registrado correctamente")
            } catch (e:Exception) {
                Log.d("LoginActivity", e.message)
            }
        }
    }

    private fun obtenerUsuarioLaunch(uid:String) = CoroutineScope(Dispatchers.IO).async {
        obtenerUsuario(
            uid
        )
    }

    private suspend fun obtenerUsuario(uid:String) : Usuario? {
        var usuario: Usuario? = null
        try {
            val querySnapshot = usuariosCollectionRef.whereEqualTo("idUsuario", uid).get().await()
            usuario = querySnapshot.documents[0].toObject<Usuario>()!!
        } catch (e:Exception) {
            Log.d("LoginActivity", e.message)
        }
        return usuario
    }

    fun obtenerUsuarioActualLaunch(uid:String) = CoroutineScope(Dispatchers.IO).async {
        obtenerUsuarioActual(
            uid
        )
    }

    private suspend fun obtenerUsuarioActual(uid:String) : Usuario? {
        if(usuarioActual ==null) {
            val usuarioDeferred =
                obtenerUsuarioLaunch(
                    uid
                )
            usuarioActual = usuarioDeferred.await()
        }
        return usuarioActual
    }

    fun verificarUsuarioActualPerteneceCita(usuario:Usuario): Boolean{
        try {
            if(usuario.idUsuario == authInstance.uid)
                return true
        }catch (e:Exception) {
            Log.d(TAG, e.message)
        }
        return false
    }

    fun olvidarUsuarioActual() {
        this.usuarioActual == null
    }

    fun actualizarTokenUsuario(idDocumento:String, token:String) = CoroutineScope(Dispatchers.IO).launch {
        try {
            usuariosCollectionRef.document(idDocumento).update("token", token).await()
        }catch (e:Exception) {
            Log.d(TAG, e.message)
        }

    }

    fun getUsuarioActual() : Usuario?{
        return usuarioActual
    }




}