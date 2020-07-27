package cervantes.jonatan.pruebahorario.dialogs

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
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
import com.bumptech.glide.Glide
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.dialog_cita_agregar.*
import kotlinx.android.synthetic.main.mini_empleado_servicio_view.view.*
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class AgregarCitaDialog : DialogFragment() {

    private lateinit var tv_texto: TextView
    private lateinit var tv_ok: TextView
    private lateinit var tv_cancel: TextView
    private lateinit var tv_fechaCita: TextView

    private val citaCollectionRef= Firebase.firestore.collection("citas")
    private val empleadosCollectionRef = Firebase.firestore.collection("empleados")
    private var listaEmpleados:ArrayList<Empleado> = ArrayList<Empleado>()
    private var listaIdDocumentosEmpleados:ArrayList<String> = ArrayList<String>()
    private val serviciosCollectionRef = Firebase.firestore.collection("servicios")
    private var listaServicios:ArrayList<Servicio> = ArrayList<Servicio>()
    private var listaIdDocumentosServicios:ArrayList<String> = ArrayList<String>()
    private val usuariosCollectionRef = Firebase.firestore.collection("usuarios")
    lateinit var auth: FirebaseAuth

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

        auth = FirebaseAuth.getInstance()

        obtenerUsuarioLaunch()

        tv_cancel.setOnClickListener {
            CitasFragment.inicializarFechas()
            dialog!!.dismiss()
        }

        tv_ok.setOnClickListener {
            if(isOnline(contextoActivityMain!!)) {
                lateinit var loadingDialog:LoadingDialog
                val job = CoroutineScope(Dispatchers.IO).launch {
                    val timeout = withTimeout(15000) {
                        try {
                            var idCita = 7
                            var fecha = Timestamp(CitasFragment.fechaSeleccionada.time)

                            var cliente = cliente
                            var empleado = empleadoSeleccionado
                            var servicio = servicioSeleccionado

                            if(cliente != null && empleado != null && servicio != null && fecha != null) {
                                loadingDialog = LoadingDialog()
                                loadingDialog.show(fragmentManager!!, "LoadingDialog")
                                var cita:Cita = Cita(idCita, cliente, empleado!!, servicio!!, fecha)

                                val trabajoGuardarCita = guardarCita(cita)

                                trabajoGuardarCita.invokeOnCompletion {
                                    loadingDialog.changeAnimationLaunch(true)
                                    CitasFragment.inicializarFechas()
                                    dialog?.dismiss()
                                }

                            } else {
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(contextoActivityMain, "Porfavor seleccione los detalles de la cita", Toast.LENGTH_LONG).show()
                                }
                            }

                        } catch (e: TimeoutCancellationException) {
                            e.printStackTrace()
                            loadingDialog.changeAnimationLaunch(false)
                            dialog?.dismiss()
                        }
                    }
                }
            } else {

                Toast.makeText(contextoActivityMain, "Porfavor revise su conexion a internet", Toast.LENGTH_LONG).show()

            }
        }


        tv_fechaCita.text = "Fecha seleccionada: ".plus(Editable.Factory.getInstance().newEditable(SimpleDateFormat("EEE dd/MM/yyyy")
            .format(CitasFragment.fechaSeleccionada.time)))
        tv_horaCita.text = "Hora: " + CitasFragment.fechaSeleccionada.get(Calendar.HOUR_OF_DAY).toString() + ":" + CitasFragment.fechaSeleccionada.get(Calendar.MINUTE).toString()

        //configurarSpinnerEmpleados(view)
        //configurarSpinnerServicios(view)
        subscribeToRealTimeupdatesEmpleadosLaunch()
        subscribeToRealtimeUpdatesServiciosLaunch()
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

    fun subscribeToRealTimeupdatesEmpleadosLaunch() = CoroutineScope(Dispatchers.Main).launch {
        subscribeToRealtimeEmpleadosUpdates()
    }

    private fun subscribeToRealtimeEmpleadosUpdates() {
        empleadosCollectionRef.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
            firebaseFirestoreException?.let {
                Toast.makeText(view!!.context, it.message, Toast.LENGTH_LONG).show()
                return@addSnapshotListener
            }

            var job = CoroutineScope(Dispatchers.IO).launch {
                listaEmpleados.clear()
                listaIdDocumentosEmpleados.clear()
                querySnapshot?.let {
                    for (document in it) {
                        var empleado = document.toObject<Empleado>()
                        listaEmpleados.add(empleado)
                        listaIdDocumentosEmpleados.add(document.id)
                    }
                    withContext(Dispatchers.Main) {
                        inflarEmpleados()
                    }
                }
            }
        }
    }

    private fun inflarEmpleados() {
        for (i in listaEmpleados.indices) {
            var vista = layoutInflater.inflate(R.layout.mini_empleado_servicio_view, null)
            Glide.with(this).load(listaEmpleados[i].fotoPerfil).into(vista.iv_imagenEmpleadoServicio)
            vista.tv_nombreEmpleadoServicio.text = listaEmpleados[i].nombre
            vista.iv_imagenEmpleadoServicio.setOnClickListener {
                seleccionarEmpleado(vista.iv_imagenEmpleadoServicio, listaEmpleados[i])
            }
            ll_viewEmpleados.addView(vista)
        }
    }

    private var empleadoSeleccionado: Empleado ?= null
    private var imageViewSeleccionada: ImageView ?=null

    fun seleccionarEmpleado(imageView: ImageView, empleado:Empleado) {
        imageViewSeleccionada?.alpha = 1.0f

        empleadoSeleccionado = empleado
        imageViewSeleccionada = imageView
        imageViewSeleccionada?.alpha = 0.4f
    }

    fun subscribeToRealtimeUpdatesServiciosLaunch() = CoroutineScope(Dispatchers.IO).launch {
        subscribeToRealtimeServiciosUpdates()
    }


    private fun subscribeToRealtimeServiciosUpdates(){
        serviciosCollectionRef.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
            firebaseFirestoreException?.let {
                Toast.makeText(view!!.context, it.message, Toast.LENGTH_LONG).show()
                return@addSnapshotListener
            }

            var job = CoroutineScope(Dispatchers.IO).launch {
                listaServicios.clear()
                listaIdDocumentosServicios.clear()
                querySnapshot?.let {
                    for (document in it) {
                        var servicio = document.toObject<Servicio>()
                        listaServicios.add(servicio)
                        listaIdDocumentosServicios.add(document.id)
                    }
                    withContext(Dispatchers.Main) {
                        inflarServicios()
                    }
                }
            }
        }
    }


    private fun inflarServicios() {
        for (i in listaServicios.indices) {
            var vista = layoutInflater.inflate(R.layout.mini_empleado_servicio_view, null)
            Glide.with(this).load(listaServicios[i].imagen).into(vista.iv_imagenEmpleadoServicio)
            vista.tv_nombreEmpleadoServicio.text = listaServicios[i].nombre
            vista.iv_imagenEmpleadoServicio.setOnClickListener {
                seleccionarServicio(vista.iv_imagenEmpleadoServicio, listaServicios[i])
            }

            ll_viewServicios.addView(vista)
        }
    }


    private var servicioSeleccionado: Servicio ?= null
    private var iamgeViewServicioSeleccionada: View ?= null

    fun seleccionarServicio(imageView: ImageView, servicio:Servicio) {
        iamgeViewServicioSeleccionada?.alpha = 1.0f

        servicioSeleccionado = servicio
        iamgeViewServicioSeleccionada = imageView
        iamgeViewServicioSeleccionada?.alpha = 0.4f
    }

    private fun obtenerUsuarioLaunch() = CoroutineScope(Dispatchers.IO).async {
        obtenerUsuario()
    }

    private var cliente:Usuario ?= null

    private fun obtenerUsuario() {
        val coincidencias = CoroutineScope(Dispatchers.IO).launch {
            try {
                val querySnapshot = usuariosCollectionRef.whereEqualTo("idUsuario", auth.uid).get().await()
                cliente = querySnapshot.documents[0].toObject<Usuario>()!!
            } catch (e:Exception) {
                Log.d("LoginActivity", e.message)
            }
        }

    }

    fun isOnline(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (connectivityManager != null) {
            val capabilities =
                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            if (capabilities != null) {
                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_CELLULAR")
                    return true
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_WIFI")
                    return true
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_ETHERNET")
                    return true
                }
            }
        }
        return false
    }








}