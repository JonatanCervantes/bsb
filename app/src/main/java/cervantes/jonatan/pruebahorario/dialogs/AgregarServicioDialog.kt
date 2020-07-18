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
import cervantes.jonatan.pruebahorario.entidades.Servicio
import cervantes.jonatan.pruebahorario.utilidades.CitaAdapter
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.squareup.okhttp.Dispatcher
import kotlinx.android.synthetic.main.dialog_servicio_agregar.*
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await

private const val REQUEST_CODE_IMAGE_PICK = 0

class AgregarServicioDialog : DialogFragment() {

    private val serviciosCollectionRef = Firebase.firestore.collection("servicios")
    var contextoActivityMain: Context?= null
    private var  curFile: Uri? = null
    private val imagesRef = Firebase.storage.reference

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.dialog_servicio_agregar, container, false)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        contextoActivityMain = view.context

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
            var nombre = et_nombreServicio.text.toString()

            val imagen = CoroutineScope(Dispatchers.IO).async {
                Log.d("AgregarServicioDialog", "Entrando al upload image")
                uploadImageToStorage("img_${nombre}")
            }

            val job = CoroutineScope(Dispatchers.IO).launch {
                var duracion = et_duracionServicio.text.toString().toInt()
                var precio = et_precioServicio.text.toString().toFloat()

                if(duracion == 0 || precio == 0.0f || nombre == "" || imagen == null) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(contextoActivityMain, "Porfavor llene todos los campos", Toast.LENGTH_LONG).show()
                    }
                } else {
                    var servicio =  Servicio(0, nombre, precio, duracion, imagen.await())
                    guardarServicio(servicio)
                    dialog!!.dismiss()
                }
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

    private suspend fun uploadImageToStorage(fileName: String) : String {
        try {
            curFile?.let {
                imagesRef.child("images/$fileName").putFile(it).await()
                Log.d("AgregarServicioDialog", "Se subio la imagen")
            }
            val urlImagen = imagesRef.child("images/$fileName").downloadUrl.await().toString()

            withContext(Dispatchers.Main) {
                Toast.makeText(contextoActivityMain, "Se subio la imagen al storage y se obtuvo URL", Toast.LENGTH_LONG).show()
            }
            return urlImagen
        } catch (e: Exception) {
            Log.d("AgregarServicioDialog", e.message)
            withContext(Dispatchers.Main) {
                Toast.makeText(contextoActivityMain, e.message, Toast.LENGTH_LONG).show()
            }
        }
        return "noImageUrl"
    }

    private fun guardarServicio(servicio: Servicio)  = CoroutineScope(Dispatchers.IO).launch{
        try {
            serviciosCollectionRef.add(servicio).await()
            withContext(Dispatchers.Main) {
                Toast.makeText(contextoActivityMain, "Servicio guardado correctamente", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {

            e.printStackTrace()
            Log.d("AgregarServicioDialog", e.message)
            withContext(Dispatchers.Main) {
                Toast.makeText(contextoActivityMain, e.message, Toast.LENGTH_LONG).show()
            }
        }
    }


}