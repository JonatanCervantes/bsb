package cervantes.jonatan.pruebahorario.citas.aplicacion

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import cervantes.jonatan.pruebahorario.R
import cervantes.jonatan.pruebahorario.citas.infraestructura.CitasRepository
import cervantes.jonatan.pruebahorario.utilidades.Connectivity
import kotlinx.android.synthetic.main.dialog_cita_eliminar.*

class EliminarCitaDialog : DialogFragment() {

    var idDocumentoEliminar = ""
    lateinit var contexto: Context

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_cita_eliminar, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        contexto = view.context

        tv_no.setOnClickListener {
            dialog!!.dismiss()
        }

        tv_si.setOnClickListener {
            if(Connectivity.isOnline(contexto)) {
                CitasRepository.eliminarCita(idDocumentoEliminar, contexto)
                dialog!!.dismiss()
            } else {
                Toast.makeText(contexto, resources.getString(R.string.avisoInternet), Toast.LENGTH_LONG).show()
            }


        }
    }

}