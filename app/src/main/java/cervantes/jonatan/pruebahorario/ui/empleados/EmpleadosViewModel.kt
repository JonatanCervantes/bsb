package cervantes.jonatan.pruebahorario.ui.empleados

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class EmpleadosViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        //value = "Pantalla de empleados"
    }
    val text: LiveData<String> = _text
}