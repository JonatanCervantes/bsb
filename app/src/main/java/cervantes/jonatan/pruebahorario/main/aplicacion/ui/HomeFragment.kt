package cervantes.jonatan.pruebahorario.main.aplicacion.ui


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import cervantes.jonatan.pruebahorario.R
import kotlinx.android.synthetic.main.fragment_home.*

class HomeFragment : Fragment() {

    private lateinit var viewModel: HomeViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewModel = ViewModelProviders.of(this).get(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)

        viewModel.textCitas.observe(viewLifecycleOwner, Observer {
            tv_ayudaCitas.text = it
        })

        viewModel.textEmpleados.observe(viewLifecycleOwner, Observer {
            tv_ayudaEmpleados.text = it
        })

        viewModel.textServicios.observe(viewLifecycleOwner, Observer {
            tv_ayudaServicios.text = it
        })

        viewModel.textExtra.observe(viewLifecycleOwner, Observer {
            tv_ayudaExtra.text = it
        })

        return root
    }

}
