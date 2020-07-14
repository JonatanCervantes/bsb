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
import cervantes.jonatan.pruebahorario.utilidades.Disponibilidades
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.dialog_empleado_modificar.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ModificarEmpleadoDialog : DialogFragment() {

    private lateinit var tv_no: TextView
    private lateinit var tv_si: TextView
    private val TAG = "ModificarEmpleadoDialog"

    var idEmpleadoModificar = ""

    private val empleadosCollectionRef= Firebase.firestore.collection("empleados")

    var contextoActivityMain: Context?= null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.dialog_empleado_modificar, container, false)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tv_no = view.findViewById(R.id.tv_noModificarEmpleado)
        tv_si = view.findViewById(R.id.tv_siModificarEmpleado)

        contextoActivityMain = view.context

        tv_no.setOnClickListener {
            dialog!!.dismiss()
        }

        tv_si.setOnClickListener {
            val nuevaDisponibilidad = revisarSeleccion()
            if(nuevaDisponibilidad != "-1") {
                modificarEmpleado(idEmpleadoModificar, nuevaDisponibilidad)
            }
            dialog!!.dismiss()
        }
    }

    private fun revisarSeleccion(): String {
        var nuevaDisponibilidad:String = "-1"
        if(rb_disponible.isChecked) {
            nuevaDisponibilidad = Disponibilidades.DISPONIBLE.name
        } else if(rb_ocupado.isChecked) {
            nuevaDisponibilidad = Disponibilidades.OCUPADO.name
        } else if(rb_fueraDeTurno.isChecked) {
            nuevaDisponibilidad = Disponibilidades.FUERADETURNO.name
        }

        return nuevaDisponibilidad
    }

    private fun modificarEmpleado(idDocumento: String, nuevaDisponibilidad:String)  = CoroutineScope(Dispatchers.IO).launch{
        try {
            empleadosCollectionRef.document(idDocumento).update("disponibilidad", nuevaDisponibilidad).await()
            withContext(Dispatchers.Main) {
                Toast.makeText(contextoActivityMain, "Empleado actualizado correctamente", Toast.LENGTH_LONG).show()
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