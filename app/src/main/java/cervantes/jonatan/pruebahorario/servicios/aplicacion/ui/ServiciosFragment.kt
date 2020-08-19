package cervantes.jonatan.pruebahorario.servicios.aplicacion.ui

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
import cervantes.jonatan.pruebahorario.servicios.aplicacion.AgregarServicioDialog
import cervantes.jonatan.pruebahorario.servicios.aplicacion.ServicioAdapter
import cervantes.jonatan.pruebahorario.servicios.infraestructura.ServiciosRepository
import cervantes.jonatan.pruebahorario.servicios.dominio.ServicioRV
import cervantes.jonatan.pruebahorario.usuarios.dominio.TiposUsuario
import kotlinx.android.synthetic.main.fragment_servicios.*
import kotlinx.coroutines.*
import kotlin.collections.ArrayList

class ServiciosFragment : Fragment() {

    private lateinit var viewModel: ServiciosViewModel
    private var adapter: ServicioAdapter?= null

    companion object {
        var  eliminarActivado:Boolean = false
        var adminFragmento: FragmentManager ?= null
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewModel = ViewModelProviders.of(this).get(ServiciosViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_servicios, container, false)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adminFragmento = fragmentManager

        val adapterDeferred = inicializarAdapterVacioAsync()

        CoroutineScope(Dispatchers.Main).launch {
            adapter = adapterDeferred.await()
            rv_servicios.adapter = adapter
            rv_servicios.layoutManager = LinearLayoutManager(view.context)
        }

        rl_adminServicios.isEnabled = false
        rl_adminServicios.isVisible = false

        habilitarAdminServicios()

        fab_agregarServicio.setOnClickListener { view ->
            var dialog: AgregarServicioDialog =
                AgregarServicioDialog()
            dialog.show(fragmentManager!!, "AgregarServicioDialog")
        }

        fab_eliminarServicio.setOnClickListener {
            viewModel.cambiarEstado(0)

        }

        viewModel.eliminarActivado.observe(this, Observer {
            eliminarActivado = viewModel.obtenerEliminarActivado()
            if(eliminarActivado) {
                fab_eliminarServicio.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.colorSecundarioFAB))
            } else {
                fab_eliminarServicio.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.colorAccent))
            }
        })

        viewModel.listaServiciosRV.observe(this, Observer {
            CoroutineScope(Dispatchers.Main).launch {
                adapter!!.serviciosRv = it
                adapter!!.notifyDataSetChanged()
            }
        })

        ServiciosRepository.listaServicios.observe(this, Observer {
            viewModel.llenarListaRecyclerView(it)
        })

    }

    private fun inicializarAdapterVacioAsync() = CoroutineScope(Dispatchers.Default).async {
        inicializarAdapterVacio()
    }

    private suspend fun inicializarAdapterVacio() : ServicioAdapter {
            var servicioRvList = ArrayList<ServicioRV>()
            return ServicioAdapter(
                servicioRvList
            )
    }

    private fun habilitarAdminServicios() {
        val sharedPref = activity?.getSharedPreferences(
            getString(R.string.preference_file_key), Context.MODE_PRIVATE) ?: return
        val rolActual = sharedPref.getString(getString(R.string.rol_usuario), "defaultValue")

        Log.d("ServiciosFragment", rolActual)
        if(rolActual == (TiposUsuario.EMPLEADO.name)) {
            rl_adminServicios.isEnabled = true
            rl_adminServicios.isVisible = true
        }
    }


}
