package cervantes.jonatan.pruebahorario.ui.servicios

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import cervantes.jonatan.pruebahorario.dialogs.AgregarServicioDialog
import cervantes.jonatan.pruebahorario.R
import cervantes.jonatan.pruebahorario.entidades.Servicio
import cervantes.jonatan.pruebahorario.utilidades.RolUsuario
import cervantes.jonatan.pruebahorario.utilidades.ServicioAdapter
import cervantes.jonatan.pruebahorario.utilidades.ServicioRV
import cervantes.jonatan.pruebahorario.utilidades.TiposUsuario
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_servicios.*
import kotlinx.coroutines.*
import kotlin.collections.ArrayList

class ServiciosFragment : Fragment() {

    private lateinit var serviciosViewModel: ServiciosViewModel
    private var adapter: ServicioAdapter?= null
    private val serviciosCollectionRef = Firebase.firestore.collection("servicios")
    private var listaServicios:ArrayList<Servicio> = ArrayList<Servicio>()
    private var listaIdDocumentos:ArrayList<String> = ArrayList<String>()

    companion object {
        var  eliminarActivado:Boolean = false
        var adminFragmento: FragmentManager ?= null
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        serviciosViewModel =
                ViewModelProviders.of(this).get(ServiciosViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_servicios, container, false)
//        val textView: TextView = root.findViewById(R.id.text_gallery)
//        galleryViewModel.text.observe(viewLifecycleOwner, Observer {
//            textView.text = it
//        })
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adminFragmento = fragmentManager

        var job = CoroutineScope(Dispatchers.IO).launch {
            adapter = ServicioAdapter(llenarListaRecyclerView())
            subscribeToRealtimeUpdates()
        }

        runBlocking {
            job.join()
        }

        rv_servicios.adapter = adapter
        rv_servicios.layoutManager = LinearLayoutManager(view.context)


        val fabAgregarServicio: FloatingActionButton = view.findViewById(R.id.fab_agregarServicio)
        fabAgregarServicio.setOnClickListener { view ->
            var dialog: AgregarServicioDialog =
                AgregarServicioDialog()
            dialog.show(fragmentManager!!, "AgregarServicioDialog")
        }
        val fabEliminarServicio:FloatingActionButton = view.findViewById(R.id.fab_eliminarServicio)
        fabEliminarServicio.setOnClickListener {
            eliminarActivado = !eliminarActivado

            if(eliminarActivado) {
                fabEliminarServicio.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.colorSecundarioFAB))
            } else {
                fabEliminarServicio.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.colorAccent))
            }
        }

        rl_adminServicios.isEnabled = false
        rl_adminServicios.isVisible = false

        habilitarAdminServicios()
    }

    private fun habilitarAdminServicios() {
        val sharedPref = activity?.getSharedPreferences(
            getString(R.string.preference_file_key), Context.MODE_PRIVATE) ?: return
        val rolActual = sharedPref.getString(getString(R.string.rol_usuario), "defaultValue")

        Log.d("EmpleadosFragment", rolActual)
        if(rolActual == (TiposUsuario.EMPLEADO.name)) {
            rl_adminServicios.isEnabled = true
            rl_adminServicios.isVisible = true
        }
    }


    private fun subscribeToRealtimeUpdates(){
        serviciosCollectionRef.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                firebaseFirestoreException?.let {
                    Toast.makeText(view!!.context, it.message, Toast.LENGTH_LONG).show()
                    return@addSnapshotListener
                }

                var job = CoroutineScope(Dispatchers.IO).launch {
                    listaServicios.clear()
                    listaIdDocumentos.clear()
                    querySnapshot?.let {
                        for (document in it) {
                            var servicio = document.toObject<Servicio>()
                            listaServicios.add(servicio)
                            listaIdDocumentos.add(document.id)
                        }
                        withContext(Dispatchers.Main) {
                            adapter!!.serviciosRv = llenarListaRecyclerView()
                            adapter!!.notifyDataSetChanged()
                        }
                    }
                }
            }
    }

    private suspend fun llenarListaRecyclerView(): ArrayList<ServicioRV> {

        var servicioRvList = ArrayList<ServicioRV>()

        for (i in listaServicios.indices) {
            var servicioRv = ServicioRV("", 0.0f, 0, listaIdDocumentos[i], "")
            servicioRv.nombre = listaServicios[i]!!.nombre
            servicioRv.duracion = listaServicios[i]!!.duracion
            servicioRv.precio = listaServicios[i]!!.precio
            servicioRv.imagen = listaServicios[i]!!.imagen

            servicioRvList.add(servicioRv)
        }

        return servicioRvList
    }

}
