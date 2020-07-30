package cervantes.jonatan.pruebahorario.empleados.aplicacion

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cervantes.jonatan.pruebahorario.R
import cervantes.jonatan.pruebahorario.empleados.aplicacion.ui.EmpleadosFragment
import cervantes.jonatan.pruebahorario.empleados.dominio.EmpleadoRV
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.empleado_view.view.*

class EmpleadoAdapter(var empleadosRv: List<EmpleadoRV>) : RecyclerView.Adapter<EmpleadoAdapter.EmpleadoViewHolder>() {

    inner class EmpleadoViewHolder(itemView:View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmpleadoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.empleado_view, parent, false)
        return EmpleadoViewHolder(view)
    }

    override fun getItemCount(): Int {
        return empleadosRv.size
    }

    override fun onBindViewHolder(holder: EmpleadoViewHolder, position: Int) {
        holder.itemView.apply {
            tv_nombreEmpleado.text = empleadosRv[position].nombre
            tv_horarioEmpleado.text = "Horario: " + empleadosRv[position].horario
            tv_disponibilidadEmpleado.text = "Disponibilidad: " + empleadosRv[position].disponibilidad
            icon_disponibilidad.setAnimation(empleadosRv[position].animacionDisponibilidad)
            Glide.with(this).load(empleadosRv[position].fotoPerfil).into(this.iv_imagenEmpleado)
        }

        holder.itemView.setOnClickListener {
            if(EmpleadosFragment.eliminarActivado) {
                var dialog: EliminarEmpleadoDialog =
                    EliminarEmpleadoDialog()
                dialog.idEmpleadoEliminar = empleadosRv[position].idDocumento
                dialog.show(EmpleadosFragment.adminFragmento!!, "EliminarEmpleadoDialog")
            }

            if(EmpleadosFragment.modificarActivado) {
                var dialog: ModificarEmpleadoDialog =
                    ModificarEmpleadoDialog()
                dialog.idEmpleadoModificar = empleadosRv[position].idDocumento
                dialog.show(EmpleadosFragment.adminFragmento!!, "ModificarEmpleadoDialog")
            }

        }
    }


}