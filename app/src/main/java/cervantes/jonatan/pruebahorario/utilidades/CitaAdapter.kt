package cervantes.jonatan.pruebahorario.utilidades

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import cervantes.jonatan.pruebahorario.R
import java.util.*
import kotlin.math.roundToInt



class CitaAdapter(var citasRv: List<CitaRV>) : RecyclerView.Adapter<CitaAdapter.CitaViewHolder>() {

    private var contadorPeriodosPendientes = 0

    inner class CitaViewHolder(holder: View): RecyclerView.ViewHolder(holder) {
        private var view = holder
        private lateinit var cita:CitaRV

        fun bindDataWithViewHolder(dataModel:CitaRV, position: Int) {
            this.cita = dataModel

            val separador = ":"
            val textViewsHoras = obtenerTextViews(itemView, 0)
            val textViewsCitas = obtenerTextViews(itemView, 1)

            itemView.apply {
                //Se llenan los textviews de la vista que contienen las horas
                val horariosMinutos:Array<String> = resources.getStringArray(R.array.horariosMinutos)
                for (i in textViewsHoras.indices) {
                    textViewsHoras[i].text = cita.hora.plus(separador.plus(horariosMinutos[i]))
                }

                //Se llenan los textviws de la vista que contienen las citas
                var listaCitasLocal = cita.listaCitas
                if(listaCitasLocal.isNotEmpty()) {
                    for (i in listaCitasLocal.indices) {
                        var date = listaCitasLocal[i].fecha.toDate()
                        var calendar = Calendar.getInstance()
                        calendar.time = date
                        var minutoInicio = calendar.get(Calendar.MINUTE)
                        var duracion = listaCitasLocal[i].servicio.duracion

                        var inicioCitaFloat = (minutoInicio.toFloat() / PERIODO_SEPARACION_MINUTOS)
                        var inicioCita = inicioCitaFloat.roundToInt()

                        var duracionCitaFloat = (duracion.toFloat() / PERIODO_SEPARACION_MINUTOS)
                        var duracionCita = duracionCitaFloat.roundToInt()

                        var terminacionCita = inicioCita + duracionCita

                        for (j in textViewsCitas.indices) {
                            if(j < inicioCita && i == 0) {
                                textViewsCitas[j].setBackgroundResource(R.drawable.background_citas_disponible)
                            }
                            if(j in inicioCita..terminacionCita) {
                                if(j >= textViewsCitas.size) {
                                    contadorPeriodosPendientes++
                                } else {
                                    textViewsCitas[j].setBackgroundResource(R.drawable.background_citas_ocupado)
                                }
                            }
                            if(j == inicioCita) {
                                textViewsCitas[j].text = listaCitasLocal[i].cliente.nombre
                            }
                        }
                    }
                } else {
                    for (i in textViewsCitas.indices) {
                        textViewsCitas[i].setBackgroundResource(R.drawable.background_citas_disponible)
                        textViewsCitas[i].text = ""
                    }
                }



            } // Termina el itemView.apply

        }//Termina el metodo bindDataWithViewHolder()

    }//Termina la clase

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CitaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.hora_view, parent, false)

        return CitaViewHolder(view)
    }

    override fun getItemCount(): Int {
        return citasRv.size
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onBindViewHolder(holder: CitaViewHolder, position: Int) {
//        holder.setIsRecyclable(false)
        holder.bindDataWithViewHolder(citasRv[position], position)




//            val textViews = obtenerTextViews(holder.itemView)
//            val separador = ":"
//
//        for (i in textViews.indices) {
//            textViews[i].text = ""
//        }
//
//            holder.itemView.apply {
//                val horariosMinutos:Array<String> = resources.getStringArray(R.array.horariosMinutos)
//
//                if(citasRv[position].listaCitas.isEmpty()) {
//                    for (i in textViews.indices) {
//                        if(contadorPeriodosPendientes != 0) {
//                            textViews[i].setBackgroundResource(R.drawable.background_citas_ocupado)
//                            contadorPeriodosPendientes--
//                        } else {
//                            textViews[i].setBackgroundResource(R.drawable.background_citas_disponible)
//                            textViews[i].setOnClickListener {
//                                CitasFragment.fechaSeleccionada.set(Calendar.HOUR_OF_DAY, Integer.valueOf(citasRv[position].hora))
//                                CitasFragment.fechaSeleccionada.set(Calendar.MINUTE, Integer.valueOf(horariosMinutos[i]))
//                                CitasFragment.fechaSeleccionada.set(Calendar.SECOND, 0)
//
//                                var dialog: AgregarCitaDialog =
//                                    AgregarCitaDialog()
//                                dialog.show(CitasFragment.adminFragmento!!, "CustomDialog")
//                            }
//                        }
//                    }
//                } else {
//                    val listaCitas = citasRv[position].listaCitas
//                    for (i in listaCitas.indices) {
//                        val date = listaCitas[i].fecha.toDate()
//                        var calendar = Calendar.getInstance()
//                        calendar.time = date
//
//                        val minuto = calendar.get(Calendar.MINUTE)
//                        val inicioCitaFloat = (minuto.toFloat() / PERIODO_SEPARACION_MINUTOS)
//                        val inicioCita = inicioCitaFloat.roundToInt()
////                        Log.d("CitaAdapter", "inicioCita" +  inicioCita.toString())
//
//                        val tiempoServicio = listaCitas[i].servicio.duracion
//                        val duracionCitaFloat = (tiempoServicio.toFloat() / PERIODO_SEPARACION_MINUTOS)
//                        val duracionCita = duracionCitaFloat.roundToInt()
//
//                        val terminacionCita = inicioCita + duracionCita
////                        Log.d("CitaAdapter", "DuracionCita" +  duracionCita.toString())
////                        Log.d("CitaAdapter", "FinCita" +  terminacionCita.toString())
//
//                       for (j in inicioCita until terminacionCita) {
//                           if(j == inicioCita) {
//                               textViews[j].text = listaCitas[i].cliente.nombre
//                               textViews[j].setOnClickListener {
//                                   var dialog: EliminarCitaDialog = EliminarCitaDialog()
//                                   dialog.idDocumentoEliminar = citasRv[position].listaIdDocumento[i]
//                                   dialog.show(CitasFragment.adminFragmento!!, "CitaCancelacionDialog")
//                               }
//                           }
//
//                           if(j < textViews.size) {
//                               textViews[j].setBackgroundResource(R.drawable.background_citas_ocupado)
//                           } else {
//                               contadorPeriodosPendientes++
//                           }
//                       }
//                    }
//                }
//
//                tv_horaCita00.text = citasRv[position].hora.plus(separador.plus(horariosMinutos[0]))
//                tv_horaCita10.text = citasRv[position].hora.plus(separador.plus(horariosMinutos[1]))
//                tv_horaCita20.text = citasRv[position].hora.plus(separador.plus(horariosMinutos[2]))
//                tv_horaCita30.text = citasRv[position].hora.plus(separador.plus(horariosMinutos[3]))
//                tv_horaCita40.text = citasRv[position].hora.plus(separador.plus(horariosMinutos[4]))
//                tv_horaCita50.text = citasRv[position].hora.plus(separador.plus(horariosMinutos[5]))
//
//            }
//            Log.d("CitaAdapter", "Periodos pendientes=  " +contadorPeriodosPendientes.toString())

    }

    private fun obtenerTextViews(view: View, tipoTextViews:Int) : ArrayList<TextView> {
        var listaChilds = ArrayList<TextView>()
        var mlayout: GridLayout = view.findViewById(R.id.gl_horaView)

        //Se divide entre 2 porque siempre se querran obtener la mitad de los textviews, basados en el tipo requerido
        var count: Int = mlayout.childCount / 2
        var contadorInicial = 0

        if(tipoTextViews == 1) {
            //1 cuando se quiere obtener los textviews que contienen las citas
            contadorInicial = count
            count = count.plus(count)
        }

        for (i in contadorInicial until count) {
            val child: View = mlayout.getChildAt(i)
            if(child is TextView) {
                listaChilds.add(child)
            }
        }
        return listaChilds
    }





}