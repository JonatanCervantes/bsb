package cervantes.jonatan.pruebahorario.main.aplicacion.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HomeViewModel : ViewModel() {

    private val _textCitas = MutableLiveData<String>().apply {
        value = "¡Puedes registrar una cita seleccionando este icono en el menú lateral!"
    }
    val textCitas: LiveData<String> = _textCitas

    private val _textEmpleados = MutableLiveData<String>().apply {
        value = "¡Si quieres saber si alguno de nuestros empleados se encuentra disponible, hazlo seleccionando este icono en el menú lateral!"
    }
    val textEmpleados: LiveData<String> = _textEmpleados

    private val _textServicios = MutableLiveData<String>().apply {
        value = "¡Si quieres saber más sobre los servicios que ofrecemos, hazlo seleccionando este icono en el menú lateral!"
    }
    val textServicios: LiveData<String> = _textServicios

    private val _textExtra = MutableLiveData<String>().apply {
        value = "¡No olvides que seguimos las mejores normas de higiene pa que no se enferme el nino!!"
    }
    val textExtra: LiveData<String> = _textExtra


}