package cervantes.jonatan.pruebahorario.dialogs

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.DialogFragment
import cervantes.jonatan.pruebahorario.R
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class EliminarServicioDialog : DialogFragment() {

    private lateinit var tv_no: TextView
    private lateinit var tv_si: TextView
    private val TAG = "EliminarServicioDialog"

    var idServicioEliminar = ""

    private val serviciosCollectionRef= Firebase.firestore.collection("servicios")

    var contextoActivityMain: Context?= null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.dialog_servicio_eliminar, container, false)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tv_no = view.findViewById(R.id.tv_noServicio)
        tv_si = view.findViewById(R.id.tv_siServicio)

        contextoActivityMain = view.context

        tv_no.setOnClickListener {
            dialog!!.dismiss()
        }

        tv_si.setOnClickListener {
            eliminarServicio(idServicioEliminar)
            dialog!!.dismiss()
        }
    }

    private fun eliminarServicio(idDocumento: String)  = CoroutineScope(Dispatchers.IO).launch{
        try {
            serviciosCollectionRef.document(idDocumento).delete().await()
            withContext(Dispatchers.Main) {
                Toast.makeText(contextoActivityMain, "Servicio eliminado correctamente", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d(TAG, e.message)
            withContext(Dispatchers.Main) {
                Toast.makeText(contextoActivityMain, e.message, Toast.LENGTH_LONG).show()
            }
        }
    }






}