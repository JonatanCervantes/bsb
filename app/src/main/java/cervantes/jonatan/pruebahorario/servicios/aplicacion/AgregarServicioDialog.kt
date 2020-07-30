package cervantes.jonatan.pruebahorario.servicios.aplicacion

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.DialogFragment
import cervantes.jonatan.pruebahorario.R
import cervantes.jonatan.pruebahorario.empleados.aplicacion.TIEMPO_TIMEOUT
import cervantes.jonatan.pruebahorario.servicios.dominio.Servicio
import cervantes.jonatan.pruebahorario.servicios.infraestructura.ServiciosRepository
import cervantes.jonatan.pruebahorario.utilidades.Connectivity
import cervantes.jonatan.pruebahorario.utilidades.LoadingDialog
import kotlinx.android.synthetic.main.dialog_servicio_agregar.*
import kotlinx.coroutines.*

private const val REQUEST_CODE_IMAGE_PICK = 0

class AgregarServicioDialog : DialogFragment() {

    lateinit var contexto: Context
    private var  curFile: Uri? = null
    private val TAG = "AgregarServicioDialog"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.dialog_servicio_agregar, container, false)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        contexto = view.context

        iv_imagenServicio.setOnClickListener {
            Intent(Intent.ACTION_GET_CONTENT).also {
                it.type = "image/*"
                startActivityForResult(it,
                    REQUEST_CODE_IMAGE_PICK
                )
            }
        }

        tv_cancelarServicio.setOnClickListener {
            dialog!!.dismiss()
        }

        tv_agregarServicio.setOnClickListener {
            if(Connectivity.isOnline(contexto)) {
                lateinit var loadingDialog: LoadingDialog
                var duracion = 0
                var precio = 0.0f

                var nombre = et_nombreServicio.text.toString()

                val job = CoroutineScope(Dispatchers.IO).launch {
                    try {
                        duracion = et_duracionServicio.text.toString().toInt()
                        precio = et_precioServicio.text.toString().toFloat()
                    } catch (e: Exception) {
                        Log.d(TAG, e.message)
                    }

                    if(duracion == 0 || precio == 0.0f || nombre == "" || curFile == null) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(contexto, "Porfavor llene todos los campos", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        try {
                            withTimeout(TIEMPO_TIMEOUT) {
                                loadingDialog =
                                    LoadingDialog()
                                loadingDialog.show(fragmentManager!!, "LoadingDialog")

                                val imagen =  ServiciosRepository.uploadImageToStorageAsync("img_${nombre}", curFile, contexto)
                                var servicio =
                                    Servicio(
                                        0,
                                        nombre,
                                        precio,
                                        duracion,
                                        imagen.await()
                                    )
                                val trabajoGuardarServicio = ServiciosRepository.guardarServicio(servicio, contexto)
                                trabajoGuardarServicio.invokeOnCompletion {
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
                iv_imagenServicio.setImageURI(curFile)
            }
        }
    }


}