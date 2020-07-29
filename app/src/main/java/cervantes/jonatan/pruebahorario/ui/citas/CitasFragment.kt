package cervantes.jonatan.pruebahorario.ui.citas

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import cervantes.jonatan.pruebahorario.R
import cervantes.jonatan.pruebahorario.firebase.CitasRepository
import cervantes.jonatan.pruebahorario.firebase.EmpleadosRepository
import cervantes.jonatan.pruebahorario.firebase.ServiciosRepository
import cervantes.jonatan.pruebahorario.utilidades.FechasHelper
import cervantes.jonatan.pruebahorario.utilidades.TablaAdapter
import kotlinx.android.synthetic.main.fragment_citas.*
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*


class CitasFragment : Fragment() {

    private lateinit var tablaAdapter: TablaAdapter
    private lateinit var viewModel: CitasViewModel

    companion object {
        var adminFragmento: FragmentManager?= null
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewModel = ViewModelProviders.of(this).get(CitasViewModel::class.java)
        return inflater.inflate(R.layout.fragment_citas, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adminFragmento = fragmentManager

        var job2 = CoroutineScope(Dispatchers.Default).launch {
            val salvador = FechasHelper
        }

        CoroutineScope(Dispatchers.IO).launch {
            //Inicializacion de los repositorios para dar tiempo a obtener los datos
            job2.join()
            ServiciosRepository
            EmpleadosRepository
            CitasRepository
        }

        CoroutineScope(Dispatchers.Main).launch {
            inicializarBarraSuperiorLaunch(view)
        }

        viewModel?.listaCitasRV!!.observe(this, Observer {
            CoroutineScope(Dispatchers.Main).launch {
                tablaAdapter = TablaAdapter(it, view!!)
                tablaAdapter.inflarVista()
            }
        })

        CitasRepository.listaCitas.observe(this, Observer {
            CoroutineScope(Dispatchers.Default).launch {
                viewModel?.llenarListaRecyclerView(it, resources.getStringArray(R.array.horarios))
            }
        })
    }


    private fun inicializarBarraSuperiorLaunch(view: View) = CoroutineScope(Dispatchers.Main).launch {
        configurarTvFechaActual(view)
        configurarBotones(view)
    }

    private fun configurarTvFechaActual(view:View) {
        tv_diaActual.text = Editable.Factory.getInstance().newEditable(SimpleDateFormat("EEE dd/MM/yyyy").format(
            FechasHelper.obtenerFechaParaCitas().time))

        tv_diaActual.setOnClickListener {
            val copiaFecha = FechasHelper.obtenerCopiaFechaParaCitas()
            val dp = DatePickerDialog.OnDateSetListener { datePicker, year, month, day ->
                copiaFecha.set(Calendar.YEAR, year)
                copiaFecha.set(Calendar.MONTH, month)
                copiaFecha.set(Calendar.DAY_OF_MONTH, day)
                tv_diaActual.text = Editable.Factory.getInstance().newEditable(SimpleDateFormat("EEE dd/MM/yyyy").format(
                    copiaFecha.time))
            }
            DatePickerDialog(view.context, R.style.TimePickerTheme, dp,
                copiaFecha.get(Calendar.YEAR),
                copiaFecha.get(Calendar.MONTH),
                copiaFecha.get(Calendar.DAY_OF_MONTH)).show()
        }
    }

    private fun configurarBotones(view:View) {
        btn_ayer.setOnClickListener {
            FechasHelper.modificarFechaParaCitas(-1)
            tv_diaActual.text = Editable.Factory.getInstance().newEditable(SimpleDateFormat("EEE dd/MM/yyyy").format(FechasHelper.obtenerFechaParaCitas().time))
            CitasRepository.solicitarCitasManualmente()
        }

        btn_manana.setOnClickListener {
            FechasHelper.modificarFechaParaCitas(1)
            tv_diaActual.text = Editable.Factory.getInstance().newEditable(SimpleDateFormat("EEE dd/MM/yyyy").format(FechasHelper.obtenerFechaParaCitas().time))
            CitasRepository.solicitarCitasManualmente()
        }
    }




}
