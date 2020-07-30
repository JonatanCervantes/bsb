package cervantes.jonatan.pruebahorario.servicios.infraestructura

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import cervantes.jonatan.pruebahorario.servicios.dominio.Servicio
import cervantes.jonatan.pruebahorario.utilidades.Connectivity
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await

object ServiciosRepository {

    val serviciosCollectionRef = Firebase.firestore.collection("servicios")
    private val imagesRef = Firebase.storage.reference

    var listaServicios = MutableLiveData<ArrayList<Servicio>>()

    private val TAG = "ServiciosRepository"

    init {
        listaServicios.postValue(ArrayList<Servicio>())
        Log.d(TAG, "Entrando al init del repo de servicios")
        subscribeToRealtimeUpdatesLaunch()
    }

    fun subscribeToRealtimeUpdatesLaunch() = CoroutineScope(Dispatchers.IO).launch {
        subscribeToRealtimeUpdates()
    }


    private fun subscribeToRealtimeUpdates(){
        serviciosCollectionRef.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
            var job = CoroutineScope(Dispatchers.IO).launch {

                firebaseFirestoreException?.let {
                    Log.d(TAG, it.message)
                    return@launch
                }
                Log.d(TAG, "Entrando al empleados snapshotlistener")
                var listaServiciosProvisional = ArrayList<Servicio>()
                querySnapshot?.let {
                    for (document in it) {
                        var servicio = document.toObject<Servicio>()
                        servicio.idDocumento = document.id
                        listaServiciosProvisional.add(servicio)
                        listaServicios.postValue(listaServiciosProvisional)
                    }
                }
            }
        }
    }

    fun guardarServicio(servicio: Servicio, contexto:Context)  = CoroutineScope(Dispatchers.IO).launch{
        try {
            serviciosCollectionRef.add(servicio).await()
            withContext(Dispatchers.Main) {
                Toast.makeText(contexto, "Servicio guardado correctamente", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {

            e.printStackTrace()
            Log.d(TAG, e.message)
            withContext(Dispatchers.Main) {
                Toast.makeText(contexto, e.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    fun eliminarServicio(idDocumento: String, contexto: Context)  = CoroutineScope(Dispatchers.IO).launch{
        try {
            serviciosCollectionRef.document(idDocumento).delete().await()
            withContext(Dispatchers.Main) {
                Toast.makeText(contexto, "Servicio eliminado correctamente", Toast.LENGTH_LONG).show()
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

    private suspend fun uploadImageToStorage(fileName: String, currentFile: Uri?, contexto:Context ) : String {

        if(currentFile != null) {
            try {
                currentFile?.let {
                    imagesRef.child("images/$fileName").putFile(it).await()
                    Log.d("AgregarServicioDialog", "Se subio la imagen")
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

    fun obtenerListaServicios() : ArrayList<Servicio> {
        return listaServicios.value!!
    }



}