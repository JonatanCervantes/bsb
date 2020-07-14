package cervantes.jonatan.pruebahorario.dialogs

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
import cervantes.jonatan.pruebahorario.entidades.Empleado
import cervantes.jonatan.pruebahorario.entidades.Servicio
import cervantes.jonatan.pruebahorario.utilidades.CitaAdapter
import cervantes.jonatan.pruebahorario.utilidades.Disponibilidades
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.squareup.okhttp.Dispatcher
import kotlinx.android.synthetic.main.dialog_empleado_agregar.*
import kotlinx.android.synthetic.main.dialog_servicio_agregar.*
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await

private const val REQUEST_CODE_IMAGE_PICK = 0

class AgregarEmpleadoDialog : DialogFragment() {

    private val empleadosCollectionRef = Firebase.firestore.collection("empleados")
    var contextoActivityMain: Context?= null
    private var  curFile: Uri? = null
    private val imagesRef = Firebase.storage.reference

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.dialog_empleado_agregar, container, false)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        contextoActivityMain = view.context

        iv_imagenEmpleado.setOnClickListener {
            Intent(Intent.ACTION_GET_CONTENT).also {
                it.type = "image/*"
                startActivityForResult(it,
                    REQUEST_CODE_IMAGE_PICK
                )
            }
        }

        tv_cancelarEmpleado.setOnClickListener {
            dialog!!.dismiss()
        }

        tv_agregarEmpleado.setOnClickListener {
            var nombre = et_nombreEmpleado.text.toString()

            val imagen = CoroutineScope(Dispatchers.IO).async {
                Log.d("AgregarEmpleadoDialog", "Entrando al upload image empleado")
                uploadImageToStorage("img_${nombre}")
            }

            val job = CoroutineScope(Dispatchers.IO).launch {
                var horario = et_horarioEmpleado.text.toString()
                var email = et_emailEmpleado.text.toString()

                var empleado =  Empleado(0,
                    nombre,
                    email,
                    horario,
                    Disponibilidades.FUERADETURNO.name,
                    imagen.await())
                guardarEmpleado(empleado)
                dialog!!.dismiss()
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

    private suspend fun uploadImageToStorage(fileName: String) : String {
        try {
            curFile?.let {
                imagesRef.child("images/$fileName").putFile(it).await()
                Log.d("AgregarEmpleadoDialog", "Se subio la imagen")
            }
            val urlImagen = imagesRef.child("images/$fileName").downloadUrl.await().toString()

            withContext(Dispatchers.Main) {
                Toast.makeText(contextoActivityMain, "Se subio la imagen al storage y se obtuvo URL", Toast.LENGTH_LONG).show()
            }
            return urlImagen
        } catch (e: Exception) {
            Log.d("AgregarEmpleadoDialog", e.message)
            withContext(Dispatchers.Main) {
                Toast.makeText(contextoActivityMain, e.message, Toast.LENGTH_LONG).show()
            }
        }
        return "SinURLDeImagen"
    }

    private fun guardarEmpleado(empleado: Empleado)  = CoroutineScope(Dispatchers.IO).launch{
        try {
            empleadosCollectionRef.add(empleado).await()
            withContext(Dispatchers.Main) {
                Toast.makeText(contextoActivityMain, "Empleado guardado correctamente", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d("AgregarEmpleadoDialog", e.message)
            withContext(Dispatchers.Main) {
                Toast.makeText(contextoActivityMain, e.message, Toast.LENGTH_LONG).show()
            }
        }
    }


}