package cervantes.jonatan.pruebahorario.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HomeViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Fragmento de bienvenida!"
    }
    val text: LiveData<String> = _text
}