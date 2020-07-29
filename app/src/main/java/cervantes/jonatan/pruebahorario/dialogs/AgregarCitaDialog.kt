package cervantes.jonatan.pruebahorario.dialogs

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.DialogFragment
import cervantes.jonatan.pruebahorario.R
import cervantes.jonatan.pruebahorario.entidades.*
import cervantes.jonatan.pruebahorario.firebase.CitasRepository
import cervantes.jonatan.pruebahorario.firebase.EmpleadosRepository
import cervantes.jonatan.pruebahorario.firebase.ServiciosRepository
import cervantes.jonatan.pruebahorario.firebase.UsuariosRepository
import cervantes.jonatan.pruebahorario.utilidades.Connectivity
import com.bumptech.glide.Glide
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.dialog_cita_agregar.*
import kotlinx.android.synthetic.main.mini_empleado_servicio_view.view.*
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

class AgregarCitaDialog(val fechaCita:Calendar) : DialogFragment() {
    lateinit var auth: FirebaseAuth
    lateinit var contexto: Context
    private var cliente:Usuario ?= null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_cita_agregar, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        contexto = view.context

        CoroutineScope(Dispatchers.Default).launch {
            auth = FirebaseAuth.getInstance()
            cliente = UsuariosRepository.obtenerUsuarioActualLaunch(auth.uid!!).await()
        }

        CoroutineScope(Dispatchers.Main).launch {
            inflarEmpleados()
            inflarServicios()
        }

        tv_cancel.setOnClickListener {
            dialog!!.dismiss()
        }

        tv_ok.setOnClickListener {
            if(Connectivity.isOnline(contexto)) {
                lateinit var loadingDialog:LoadingDialog
                val job = CoroutineScope(Dispatchers.IO).launch {
                    val timeout = withTimeout(15000) {
                        try {
                            var idCita = 7
                            var fecha = Timestamp(fechaCita.time)

                            var cliente = cliente
                            var empleado = empleadoSeleccionado
                            var servicio = servicioSeleccionado

                            if(cliente != null && empleado != null && servicio != null && fecha != null) {
                                loadingDialog = LoadingDialog()
                                loadingDialog.show(fragmentManager!!, "LoadingDialog")
                                var cita:Cita = Cita(idCita, cliente!!, empleado!!, servicio!!, fecha)

                                val trabajoGuardarCita = CitasRepository.guardarCita(cita, contexto)

                                trabajoGuardarCita.invokeOnCompletion {
                                    loadingDialog.changeAnimationLaunch(true)
                                    dialog?.dismiss()
                                }

                            } else {
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(contexto, "Porfavor seleccione los detalles de la cita", Toast.LENGTH_LONG).show()
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
                Toast.makeText(contexto, "Porfavor revise su conexion a internet", Toast.LENGTH_LONG).show()
            }
        }

        tv_fechaCita.text = "Fecha seleccionada: ".plus(Editable.Factory.getInstance().newEditable(SimpleDateFormat("EEE dd/MM/yyyy")
            .format(fechaCita.time)))
        tv_horaCita.text = "Hora: ${fechaCita.get(Calendar.HOUR_OF_DAY).toString()}:${fechaCita.get(Calendar.MINUTE).toString()}"
    }

    private fun inflarEmpleados() {
        val listaEmpleados = EmpleadosRepository.obtenerListaEmpleados()
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


    private fun inflarServicios() {
        val listaServicios = ServiciosRepository.obtenerListaServicios()
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



}