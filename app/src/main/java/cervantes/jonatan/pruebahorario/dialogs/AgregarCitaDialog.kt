package cervantes.jonatan.pruebahorario.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.DialogFragment
import cervantes.jonatan.pruebahorario.R
import cervantes.jonatan.pruebahorario.entidades.*
import cervantes.jonatan.pruebahorario.ui.citas.CitasFragment
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.dialog_cita_agregar.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat

class AgregarCitaDialog : DialogFragment() {

    private lateinit var tv_texto: TextView
    private lateinit var tv_ok: TextView
    private lateinit var tv_cancel: TextView
    private lateinit var tv_fechaCita: TextView

    private val citaCollectionRef= Firebase.firestore.collection("citas")

    var contextoActivityMain: Context?= null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.dialog_cita_agregar, container, false)

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tv_texto = view.findViewById(R.id.tv_detallesCita)
        tv_ok = view.findViewById(R.id.tv_ok)
        tv_cancel = view.findViewById(R.id.tv_cancel)
        tv_fechaCita = view.findViewById(R.id.tv_fechaCita)

        contextoActivityMain = view.context

        tv_cancel.setOnClickListener {
            CitasFragment.inicializarFechas()
            dialog!!.dismiss()
        }

        tv_ok.setOnClickListener {

            val spinnerServicio = view.findViewById<Spinner>(R.id.spinner_servicios)
            val spinnerEmpleado = spinner_empleados

            var idServicio = spinnerServicio.selectedItemId.toInt()
            var idEmpleado = spinnerEmpleado.selectedItemId.toInt()
            var idCita = 7
            var fecha = Timestamp(CitasFragment.fechaSeleccionada.time)

            var cliente = Usuario(idUsuario = "")
            var empleado = Usuario(idUsuario = idEmpleado.toString())
            var servicio = Servicio(idServicio)

            var cita:Cita = Cita(idCita, cliente, empleado, servicio, fecha)

            guardarCita(cita)

            CitasFragment.inicializarFechas()

            dialog!!.dismiss()
        }
        tv_fechaCita.text = Editable.Factory.getInstance().newEditable(SimpleDateFormat("EEE dd/MM/yyyy")
            .format(CitasFragment.fechaSeleccionada.time))

        configurarSpinnerEmpleados(view)
        configurarSpinnerServicios(view)
    }

    private fun guardarCita(cita: Cita)  = CoroutineScope(Dispatchers.IO).launch{
        try {
            citaCollectionRef.add(cita).await()
            withContext(Dispatchers.Main) {
                Toast.makeText(contextoActivityMain, "Cita guardada correctamente", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {

            e.printStackTrace()
            Log.d("CustomDialog", e.message)
            withContext(Dispatchers.Main) {
                Toast.makeText(contextoActivityMain, e.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    fun configurarSpinnerEmpleados(view: View) {
        val empleados = resources.getStringArray(R.array.arreglo_empleados)

        val spinner = view.findViewById<Spinner>(R.id.spinner_empleados)
        if (spinner != null) {
            //val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, medicamentos)
            val adapter = ArrayAdapter(view.context,
                R.layout.spinner_item, empleados)
            //adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
            spinner.adapter = adapter

            spinner.onItemSelectedListener = object :
                AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {

                }
                override fun onNothingSelected(parent: AdapterView<*>) {
                }
            }
        }
    }

    fun configurarSpinnerServicios(view: View) {
        val servicios = resources.getStringArray(R.array.arreglo_servicios)

        val spinner = view.findViewById<Spinner>(R.id.spinner_servicios)
        if (spinner != null) {
            //val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, medicamentos)
            val adapter = ArrayAdapter(view.context,
                R.layout.spinner_item, servicios)
            //adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
            spinner.adapter = adapter

            spinner.onItemSelectedListener = object :
                AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {

                }
                override fun onNothingSelected(parent: AdapterView<*>) {
                }
            }
        }
    }






}