package cervantes.jonatan.pruebahorario.main.aplicacion.ui


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import cervantes.jonatan.pruebahorario.R
import kotlinx.android.synthetic.main.fragment_about.*
import kotlinx.android.synthetic.main.fragment_home.*

class AboutFragment : Fragment() {

    private lateinit var viewModel: AboutViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewModel = ViewModelProviders.of(this).get(AboutViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_about, container, false)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tv_tituloCreditos.text = getString(R.string.tituloCreditos)
        tv_creditos1.text = getString(R.string.creditos1)
        tv_creditos2.text = getString(R.string.creditos2)
        tv_creditos3.text = getString(R.string.creditos3)
        tv_creditos4.text = getString(R.string.creditos4)
        tv_creditos5.text = getString(R.string.creditos5)
        tv_creditos6.text = getString(R.string.creditos6)
    }

}
