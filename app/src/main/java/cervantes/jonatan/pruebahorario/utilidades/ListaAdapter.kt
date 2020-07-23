package cervantes.jonatan.pruebahorario.utilidades

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.GridLayout
import android.widget.TextView
import android.widget.ToggleButton
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import cervantes.jonatan.pruebahorario.R
import java.util.*
import kotlin.math.roundToInt


class ListaAdapter (var citasRv: List<CitaRV>, var context: Context) : BaseAdapter()  {

    companion object {
        private var contadorPeriodosPendientes = 0
    }

    override fun getView(position: Int, itemView: View?, p2: ViewGroup?): View {
        Log.d("ListaAdapter", "contador periodos" + contadorPeriodosPendientes.toString())

        var cita = citasRv[position]
        var inflador = LayoutInflater.from(context)
        var vista = inflador.inflate(R.layout.hora_view, null)

        val separador = ":"
        val textViewsHoras = obtenerTextViews(vista, 0)
        val textViewsCitas = obtenerTextViews(vista, 1)

        vista.apply {
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

                    for (j in 0..terminacionCita) {
                        if(j < inicioCita && i == 0) {
                            textViewsCitas[j].setBackgroundResource(R.drawable.background_citas_disponible)
                        }
                        if(j in inicioCita..terminacionCita) {
                            if(j >= textViewsCitas.size) {
                                contadorPeriodosPendientes++
                                Log.d("ListaAdapter", "contador periodos" + contadorPeriodosPendientes.toString())
                            } else {
                                textViewsCitas[j].setBackgroundResource(R.drawable.background_citas_ocupado)
                            }
                        }
                        if(j == inicioCita) {
                            textViewsCitas[j].text = listaCitasLocal[i].cliente.nombre
                        }
                    }

//                    for (j in textViewsCitas.indices) {
//                        if(j < inicioCita && i == 0) {
//                            textViewsCitas[j].setBackgroundResource(R.drawable.background_citas_disponible)
//                        }
//                        if(j in inicioCita..terminacionCita) {
//                            if(j >= textViewsCitas.size) {
//                                contadorPeriodosPendientes++
//                                Log.d("ListaAdapter", "contador periodos" + contadorPeriodosPendientes.toString())
//                            } else {
//                                textViewsCitas[j].setBackgroundResource(R.drawable.background_citas_ocupado)
//                            }
//                        }
//                        if(j == inicioCita) {
//                            textViewsCitas[j].text = listaCitasLocal[i].cliente.nombre
//                        }
//                    }
                }
            } else {
                for (i in textViewsCitas.indices) {
                    if(contadorPeriodosPendientes > 0 ) {
                        textViewsCitas[i].setBackgroundResource(R.drawable.background_citas_ocupado)
                        contadorPeriodosPendientes--
                    } else {
                        textViewsCitas[i].setBackgroundResource(R.drawable.background_citas_disponible)
                    }
                    textViewsCitas[i].text = ""
                }
            }

        } // Termina el itemView.apply


        return vista
    }

    override fun getItem(p0: Int): Any {
        return citasRv[p0]
    }

    override fun getItemId(p0: Int): Long {
        return p0.toLong()
    }

    override fun getCount(): Int {
        return citasRv.size
    }

    override fun getViewTypeCount(): Int {
        return count
    }

    override fun getItemViewType(position: Int): Int {
        return position
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