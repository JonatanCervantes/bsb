package cervantes.jonatan.pruebahorario.firebase

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import cervantes.jonatan.pruebahorario.entidades.Cita
import cervantes.jonatan.pruebahorario.utilidades.FechasHelper
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlin.collections.ArrayList

object CitasRepository {

    private val citaCollectionRef= Firebase.firestore.collection("citas")
    private const val TAG = "CitasRepository"
    var listaCitas = MutableLiveData<ArrayList<Cita>>()

    init {
        subscribeToRealtimeUpdatesLaunch()
        listaCitas.postValue(ArrayList<Cita>())
    }

    private fun subscribeToRealtimeUpdatesLaunch() = CoroutineScope(Dispatchers.IO).launch {
        subscribeToRealtimeUpdates()
    }

    private fun subscribeToRealtimeUpdates(){
        citaCollectionRef.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
            var job = CoroutineScope(Dispatchers.IO).launch {

                firebaseFirestoreException?.let {
                    Log.d(TAG, it.message)
                    return@launch
                }
                solicitarCitasManualmente()
            }
        }
    }

    fun solicitarCitasManualmente() = CoroutineScope(Dispatchers.IO).launch {
            try {
                val querySnapshot = citaCollectionRef
                    .whereGreaterThanOrEqualTo("fecha", Timestamp(FechasHelper.obtenerFechaQueryActual().time))
                    .whereLessThan("fecha", Timestamp(FechasHelper.obtenerFechaQueryFutura().time)).get().await()

                var listaCitasProvisional = ArrayList<Cita>()
                querySnapshot?.let {
                    Log.d(TAG, "Se obtuvo un conjunto de: ${it.size()} citas" )
                    for (document in it) {
                        var cita = document.toObject<Cita>()
                        cita.idDocumento = document.id
                        listaCitasProvisional.add(cita)
                    }
                    listaCitas.postValue(listaCitasProvisional)
                }
            } catch(e: Exception) {
                e.printStackTrace()
                Log.d(TAG, e.message)
            }
    }

    fun guardarCita(cita: Cita, contexto:Context)  = CoroutineScope(Dispatchers.IO).launch{
        try {
            citaCollectionRef.add(cita).await()
            withContext(Dispatchers.Main) {
                Toast.makeText(contexto, "Cita guardada correctamente", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {

            e.printStackTrace()
            Log.d(TAG, e.message)
            withContext(Dispatchers.Main) {
                Toast.makeText(contexto, e.message, Toast.LENGTH_LONG).show()
            }
        }
    }


    fun eliminarCita(idDocumento: String, contexto: Context)  = CoroutineScope(Dispatchers.IO).launch{
        try {
            citaCollectionRef.document(idDocumento).delete().await()
            withContext(Dispatchers.Main) {
                Toast.makeText(contexto, "Cita eliminada correctamente", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d(TAG, e.message)
            withContext(Dispatchers.Main) {
                Toast.makeText(contexto, e.message, Toast.LENGTH_LONG).show()
            }
        }
    }




}