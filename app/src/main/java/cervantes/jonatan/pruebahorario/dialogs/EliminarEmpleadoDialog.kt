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

class EliminarEmpleadoDialog : DialogFragment() {

    private lateinit var tv_no: TextView
    private lateinit var tv_si: TextView
    private val TAG = "EliminarEmpleadoDialog"

    var idEmpleadoEliminar = ""

    private val empleadosCollectionRef= Firebase.firestore.collection("empleados")

    var contextoActivityMain: Context?= null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.dialog_empleado_eliminar, container, false)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tv_no = view.findViewById(R.id.tv_noEmpleado)
        tv_si = view.findViewById(R.id.tv_siEmpleado)

        contextoActivityMain = view.context

        tv_no.setOnClickListener {
            dialog!!.dismiss()
        }

        tv_si.setOnClickListener {
            eliminarEmpleado(idEmpleadoEliminar)
            dialog!!.dismiss()
        }
    }

    private fun eliminarEmpleado(idDocumento: String)  = CoroutineScope(Dispatchers.IO).launch{
        try {
            empleadosCollectionRef.document(idDocumento).delete().await()
            withContext(Dispatchers.Main) {
                Toast.makeText(contextoActivityMain, "Empleado eliminado correctamente", Toast.LENGTH_LONG).show()
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