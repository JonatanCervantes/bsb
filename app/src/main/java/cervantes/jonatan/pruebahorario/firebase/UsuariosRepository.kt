package cervantes.jonatan.pruebahorario.firebase

import android.util.Log
import cervantes.jonatan.pruebahorario.entidades.Usuario
import cervantes.jonatan.pruebahorario.utilidades.TiposUsuario
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.lang.Exception

object UsuariosRepository {

    private val usuariosCollectionRef = Firebase.firestore.collection("usuarios")
    private var usuarioActual:Usuario ?= null


    fun verificarUsuarioNoRegistradoAsync(uid:String) = CoroutineScope(Dispatchers.IO).async {
        verificarUsuarioNoRegistrado(uid)
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

        var usuario = Usuario(nombre!!, email!!, tipoUsuario, uid!!)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                usuariosCollectionRef.add(usuario).await()
                Log.d("LoginActivity", "Usuario registrado correctamente")
            } catch (e:Exception) {
                Log.d("LoginActivity", e.message)
            }
        }
    }

    private fun obtenerUsuarioLaunch(uid:String) = CoroutineScope(Dispatchers.IO).async {
        obtenerUsuario(uid)
    }

    private suspend fun obtenerUsuario(uid:String) : Usuario? {
        var usuario:Usuario? = null
        try {
            val querySnapshot = usuariosCollectionRef.whereEqualTo("idUsuario", uid).get().await()
            usuario = querySnapshot.documents[0].toObject<Usuario>()!!
        } catch (e:Exception) {
            Log.d("LoginActivity", e.message)
        }
        return usuario
    }

    fun obtenerUsuarioActualLaunch(uid:String) = CoroutineScope(Dispatchers.IO).async {
        obtenerUsuarioActual(uid)
    }

    private suspend fun obtenerUsuarioActual(uid:String) : Usuario? {
        if(usuarioActual==null) {
            val usuarioDeferred = obtenerUsuarioLaunch(uid)
            usuarioActual = usuarioDeferred.await()
        }
        return usuarioActual
    }




}