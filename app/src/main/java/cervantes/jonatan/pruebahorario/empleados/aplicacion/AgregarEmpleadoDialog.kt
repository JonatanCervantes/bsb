package cervantes.jonatan.pruebahorario.empleados.aplicacion

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.DialogFragment
import cervantes.jonatan.pruebahorario.R
import cervantes.jonatan.pruebahorario.utilidades.LoadingDialog
import cervantes.jonatan.pruebahorario.empleados.dominio.Empleado
import cervantes.jonatan.pruebahorario.empleados.infraestructura.EmpleadosRepository
import cervantes.jonatan.pruebahorario.notificaciones.FirebaseService
import cervantes.jonatan.pruebahorario.utilidades.Connectivity
import cervantes.jonatan.pruebahorario.utilidades.Disponibilidades
import cervantes.jonatan.pruebahorario.utilidades.Par
import kotlinx.android.synthetic.main.dialog_empleado_agregar.*
import kotlinx.coroutines.*

private const val REQUEST_CODE_IMAGE_PICK = 0
const val TIEMPO_TIMEOUT = 15000L

class AgregarEmpleadoDialog : DialogFragment() {

    lateinit var contexto: Context
    private var  curFile: Uri? = null
    var horariosMap:HashMap<String, ArrayList<Par>> = HashMap()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.dialog_empleado_agregar, container, false)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        contexto = view.context

        iv_imagenEmpleado.setOnClickListener {
            Intent(Intent.ACTION_GET_CONTENT).also {
                it.type = "image/*"
                startActivityForResult(it,
                    REQUEST_CODE_IMAGE_PICK
                )
            }
        }

        et_horarioEmpleado.setOnClickListener {
            var dialog: SeleccionarHorarioDialog =
                SeleccionarHorarioDialog(this)
            dialog.show(fragmentManager!!, "SeleccionarHorarioDialog")
        }

        tv_cancelarEmpleado.setOnClickListener {
            dialog!!.dismiss()
        }

        tv_agregarEmpleado.setOnClickListener {
            if(Connectivity.isOnline(contexto)) {
                lateinit var loadingDialog: LoadingDialog
                var nombre = et_nombreEmpleado.text.toString()

                val job = CoroutineScope(Dispatchers.IO).launch {
                    var horario = et_horarioEmpleado.text.toString()
                    var email = et_emailEmpleado.text.toString()

                    if(nombre == "" || horario == "" || email == "" || curFile == null) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(contexto, "Porfavor llene todos los campos", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        try {
                            withTimeout(5000) {
                                loadingDialog =
                                    LoadingDialog()
                                loadingDialog.show(fragmentManager!!, "LoadingDialog")

                                val imagen = EmpleadosRepository.uploadImageToStorageAsync("img_${nombre}", curFile, contexto)
                                val token = FirebaseService.token
                                var empleado =
                                    Empleado(
                                        0, nombre, email, horario, horariosMap,
                                        Disponibilidades.FUERADETURNO.name,
                                        imagen.await(),
                                        token = token!!
                                    )
                                val trabajoGuardarEmpleado = EmpleadosRepository.guardarEmpleado(empleado, contexto)
                                trabajoGuardarEmpleado.invokeOnCompletion {
                                    loadingDialog.changeAnimationLaunch(true)
                                    dialog!!.dismiss()
                                }
                            }
                        } catch (e: TimeoutCancellationException ) {
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
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_IMAGE_PICK) {
            data?.data?.let {
                curFile = it
                iv_imagenEmpleado.setImageURI(curFile)
            }
        }
    }

    fun establecerHorario(horario:String, horariosMap:HashMap<String, ArrayList<Par>>){
        et_horarioEmpleado.text = Editable.Factory.getInstance().newEditable(horario)
        this.horariosMap = horariosMap
    }

}