package cervantes.jonatan.pruebahorario.citas.aplicacion.ui

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import cervantes.jonatan.pruebahorario.citas.dominio.Cita
import cervantes.jonatan.pruebahorario.citas.dominio.CitaRV
import cervantes.jonatan.pruebahorario.empleados.infraestructura.EmpleadosRepository
import cervantes.jonatan.pruebahorario.utilidades.FechasHelper
import java.util.*
import kotlin.collections.ArrayList

class CitasViewModel : ViewModel() {

    var listaCitasRV = MutableLiveData<ArrayList<CitaRV>>()
    var empleadoSeleccionado = MutableLiveData<String>()

    fun llenarListaRecyclerView(listaCitas:ArrayList<Cita>, idEmpleado:String)  {
        var horarios = EmpleadosRepository.obtenerEmpleado(obtenerEmpleadoSeleccionado())!!.horariosMap[FechasHelper.obtenerDayOfWeekFechaParaCitas().toString()]!!
        val copiaListaCitas = listaCitas.clone() as ArrayList<Cita>
        var citaRvList = ArrayList<CitaRV>()
        var fechaCalendar = Calendar.getInstance(TimeZone.getTimeZone("GMT-7"))

        for(i in horarios.indices){
//            if(i != horarios.lastIndex) {
                var listaCitasEnviar: ArrayList<Cita> = ArrayList()

                val iterator = copiaListaCitas.iterator()
                iterator.forEach {
                    if(it.empleado.idDocumento == idEmpleado) {
                        fechaCalendar.time = it.fechaInicio.toDate()
                        var hora =(fechaCalendar.get(Calendar.HOUR_OF_DAY).toString())

                        Log.d("CitasFragment", "Hora1: "+horarios[i]  + "Hora2: "+ hora)
                        if(horarios[i].first == (hora)) {
                            listaCitasEnviar.add(it)
                            iterator.remove()
                        }
                    }
                }

                var citaRv = CitaRV(
                    hora = horarios[i].first,
                    listaCitas = listaCitasEnviar
                )
                citaRvList.add(citaRv)
//            }
        }

        listaCitasRV.postValue(citaRvList)
    }

    fun obtenerEmpleadoSeleccionado():String{
        return empleadoSeleccionado.value!!
    }


}