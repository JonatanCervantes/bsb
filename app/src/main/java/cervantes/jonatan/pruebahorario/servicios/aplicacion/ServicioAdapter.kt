package cervantes.jonatan.pruebahorario.servicios.aplicacion

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cervantes.jonatan.pruebahorario.R
import cervantes.jonatan.pruebahorario.servicios.aplicacion.ui.ServiciosFragment
import cervantes.jonatan.pruebahorario.servicios.dominio.ServicioRV
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.servicio_view.view.*

class ServicioAdapter(var serviciosRv: List<ServicioRV>) : RecyclerView.Adapter<ServicioAdapter.ServicioViewHolder>() {

    inner class ServicioViewHolder(itemView:View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServicioViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.servicio_view, parent, false)
        return ServicioViewHolder(view)
    }

    override fun getItemCount(): Int {
        return serviciosRv.size
    }

    override fun onBindViewHolder(holder: ServicioViewHolder, position: Int) {
        holder.itemView.apply {
            tv_nombreServicio.text = serviciosRv[position].nombre
            tv_duracionServicio.text = "Duracion aprox: " + serviciosRv[position].duracion.toString() + " minutos"
            tv_precioServicio.text =  "Precio: $"+ serviciosRv[position].precio.toString()
            Glide.with(this).load(serviciosRv[position].imagen).into(this.iv_imagenServicio)
        }

        holder.itemView.setOnClickListener {
            if(ServiciosFragment.eliminarActivado) {
                var dialog: EliminarServicioDialog =
                    EliminarServicioDialog()
                dialog.idServicioEliminar = serviciosRv[position].idDocumento
                dialog.show(ServiciosFragment.adminFragmento!!, "EliminarServicioDialog")
            }

        }
    }


}