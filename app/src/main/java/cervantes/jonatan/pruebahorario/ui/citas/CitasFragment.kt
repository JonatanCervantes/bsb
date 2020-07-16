package cervantes.jonatan.pruebahorario.ui.citas

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import cervantes.jonatan.pruebahorario.R
import cervantes.jonatan.pruebahorario.entidades.Cita
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_citas.*
import cervantes.jonatan.pruebahorario.utilidades.CitaAdapter
import cervantes.jonatan.pruebahorario.utilidades.CitaRV
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class CitasFragment : Fragment() {

    private lateinit var citasViewModel: CitasViewModel


    private val citaCollectionRef= Firebase.firestore.collection("citas")

    private var listaCitas:TreeMap<Int, Cita> = TreeMap<Int, Cita>()
    private var listaIdDocumentos:TreeMap<Int, String> = TreeMap<Int, String>()
    private var adapter: CitaAdapter?= null

    private var fechaQueryActual: Calendar?= null
    private var fechaQueryFutura: Calendar?= null
    companion object {
        var fechaSeleccionada: Calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT-7"))
        var adminFragmento: FragmentManager?= null

        public fun inicializarFechas() {
            fechaSeleccionada.set(Calendar.HOUR_OF_DAY, 0)
            fechaSeleccionada.set(Calendar.MINUTE, 0)
            fechaSeleccionada.set(Calendar.SECOND, 1)
            fechaSeleccionada.set(Calendar.MILLISECOND, 0)
        }
    }



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        citasViewModel =
                ViewModelProviders.of(this).get(CitasViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_citas, container, false)
        //val textView: TextView = root.findViewById(R.id.text_home)
        //homeViewModel.text.observe(viewLifecycleOwner, Observer {
        //    textView.text = it
        //})
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adminFragmento = fragmentManager
        inicializarFechas()
        ajustarFechasQuerys()

        var job2 = CoroutineScope(Dispatchers.IO).launch {
            Log.d("CitasFragment", "corutina2 - THREAD: ${Thread.currentThread().name}")
            adapter = CitaAdapter(llenarListaRecyclerView())
            subscribeToRealtimeUpdates()
            Log.d("CitasFragment", "Fin de la corutina 2")
        }

        runBlocking {
            job2.join()
            Log.d("CitasFragment", "Continua el main thread")
            Log.d("CitasFragment", "runBlocking1 - THREAD: ${Thread.currentThread().name}")
        }

        rv_citas.adapter = adapter
        rv_citas.layoutManager = LinearLayoutManager(view.context)

        configurarTvFechaActual(view)
        configurarBotones(view)
    }

    private fun ajustarFechasQuerys() {
        fechaQueryActual = fechaSeleccionada.clone() as Calendar
        fechaQueryFutura = fechaQueryActual!!.clone() as Calendar
        fechaQueryFutura!!.add(Calendar.DAY_OF_YEAR, 1)
    }

    private fun subscribeToRealtimeUpdates(){
        citaCollectionRef.whereGreaterThanOrEqualTo("fecha", Timestamp(fechaQueryActual!!.time))
            .whereLessThan("fecha", Timestamp(fechaQueryFutura!!.time))
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->

                firebaseFirestoreException?.let {
                    Toast.makeText(view!!.context, it.message, Toast.LENGTH_LONG).show()
                    return@addSnapshotListener
                }

                var job = CoroutineScope(Dispatchers.IO).launch {
                    listaCitas.clear()
                    listaIdDocumentos.clear()
                    var index = 0
                    Log.d("CitasFragment", "RTU - THREAD: ${Thread.currentThread().name}")
                    querySnapshot?.let {
                        for (document in it) {
                            Log.d("CitasFragment", document.toString())
                            var cita = document.toObject<Cita>()

                            listaCitas.put(index, cita)
                            listaIdDocumentos.put(index, document.id)
                            index++
                            //listaCitas.add(cita)
                        }
                        withContext(Dispatchers.Main) {
                            adapter!!.citasRv = llenarListaRecyclerView()
                            adapter!!.notifyDataSetChanged()
                        }
                    }
                }
        }
    }

    //EMPIEZA EL CAMBIADERO
    private fun llenarListaRecyclerViewAsync() = CoroutineScope(Dispatchers.Default).async {

    }

    private suspend fun llenarListaRecyclerView(): ArrayList<CitaRV> {

        var horarios: Array<String> = view!!.resources.getStringArray(R.array.horarios)
        var citaRvList = ArrayList<CitaRV>()

        var fechaCalendar = Calendar.getInstance(TimeZone.getTimeZone("GMT-7"))

        Log.d("CitasFragment", "tamanio de las citas"+listaCitas.size.toString())

        for (i in horarios.indices) {
            var citaRv = CitaRV(horarios[i], resources.getString(R.string.disponible), "")
            for (j in listaCitas.keys) {
                var horaYMinuto:String = ""
                fechaCalendar.time = (listaCitas[j]!!.fecha.toDate())
                horaYMinuto+=(fechaCalendar.get(Calendar.HOUR_OF_DAY).toString())
                horaYMinuto+=(":")
                if(fechaCalendar.get(Calendar.MINUTE).toString().length == 1) {
                    horaYMinuto+="0"+(fechaCalendar.get(Calendar.MINUTE).toString())
                } else {
                    horaYMinuto+=(fechaCalendar.get(Calendar.MINUTE).toString())
                }

                Log.d("CitasFragment", "Hora1: "+horarios[i]  + "Hora2: "+ horaYMinuto)

                if(horarios[i].equals(horaYMinuto)) {
                    citaRv.cita = "Apartado: " + listaCitas[j]!!.cliente.nombre
                    citaRv.idDocumento = listaIdDocumentos[j]!!
                    break
                }
            }
            citaRvList.add(citaRv)
        }
        return citaRvList
    }

    private fun configurarTvFechaActual(view:View) {
        val tvFechaActual = view.findViewById(R.id.tv_diaActual) as TextView
        tvFechaActual.text = Editable.Factory.getInstance().newEditable(SimpleDateFormat("EEE dd/MM/yyyy").format(fechaSeleccionada.time))

        tvFechaActual.setOnClickListener {
            val dp = DatePickerDialog.OnDateSetListener { datePicker, year, month, day ->
                fechaSeleccionada.set(Calendar.YEAR, year)
                fechaSeleccionada.set(Calendar.MONTH, month)
                fechaSeleccionada.set(Calendar.DAY_OF_MONTH, day)
                tvFechaActual.text = Editable.Factory.getInstance().newEditable(SimpleDateFormat("EEE dd/MM/yyyy").format(fechaSeleccionada.time))
            }
            DatePickerDialog(view.context, R.style.TimePickerTheme, dp, fechaSeleccionada.get(Calendar.YEAR),
                fechaSeleccionada.get(Calendar.MONTH), fechaSeleccionada.get(Calendar.DAY_OF_MONTH)).show()
        }
    }

    private fun configurarBotones(view:View) {
        val btnAyer = view.findViewById(R.id.btn_ayer) as ImageButton
        val btnManana = view.findViewById(R.id.btn_manana) as ImageButton
        val tvFechaActual = view.findViewById(R.id.tv_diaActual) as TextView

        btnAyer.setOnClickListener {
            fechaSeleccionada.add(Calendar.DAY_OF_YEAR, -1)
            tvFechaActual.text = Editable.Factory.getInstance().newEditable(SimpleDateFormat("EEE dd/MM/yyyy").format(fechaSeleccionada.time))
            ajustarFechasQuerys()
            solicitarCitasManualmente()
        }

        btnManana.setOnClickListener {
            fechaSeleccionada.add(Calendar.DAY_OF_YEAR, 1)
            tvFechaActual.text = Editable.Factory.getInstance().newEditable(SimpleDateFormat("EEE dd/MM/yyyy").format(fechaSeleccionada.time))
            ajustarFechasQuerys()
            solicitarCitasManualmente()
        }
    }

    private fun solicitarCitasManualmente() = CoroutineScope(Dispatchers.IO).launch {
        try {
            listaCitas.clear()
            listaIdDocumentos.clear()
            val querySnapshot = citaCollectionRef.whereGreaterThanOrEqualTo("fecha", Timestamp(fechaQueryActual!!.time))
                .whereLessThan("fecha", Timestamp(fechaQueryFutura!!.time)).get().await()

            Log.d("CitasFragment", "sol citas manual - THREAD: ${Thread.currentThread().name}")
            var index = 0
            for (document in querySnapshot.documents) {
                Log.d("CitasFragment", document.toString())
                var cita = document.toObject<Cita>()
                if (cita != null) {
                    listaCitas.put(index, cita)
                    listaIdDocumentos.put(index, document.id)
                    index++
                }
            }
            withContext(Dispatchers.Main) {
                adapter!!.citasRv = llenarListaRecyclerView()
                adapter!!.notifyDataSetChanged()
            }
        } catch(e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@CitasFragment.context, e.message, Toast.LENGTH_LONG).show()
            }
        }
    }


}
