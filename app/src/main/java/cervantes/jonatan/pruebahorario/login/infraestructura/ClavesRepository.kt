package cervantes.jonatan.pruebahorario.login.infraestructura

import android.util.Log
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.tasks.await
import java.lang.Exception

object ClavesRepository {

    private val clavesCollectionRef = Firebase.firestore.collection("claves")
    private const val TAG = "LoginActivity"

    fun verificarClaveEmpleadoAsync(clave:String) = CoroutineScope(Dispatchers.IO).async {
        verificarClaveEmpleado(
            clave
        )
    }

    private suspend fun verificarClaveEmpleado(clave:String) : Boolean {
        try {
            val query = clavesCollectionRef.whereEqualTo("clave", clave).get().await()
            if(!query.isEmpty) {
                return true
            }
        } catch (e: Exception) {
            Log.d(TAG, e.message)
        }
        return false
    }


}