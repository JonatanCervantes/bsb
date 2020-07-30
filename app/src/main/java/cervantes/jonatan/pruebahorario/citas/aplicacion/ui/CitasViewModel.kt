package cervantes.jonatan.pruebahorario.citas.aplicacion.ui

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import cervantes.jonatan.pruebahorario.citas.dominio.Cita
import cervantes.jonatan.pruebahorario.citas.dominio.CitaRV
import java.util.*
import kotlin.collections.ArrayList

class CitasViewModel : ViewModel() {

    var listaCitasRV = MutableLiveData<ArrayList<CitaRV>>()

    fun llenarListaRecyclerView(listaCitas:ArrayList<Cita>, horarios:Array<String>)  {
        val copiaListaCitas = listaCitas.clone() as ArrayList<Cita>
        var citaRvList = ArrayList<CitaRV>()
        var fechaCalendar = Calendar.getInstance(TimeZone.getTimeZone("GMT-7"))

        for (i in horarios.indices) {
            var listaCitasEnviar: ArrayList<Cita> = ArrayList()

            val iterator = copiaListaCitas.iterator()
            iterator.forEach {
                fechaCalendar.time = it.fechaInicio.toDate()
                var hora =(fechaCalendar.get(Calendar.HOUR_OF_DAY).toString())

                Log.d("CitasFragment", "Hora1: "+horarios[i]  + "Hora2: "+ hora)
                if(horarios[i] == (hora)) {
                    listaCitasEnviar.add(it)
                    iterator.remove()
                }
            }

            var citaRv = CitaRV(
                hora = horarios[i],
                listaCitas = listaCitasEnviar
            )
            citaRvList.add(citaRv)
        }

        listaCitasRV.postValue(citaRvList)
    }


}