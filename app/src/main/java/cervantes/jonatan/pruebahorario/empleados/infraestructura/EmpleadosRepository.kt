package cervantes.jonatan.pruebahorario.empleados.infraestructura

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import cervantes.jonatan.pruebahorario.empleados.dominio.Empleado
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
                try {
                    var listaEmpleadosProvisonal = ArrayList<Empleado>()
                    querySnapshot?.let {
                        for (document in it) {
                            Log.d("EmpleadosRepository", "Antes de ToObject")
                            var empleado = document.toObject<Empleado>()
                            Log.d("EmpleadosRepository", "ToObject")
                            empleado.idDocumento = document.id
                            listaEmpleadosProvisonal.add(empleado)
                        }
                        listaEmpleados.postValue(listaEmpleadosProvisonal)
                    }

                }catch (e:Exception) {
                    Log.d("EmpleadosRepository", e.message)
                    e.printStackTrace()
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
        uploadImageToStorage(
            fileName,
            currentFile,
            contexto
        )
    }

    private suspend fun uploadImageToStorage(fileName: String, currentFile: Uri?, contexto: Context) : String {

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



        return ""
    }

    fun obtenerListaEmpleados() : ArrayList<Empleado> {
        return listaEmpleados.value!!
    }

    fun obtenerEmpleado(idEmpleadoSeleccionado:String) : Empleado? {
        obtenerListaEmpleados().forEach {
            if(it.idDocumento == idEmpleadoSeleccionado) {
                return it
            }
        }
        return null
    }




}