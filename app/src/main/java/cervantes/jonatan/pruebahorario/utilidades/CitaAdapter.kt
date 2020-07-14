package cervantes.jonatan.pruebahorario.utilidades

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cervantes.jonatan.pruebahorario.dialogs.EliminarCitaDialog
import cervantes.jonatan.pruebahorario.dialogs.AgregarCitaDialog
import cervantes.jonatan.pruebahorario.R
import cervantes.jonatan.pruebahorario.ui.citas.CitasFragment
import kotlinx.android.synthetic.main.cita_view.view.*
import java.util.*

class CitaAdapter(var citasRv: List<CitaRV>) : RecyclerView.Adapter<CitaAdapter.CitaViewHolder>() {

    inner class CitaViewHolder(itemView:View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CitaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.cita_view, parent, false)
        return CitaViewHolder(view)
    }

    override fun getItemCount(): Int {
        return citasRv.size
    }

    override fun onBindViewHolder(holder: CitaViewHolder, position: Int) {
        holder.itemView.apply {
            if(citasRv[position].cita.equals(resources.getString(R.string.disponible))) {
                tv_cita.setBackgroundResource(R.drawable.background_citas_disponible)
                tv_cita.setOnClickListener {
                    var  horaYMinutos = citasRv[position].hora.split(":")
                    CitasFragment.fechaSeleccionada.set(Calendar.HOUR_OF_DAY, Integer.valueOf(horaYMinutos.get(0)))
                    CitasFragment.fechaSeleccionada.set(Calendar.MINUTE, Integer.valueOf(horaYMinutos.get(1)))
                    CitasFragment.fechaSeleccionada.set(Calendar.SECOND, 0)

                    var dialog: AgregarCitaDialog =
                        AgregarCitaDialog()
                    dialog.show(CitasFragment.adminFragmento!!, "CustomDialog")
                }
            } else {
                tv_cita.setBackgroundResource(R.drawable.background_citas_ocupado)
                tv_cita.setOnClickListener {

                    var dialog: EliminarCitaDialog =
                        EliminarCitaDialog()
                    dialog.idDocumentoEliminar = citasRv[position].idDocumento
                    dialog.show(CitasFragment.adminFragmento!!, "CitaCancelacionDialog")
                }
            }

            //tv_cita.setTextColor(Color.WHITE)
            tv_horaCita.text = citasRv[position].hora
            tv_cita.text = citasRv[position].cita

        }

    }




}