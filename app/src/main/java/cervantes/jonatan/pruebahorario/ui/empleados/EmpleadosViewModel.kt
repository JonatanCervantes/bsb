package cervantes.jonatan.pruebahorario.ui.empleados


import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import cervantes.jonatan.pruebahorario.R
import cervantes.jonatan.pruebahorario.entidades.Empleado
import cervantes.jonatan.pruebahorario.utilidades.Disponibilidades
import cervantes.jonatan.pruebahorario.utilidades.EmpleadoRV
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EmpleadosViewModel : ViewModel() {

    private var _listaEmpleadosRV = MutableLiveData<ArrayList<EmpleadoRV>>()
    var listaEmpleadosRV = _listaEmpleadosRV

    var modificarActivado = MutableLiveData<Boolean>().apply {
        value = false
    }
    var eliminarActivado = MutableLiveData<Boolean>().apply {
        value = false
    }

    fun llenarListaRecyclerView(listaEmpleados: ArrayList<Empleado>) = CoroutineScope(Dispatchers.Default).launch {

        var empleadoRvList = ArrayList<EmpleadoRV>()

        for (i in listaEmpleados.indices) {
            var empleadoRv = EmpleadoRV("", "", "", listaEmpleados[i].idDocumento, "", R.raw.mreddot,"")
            empleadoRv.nombre = listaEmpleados[i]!!.nombre
            empleadoRv.email = listaEmpleados[i]!!.email
            empleadoRv.fotoPerfil = listaEmpleados[i]!!.fotoPerfil
            empleadoRv.horario = listaEmpleados[i]!!.horario
            empleadoRv.disponibilidad = listaEmpleados[i]!!.disponibilidad
            if(empleadoRv.disponibilidad == Disponibilidades.DISPONIBLE.name) {
                empleadoRv.animacionDisponibilidad = R.raw.mpulsing
            }
            empleadoRvList.add(empleadoRv)
        }
        _listaEmpleadosRV.postValue(empleadoRvList)
    }

    /**
     * Cambiar estado de los floating action buttons, 0 para eliminar, 1 para modificar
     */
    fun cambiarEstado(tipo:Int) {
        when (tipo) {
            0 -> {
                if(obtenerModificarActivado())
                    modificarActivado.postValue(false)
                eliminarActivado.postValue(!(eliminarActivado.value)!!)
            }
            1 -> {
                if(obtenerEliminarActivado())
                    eliminarActivado.postValue(false)
                modificarActivado.postValue(!(modificarActivado.value)!!)
            }
            else -> {
                eliminarActivado.postValue(false)
                modificarActivado.postValue(false)
            }
        }
    }

    fun obtenerModificarActivado():Boolean{
        return modificarActivado.value!!
    }

    fun obtenerEliminarActivado():Boolean{
        return eliminarActivado.value!!
    }


}