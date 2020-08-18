package cervantes.jonatan.pruebahorario.citas.aplicacion

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.DialogFragment
import cervantes.jonatan.pruebahorario.R
import cervantes.jonatan.pruebahorario.citas.dominio.Cita
import cervantes.jonatan.pruebahorario.empleados.dominio.Empleado
import cervantes.jonatan.pruebahorario.citas.infraestructura.CitasRepository
import cervantes.jonatan.pruebahorario.empleados.infraestructura.EmpleadosRepository
import cervantes.jonatan.pruebahorario.servicios.infraestructura.ServiciosRepository
import cervantes.jonatan.pruebahorario.usuarios.infraestructura.UsuariosRepository
import cervantes.jonatan.pruebahorario.servicios.dominio.Servicio
import cervantes.jonatan.pruebahorario.usuarios.dominio.Usuario
import cervantes.jonatan.pruebahorario.utilidades.Connectivity
import cervantes.jonatan.pruebahorario.utilidades.LoadingDialog
import com.bumptech.glide.Glide
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.dialog_cita_agregar.*
import kotlinx.android.synthetic.main.mini_empleado_servicio_view.view.*
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

class AgregarCitaDialog(val fechaCita:Calendar, val idEmpleadoSeleccionado:String) : DialogFragment() {
    lateinit var auth: FirebaseAuth
    lateinit var contexto: Context
    private var cliente: Usuario?= null

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
                lateinit var loadingDialog: LoadingDialog
                val job = CoroutineScope(Dispatchers.IO).launch {
                    val timeout = withTimeout(15000) {
                        try {
                            var idCita = 7
                            var fechaInicio = Timestamp(fechaCita.time)
                            var cliente = cliente
                            var empleado = empleadoSeleccionado
                            var servicio = servicioSeleccionado

                            if(cliente != null && empleado != null && servicio != null) {
                                loadingDialog = LoadingDialog()
                                loadingDialog.show(fragmentManager!!, "LoadingDialog")
                                var fechaTerminoCalendar = (fechaCita.clone() as Calendar).also {
                                    it.add(Calendar.MINUTE, servicio.duracion)
                                }
                                val fechaTermino = Timestamp(fechaTerminoCalendar.time)

                                var cita = Cita(idCita, cliente!!, empleado!!, servicio!!,
                                    fechaInicio,
                                    fechaTermino
                                    )

                                if(CitasRepository.verificarDisponibilidadCita(cita)){
                                    val trabajoGuardarCita = CitasRepository.guardarCita(cita, contexto)

                                    trabajoGuardarCita.invokeOnCompletion {
                                        loadingDialog.changeAnimationLaunch(true)
                                        dialog?.dismiss()
                                    }
                                } else {
                                    loadingDialog.changeAnimationLaunch(false)
                                    dialog?.dismiss()
                                }
                            } else {
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(contexto, resources.getString(R.string.avisoCita), Toast.LENGTH_LONG).show()
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
                Toast.makeText(contexto, resources.getString(R.string.avisoInternet), Toast.LENGTH_LONG).show()
            }
        }

        tv_fechaCita.text = resources.getString(R.string.fechaSeleccionada).plus(Editable.Factory.getInstance().newEditable(SimpleDateFormat("EEE dd/MM/yyyy")
            .format(fechaCita.time)))

        val minutosLength = fechaCita.get(Calendar.MINUTE).toString().length
        var compensador = ""
        if(minutosLength <2)
            compensador = "0"
        tv_horaCita.text = resources.getString(R.string.hora).plus(fechaCita.get(Calendar.HOUR_OF_DAY).toString()).plus(":").plus(fechaCita.get(Calendar.MINUTE).toString()).plus(compensador)
    }

    private fun inflarEmpleados() {
        val listaEmpleados = arrayListOf(EmpleadosRepository.obtenerEmpleado(idEmpleadoSeleccionado))
        for (i in listaEmpleados.indices) {
            var vista = layoutInflater.inflate(R.layout.mini_empleado_servicio_view, null)
            Glide.with(this).load(listaEmpleados[i]!!.fotoPerfil).into(vista.iv_imagenEmpleadoServicio)
            vista.tv_nombreEmpleadoServicio.text = listaEmpleados[i]!!.nombre
            vista.iv_imagenEmpleadoServicio.setOnClickListener {
                seleccionarEmpleado(vista.iv_imagenEmpleadoServicio, listaEmpleados[i]!!)
            }
            ll_viewEmpleados.addView(vista)
        }
    }

    private var empleadoSeleccionado: Empleado?= null
    private var imageViewSeleccionada: ImageView ?=null

    fun seleccionarEmpleado(imageView: ImageView, empleado: Empleado) {
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

    private var servicioSeleccionado: Servicio?= null
    private var iamgeViewServicioSeleccionada: View ?= null

    fun seleccionarServicio(imageView: ImageView, servicio: Servicio) {
        iamgeViewServicioSeleccionada?.alpha = 1.0f

        servicioSeleccionado = servicio
        iamgeViewServicioSeleccionada = imageView
        iamgeViewServicioSeleccionada?.alpha = 0.4f
        tv_duracionAproximada.text = resources.getString(R.string.duracionAproximada).plus(servicio.duracion).plus(resources.getString(R.string.minutos))
        tv_precioAgregarCita.text = resources.getString(R.string.precio).plus(servicio.precio.toString())
    }



}