package cervantes.jonatan.pruebahorario.empleados.aplicacion

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import cervantes.jonatan.pruebahorario.R
import cervantes.jonatan.pruebahorario.empleados.infraestructura.EmpleadosRepository
import kotlinx.android.synthetic.main.dialog_empleado_eliminar.*

class EliminarEmpleadoDialog : DialogFragment() {

    var idEmpleadoEliminar = ""
    lateinit var contexto: Context

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.dialog_empleado_eliminar, container, false)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        contexto = view.context

        tv_noEmpleado.setOnClickListener {
            dialog!!.dismiss()
        }

        tv_siEmpleado.setOnClickListener {
            EmpleadosRepository.eliminarEmpleado(idEmpleadoEliminar, contexto)
            dialog!!.dismiss()
        }
    }

}