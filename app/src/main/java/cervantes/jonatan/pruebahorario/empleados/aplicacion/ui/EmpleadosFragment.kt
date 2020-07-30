package cervantes.jonatan.pruebahorario.empleados.aplicacion.ui

import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import cervantes.jonatan.pruebahorario.R
import cervantes.jonatan.pruebahorario.empleados.aplicacion.AgregarEmpleadoDialog
import cervantes.jonatan.pruebahorario.empleados.aplicacion.EmpleadoAdapter
import cervantes.jonatan.pruebahorario.empleados.dominio.EmpleadoRV
import cervantes.jonatan.pruebahorario.empleados.infraestructura.EmpleadosRepository
import cervantes.jonatan.pruebahorario.usuarios.dominio.TiposUsuario
import kotlinx.android.synthetic.main.fragment_empleados.*
import kotlinx.coroutines.*

class EmpleadosFragment : Fragment() {

    private lateinit var viewModel: EmpleadosViewModel
    private lateinit var adapter: EmpleadoAdapter

    companion object {
        var  eliminarActivado:Boolean = false
        var  modificarActivado:Boolean = false
        var adminFragmento: FragmentManager?= null
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewModel = ViewModelProviders.of(this).get(EmpleadosViewModel::class.java)
        return inflater.inflate(R.layout.fragment_empleados, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adminFragmento = fragmentManager

        val adapterDeferred = inicializarAdapterVacioAsync()

        CoroutineScope(Dispatchers.Main).launch {
            adapter = adapterDeferred.await()
            rv_empleados.adapter = adapter
            rv_empleados.layoutManager = LinearLayoutManager(view.context)
        }

        rl_adminEmpleados.isEnabled = false
        rl_adminEmpleados.isVisible = false

        hablitarAdminEmpleados()

        fab_agregarEmpleado.setOnClickListener { view ->
            var dialog: AgregarEmpleadoDialog =
                AgregarEmpleadoDialog()
            dialog.show(fragmentManager!!, "AgregarEmpleadoDialog")
        }

        fab_eliminarEmpleado.setOnClickListener {
            viewModel.cambiarEstado(0)
        }

        fab_modificarEmpleado.setOnClickListener {
            viewModel.cambiarEstado(1)
        }

        viewModel.eliminarActivado.observe(this, Observer {
            eliminarActivado = viewModel.obtenerEliminarActivado()
            if(eliminarActivado) {
                fab_eliminarEmpleado.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.colorSecundarioFAB))
            } else {
                fab_eliminarEmpleado.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.colorAccent))
            }
        })

        viewModel.modificarActivado.observe(this, Observer {
            modificarActivado = viewModel.obtenerModificarActivado()
            if(modificarActivado) {
                fab_modificarEmpleado.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.colorSecundarioFAB))
            } else {
                fab_modificarEmpleado.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.colorAccent))
            }
        })

        viewModel.listaEmpleadosRV.observe(this, Observer {
            CoroutineScope(Dispatchers.Main).launch {
                adapter.empleadosRv = it
                adapter.notifyDataSetChanged()
            }
        })

        EmpleadosRepository.listaEmpleados.observe(this, Observer {
            viewModel.llenarListaRecyclerView(it)
        })
    }

    private fun inicializarAdapterVacioAsync() = CoroutineScope(Dispatchers.Default).async {
        inicializarAdapterVacio()
    }

    private suspend fun inicializarAdapterVacio() : EmpleadoAdapter {
        var empleadoRvList = ArrayList<EmpleadoRV>()
        return EmpleadoAdapter(
            empleadoRvList
        )
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
}
