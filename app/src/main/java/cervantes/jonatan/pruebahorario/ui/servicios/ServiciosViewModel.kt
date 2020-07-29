package cervantes.jonatan.pruebahorario.ui.servicios

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import cervantes.jonatan.pruebahorario.entidades.Empleado
import cervantes.jonatan.pruebahorario.entidades.Servicio
import cervantes.jonatan.pruebahorario.utilidades.EmpleadoRV
import cervantes.jonatan.pruebahorario.utilidades.ServicioRV

class ServiciosViewModel : ViewModel() {

//    private val _text = MutableLiveData<String>().apply {
//        //value = "This is gallery Fragment"
//    }
//    val text: LiveData<String> = _text

    private var _listaServiciosRV = MutableLiveData<ArrayList<ServicioRV>>()
    var listaServiciosRV = _listaServiciosRV

    var eliminarActivado = MutableLiveData<Boolean>().apply {
        value = false
    }

    fun llenarListaRecyclerView(listaServicios: ArrayList<Servicio>) {

        var servicioRvList = ArrayList<ServicioRV>()

        for (i in listaServicios.indices) {
            var servicioRv = ServicioRV("", 0.0f, 0, listaServicios[i].idDocumento, "")
            servicioRv.nombre = listaServicios[i]!!.nombre
            servicioRv.duracion = listaServicios[i]!!.duracion
            servicioRv.precio = listaServicios[i]!!.precio
            servicioRv.imagen = listaServicios[i]!!.imagen

            servicioRvList.add(servicioRv)
        }

        _listaServiciosRV.postValue(servicioRvList)
    }

    /**
     * Cambiar estado de los floating action buttons, 0 para eliminar
     */
    fun cambiarEstado(tipo:Int) {
        when (tipo) {
            0 -> {
                eliminarActivado.postValue(!obtenerEliminarActivado())
            }
            else -> {
                eliminarActivado.postValue(false)
            }
        }
    }


    fun obtenerEliminarActivado():Boolean{
        return eliminarActivado.value!!
    }




}