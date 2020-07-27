package cervantes.jonatan.pruebahorario.ui.empleados

import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import cervantes.jonatan.pruebahorario.R
import cervantes.jonatan.pruebahorario.dialogs.AgregarEmpleadoDialog
import cervantes.jonatan.pruebahorario.dialogs.AgregarServicioDialog
import cervantes.jonatan.pruebahorario.entidades.Empleado
import cervantes.jonatan.pruebahorario.entidades.Servicio
import cervantes.jonatan.pruebahorario.entidades.Usuario
import cervantes.jonatan.pruebahorario.ui.servicios.ServiciosFragment
import cervantes.jonatan.pruebahorario.utilidades.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_empleados.*
import kotlinx.android.synthetic.main.fragment_servicios.*
import kotlinx.coroutines.*

class EmpleadosFragment : Fragment() {

    private lateinit var slideshowViewModel: EmpleadosViewModel
    private var adapter: EmpleadoAdapter?= null
    private val empleadosCollectionRef = Firebase.firestore.collection("empleados")
    private var listaEmpleados:ArrayList<Empleado> = ArrayList<Empleado>()
    private var listaIdDocumentos:ArrayList<String> = ArrayList<String>()

    companion object {
        var  eliminarActivado:Boolean = false
        var  modificarActivado:Boolean = false
        var adminFragmento: FragmentManager?= null
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        slideshowViewModel = ViewModelProviders.of(this).get(EmpleadosViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_empleados, container, false)
//        val textView: TextView = root.findViewById(R.id.text_slideshow)
//        slideshowViewModel.text.observe(viewLifecycleOwner, Observer {
//            textView.text = it
//        })
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adminFragmento = fragmentManager

        var job = CoroutineScope(Dispatchers.IO).launch {
            adapter = EmpleadoAdapter(llenarListaRecyclerView())
            subscribeToRealtimeUpdatesLaunch()
        }

        runBlocking {
            job.join()
        }

        rv_empleados.adapter = adapter
        rv_empleados.layoutManager = LinearLayoutManager(view.context)

        fab_agregarEmpleado.setOnClickListener { view ->
            var dialog: AgregarEmpleadoDialog =
                AgregarEmpleadoDialog()
            dialog.show(fragmentManager!!, "AgregarEmpleadoDialog")
        }

        fab_eliminarEmpleado.setOnClickListener {
            eliminarActivado = !eliminarActivado

            if(eliminarActivado) {
                fab_eliminarEmpleado.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.colorSecundarioFAB))
            } else {
                fab_eliminarEmpleado.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.colorAccent))
            }
        }

        fab_modificarEmpleado.setOnClickListener {
            modificarActivado = !modificarActivado

            if(modificarActivado) {
                fab_modificarEmpleado.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.colorSecundarioFAB))
            } else {
                fab_modificarEmpleado.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.colorAccent))
            }
        }

        rl_adminEmpleados.isEnabled = false
        rl_adminEmpleados.isVisible = false

        hablitarAdminEmpleados()
    }

    private fun hablitarAdminEmpleados() {
        val sharedPref = activity?.getSharedPreferences(
            getString(R.string.preference_file_key), Context.MODE_PRIVATE) ?:return
        val rolActual = sharedPref.getString(getString(R.string.rol_usuario), "defaultValue")

        Log.d("EmpleadosFragment", rolActual)
        if(rolActual == (TiposUsuario.EMPLEADO.name)) {
            rl_adminEmpleados.isEnabled = true
            rl_adminEmpleados.isVisible = true
        }
    }

    fun subscribeToRealtimeUpdatesLaunch() = CoroutineScope(Dispatchers.IO).launch {
        subscribeToRealtimeUpdates()
    }

    private fun subscribeToRealtimeUpdates() {
        empleadosCollectionRef.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
            var job = CoroutineScope(Dispatchers.IO).launch {
                firebaseFirestoreException?.let {
                    Toast.makeText(view!!.context, it.message, Toast.LENGTH_LONG).show()
                    return@launch
                }
                if(activity!=null) {
                    listaEmpleados.clear()
                    listaIdDocumentos.clear()
                    querySnapshot?.let {
                        for (document in it) {
                            var empleado = document.toObject<Empleado>()
                            listaEmpleados.add(empleado)
                            listaIdDocumentos.add(document.id)
                        }
                        withContext(Dispatchers.Main) {
                            adapter!!.empleadosRv = llenarListaRecyclerView()
                            adapter!!.notifyDataSetChanged()
                        }
                    }
                }

            }//Termina el job
        }
    }

    private suspend fun llenarListaRecyclerView(): ArrayList<EmpleadoRV> {

        var empleadoRvList = ArrayList<EmpleadoRV>()

        for (i in listaEmpleados.indices) {
            var empleadoRv = EmpleadoRV("", "", "", listaIdDocumentos[i], "", R.raw.mreddot,"")
            empleadoRv.nombre = listaEmpleados[i]!!.nombre
            empleadoRv.email = listaEmpleados[i]!!.email
            empleadoRv.fotoPerfil = listaEmpleados[i]!!.fotoPerfil
            empleadoRv.horario = listaEmpleados[i]!!.horario
            empleadoRv.disponibilidad = listaEmpleados[i]!!.disponibilidad
            if(empleadoRv.disponibilidad == Disponibilidades.DISPONIBLE.name) {
                empleadoRv.animacionDisponibilidad = R.raw.mpulsing
            }
            empleadoRvList.add(empleadoRv)
        }

        return empleadoRvList
    }



}
