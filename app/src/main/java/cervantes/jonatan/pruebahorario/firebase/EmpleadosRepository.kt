package cervantes.jonatan.pruebahorario.firebase

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import cervantes.jonatan.pruebahorario.entidades.Empleado
import cervantes.jonatan.pruebahorario.utilidades.Connectivity
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await

object EmpleadosRepository {

    val empleadosCollectionRef = Firebase.firestore.collection("empleados")
    private val imagesRef = Firebase.storage.reference

    var listaEmpleados = MutableLiveData<ArrayList<Empleado>>().apply {
        this.postValue(ArrayList())
    }
    private val TAG = "EmpleadosRepository"

    init {
//        listaEmpleados.postValue(ArrayList<Empleado>())
        Log.d(TAG, "Entrando al init del repo de empleados")
        subscribeToRealtimeUpdatesLaunch()
    }

    fun subscribeToRealtimeUpdatesLaunch() = CoroutineScope(Dispatchers.IO).launch {
        subscribeToRealtimeUpdates()
    }

    private fun subscribeToRealtimeUpdates() {
        empleadosCollectionRef.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
            var job = CoroutineScope(Dispatchers.IO).launch {

                firebaseFirestoreException?.let {
                    Log.d(TAG, it.message)
                    return@launch
                }
                Log.d(TAG, "Entrando al empleados snapshotlistener")

                var listaEmpleadosProvisonal = ArrayList<Empleado>()
                querySnapshot?.let {
                    for (document in it) {
                        var empleado = document.toObject<Empleado>()
                        empleado.idDocumento = document.id
                        listaEmpleadosProvisonal.add(empleado)
                        listaEmpleados.postValue(listaEmpleadosProvisonal)
                    }
                }
            }
        }
    }

    fun guardarEmpleado(empleado: Empleado, contexto:Context)  = CoroutineScope(Dispatchers.IO).launch{
        try {
            empleadosCollectionRef.add(empleado).await()
            withContext(Dispatchers.Main) {
                Toast.makeText(contexto, "Empleado guardado correctamente", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d(TAG, e.message)
            withContext(Dispatchers.Main) {
                Toast.makeText(contexto, e.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    fun eliminarEmpleado(idDocumento: String, contexto:Context)  = CoroutineScope(Dispatchers.IO).launch{
        try {
            empleadosCollectionRef.document(idDocumento).delete().await()
            withContext(Dispatchers.Main) {
                Toast.makeText(contexto, "Empleado eliminado correctamente", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d(TAG, e.message)
            withContext(Dispatchers.Main) {
                Toast.makeText(contexto, e.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    fun modificarEmpleado(idDocumento: String, nuevaDisponibilidad:String, contexto:Context)  = CoroutineScope(Dispatchers.IO).launch{
        try {
            empleadosCollectionRef.document(idDocumento).update("disponibilidad", nuevaDisponibilidad).await()
            withContext(Dispatchers.Main) {
                Toast.makeText(contexto, "Empleado actualizado correctamente", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d(TAG, e.message)
            withContext(Dispatchers.Main) {
                Toast.makeText(contexto, e.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    fun uploadImageToStorageAsync(fileName: String, currentFile: Uri?, contexto: Context) = CoroutineScope(Dispatchers.IO).async {
        uploadImageToStorage(fileName, currentFile, contexto)
    }

    private suspend fun uploadImageToStorage(fileName: String, currentFile: Uri?, contexto: Context) : String {
        if(Connectivity.isOnline(contexto)) {
            if(currentFile != null) {
                try {
                    currentFile?.let {
                        imagesRef.child("images/$fileName").putFile(it).await()
                        Log.d(TAG, "Se subio la imagen")
                    }
                    val urlImagen = imagesRef.child("images/$fileName").downloadUrl.await().toString()

                    withContext(Dispatchers.Main) {
                        Toast.makeText(contexto, "Se subio la imagen al storage y se obtuvo URL", Toast.LENGTH_LONG).show()
                    }
                    return urlImagen
                } catch (e: Exception) {
                    Log.d(TAG, e.message)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(contexto, e.message, Toast.LENGTH_LONG).show()
                    }
                }
            }

        } else {
            withContext(Dispatchers.Main) {
                Toast.makeText(contexto, "Porfavor revise su conexion a internet", Toast.LENGTH_LONG).show()
            }
        }

        return ""
    }

    fun obtenerListaEmpleados() : ArrayList<Empleado> {
        return listaEmpleados.value!!
    }




}