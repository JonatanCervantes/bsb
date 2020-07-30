package cervantes.jonatan.pruebahorario.servicios.aplicacion

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import cervantes.jonatan.pruebahorario.R
import cervantes.jonatan.pruebahorario.servicios.infraestructura.ServiciosRepository
import kotlinx.android.synthetic.main.dialog_servicio_eliminar.*

class EliminarServicioDialog : DialogFragment() {

    var idServicioEliminar = ""

    lateinit var contexto: Context

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.dialog_servicio_eliminar, container, false)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        contexto = view.context

        tv_noServicio.setOnClickListener {
            dialog!!.dismiss()
        }

        tv_siServicio.setOnClickListener {
            ServiciosRepository.eliminarServicio(idServicioEliminar, contexto)
            dialog!!.dismiss()
        }
    }

}