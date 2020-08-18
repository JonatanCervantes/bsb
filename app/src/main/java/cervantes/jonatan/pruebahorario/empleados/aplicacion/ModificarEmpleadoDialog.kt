package cervantes.jonatan.pruebahorario.empleados.aplicacion

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import cervantes.jonatan.pruebahorario.R
import cervantes.jonatan.pruebahorario.empleados.infraestructura.EmpleadosRepository
import cervantes.jonatan.pruebahorario.utilidades.Connectivity
import cervantes.jonatan.pruebahorario.utilidades.Disponibilidades
import kotlinx.android.synthetic.main.dialog_empleado_modificar.*

class ModificarEmpleadoDialog : DialogFragment() {

    var idEmpleadoModificar = ""
    lateinit var contexto: Context

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.dialog_empleado_modificar, container, false)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        contexto = view.context

        tv_noModificarEmpleado.setOnClickListener {
            dialog!!.dismiss()
        }

        tv_siModificarEmpleado.setOnClickListener {
            val nuevaDisponibilidad = revisarSeleccion()
            if(nuevaDisponibilidad != "-1") {
                if(Connectivity.isOnline(contexto)) {
                    EmpleadosRepository.modificarEmpleado(idEmpleadoModificar, nuevaDisponibilidad, contexto)
                    dialog!!.dismiss()
                } else {
                    Toast.makeText(contexto, resources.getString(R.string.avisoInternet), Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(contexto, "Porfavor seleccione una disponibilidad", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun revisarSeleccion(): String {
        var nuevaDisponibilidad = "-1"
        when {
            rb_disponible.isChecked -> {
                nuevaDisponibilidad = Disponibilidades.DISPONIBLE.name
            }
            rb_ocupado.isChecked -> {
                nuevaDisponibilidad = Disponibilidades.OCUPADO.name
            }
            rb_fueraDeTurno.isChecked -> {
                nuevaDisponibilidad = Disponibilidades.FUERADETURNO.name
            }
        }

        return nuevaDisponibilidad
    }

}