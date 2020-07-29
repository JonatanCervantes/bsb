package cervantes.jonatan.pruebahorario.utilidades

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.GridLayout
import android.widget.TextView
import cervantes.jonatan.pruebahorario.R
import cervantes.jonatan.pruebahorario.dialogs.AgregarCitaDialog
import cervantes.jonatan.pruebahorario.dialogs.EliminarCitaDialog
import cervantes.jonatan.pruebahorario.entidades.Cita
import cervantes.jonatan.pruebahorario.ui.citas.CitasFragment
import kotlinx.android.synthetic.main.fragment_citas.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.roundToInt
import kotlin.properties.Delegates

const val PERIODO_SEPARACION_MINUTOS = 10

class TablaAdapter  {

    private var citasRv: List<CitaRV>
    private var view: View

    companion object {
        private var contadorPeriodosPendientes = 0
    }

    constructor(citasRv: List<CitaRV>, view: View) {
        this.citasRv = citasRv
        this.view = view
        contadorPeriodosPendientes = 0
        view.rv_citas.removeAllViews()
    }

    fun inflarVista(){
        for (position in view.resources.getStringArray(R.array.horarios).indices) {

            var cita = citasRv[position]

            var inflador = LayoutInflater.from(view.context)
            var vista = inflador.inflate(R.layout.hora_view, null)


            val separador = ":"
            val textViewsHoras = obtenerTextViews(vista, 0)
            val textViewsCitas = obtenerTextViews(vista, 1)

            vista.apply {
                //Se llenan los textviews de la vista que contienen las horas
                val horariosMinutos:Array<String> = resources.getStringArray(R.array.horariosMinutos)
                for (i in textViewsHoras.indices) {
                    textViewsHoras[i].text = cita.hora.plus(separador.plus(horariosMinutos[i]))
                }

                //Se llenan los textviews de la vista que contienen las citas
                var listaCitasLocal = cita.listaCitas
                if(listaCitasLocal.isNotEmpty()) {
                    lateinit var date: Date
                    lateinit var calendar: Calendar
                    var minutoInicio by Delegates.notNull<Int>()
                    var duracion by Delegates.notNull<Int>()
                    var inicioCitaFloat by Delegates.notNull<Float>()
                    var inicioCita by Delegates.notNull<Int>()
                    var duracionCitaFloat by Delegates.notNull<Float>()
                    var duracionCita by Delegates.notNull<Int>()
                    var terminacionCita by Delegates.notNull<Int>()

                    for (contadorCitas in listaCitasLocal.indices) {
                        date = listaCitasLocal[contadorCitas].fecha.toDate()
                        calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT-7"))
                        calendar.time = date
                        minutoInicio = calendar.get(Calendar.MINUTE)
                        duracion = listaCitasLocal[contadorCitas].servicio.duracion

                        inicioCitaFloat = (minutoInicio.toFloat() / PERIODO_SEPARACION_MINUTOS)
                        inicioCita = inicioCitaFloat.roundToInt()

                        duracionCitaFloat = (duracion.toFloat() / PERIODO_SEPARACION_MINUTOS)
                        duracionCita = duracionCitaFloat.roundToInt()

                        terminacionCita = inicioCita + duracionCita

                        var compensacionParaNoDesborde = 0
                        if(terminacionCita < textViewsCitas.size) {
                            compensacionParaNoDesborde = (textViewsCitas.size - terminacionCita)
                        }

                        for (contadorBloques in 0 until (terminacionCita+compensacionParaNoDesborde)) {
                            if(contadorBloques < inicioCita && contadorCitas == 0 && contadorPeriodosPendientes == 0) {
                                establecerHorarioDisponible(textViewsCitas, contadorBloques,
                                    Integer.valueOf(citasRv[position].hora), Integer.valueOf(horariosMinutos[contadorBloques]))
                            }
                            if(contadorPeriodosPendientes > 0 && contadorBloques<textViewsCitas.size) {
                                establecerHorarioOcupado(textViewsCitas, contadorBloques)
                                contadorPeriodosPendientes--
                            }
                            if(contadorBloques in inicioCita..(terminacionCita+compensacionParaNoDesborde)) {
                                //ESTO SIRVE PARA CUANDO LA CITA SE DESBORDA DE LA HORA
                                if(terminacionCita >= textViewsCitas.size) {
                                    if (contadorBloques >= textViewsCitas.size) {
                                        contadorPeriodosPendientes++
                                    } else {
                                        establecerHorarioOcupado(textViewsCitas, contadorBloques)
                                    }
                                }

                                //ESTO SIRVE PARA CUANDO LA CITA NO SE DESBORDA DE LA HORA
                                if(terminacionCita < textViewsCitas.size) {
                                    if(contadorBloques >= terminacionCita) {
                                        establecerHorarioDisponible(textViewsCitas, contadorBloques,
                                            Integer.valueOf(citasRv[position].hora), Integer.valueOf(horariosMinutos[contadorBloques]))
                                    } else {
                                        establecerHorarioOcupado(textViewsCitas, contadorBloques)
                                    }
                                }
                            }

                            if(contadorBloques == inicioCita) {
                                establecerDatosCita(textViewsCitas, contadorBloques, listaCitasLocal, contadorCitas)
                            }
                        }
                    }
                } else {
                    for (i in textViewsCitas.indices) {
                        if(contadorPeriodosPendientes > 0 ) {
                            establecerHorarioOcupado(textViewsCitas, i)
                            contadorPeriodosPendientes--
                        } else {
                            establecerHorarioDisponible(textViewsCitas, i,
                                Integer.valueOf(citasRv[position].hora), Integer.valueOf(horariosMinutos[i]))
                        }
                    }
                }
            } // Termina el itemView.apply

            view.rv_citas.addView(vista)
        }
    }

    private fun obtenerTextViews(view: View, tipoTextViews:Int) : ArrayList<TextView> {
        var listaChilds = ArrayList<TextView>()
        var mlayout: GridLayout = view.findViewById(R.id.gl_horaView)

        //Se divide entre 2 porque siempre se querran obtener la mitad de los textviews, basados en el tipo requerido
        var count: Int = mlayout.childCount / 2
        var contadorInicial = 0

        if(tipoTextViews == 1) {
            //1 cuando se quiere obtener los textviews que contienen las citas
            contadorInicial = count
            count = count.plus(count)
        }

        for (i in contadorInicial until count) {
            val child: View = mlayout.getChildAt(i)
            if(child is TextView) {
                listaChilds.add(child)
            }
        }
        return listaChilds
    }

//    private fun establecerHorarioDisponibleLaunch(textViewsCitas: ArrayList<TextView>, contador: Int, hora:Int, minutos: Int ) = CoroutineScope(Dispatchers.Main).launch{
//        establecerHorarioDisponible(textViewsCitas, contador, hora, minutos)
//    }

    private fun establecerHorarioDisponible(textViewsCitas: ArrayList<TextView>, contador: Int, hora:Int, minutos: Int ) {
        if(FechasHelper.obtenerFechaParaCitas().after(FechasHelper.obtenerFechaActualReal())) {
            textViewsCitas[contador].setBackgroundResource(R.drawable.background_citas_disponible)
            textViewsCitas[contador].setOnClickListener {
                val copiaFechaCita = FechasHelper.obtenerCopiaFechaParaCitas()
                copiaFechaCita.set(Calendar.HOUR_OF_DAY, hora)
                copiaFechaCita.set(Calendar.MINUTE, minutos)
                copiaFechaCita.set(Calendar.SECOND, 0)
                var dialog: AgregarCitaDialog = AgregarCitaDialog(copiaFechaCita)
                dialog.show(CitasFragment.adminFragmento!!, "CustomDialog")
            }
        } else {
            establecerHorarioNoDisponible(textViewsCitas, contador)
        }
    }

    private fun establecerHorarioNoDisponible(textViewsCitas: ArrayList<TextView>, contador: Int){
        textViewsCitas[contador].setBackgroundResource(R.drawable.background_citas_no_disponible)
    }

    private fun establecerHorarioOcupado(textViewsCitas: ArrayList<TextView>, contador: Int) {
        textViewsCitas[contador].setBackgroundResource(R.drawable.background_citas_ocupado)
    }

//    private fun establecerDatosCitaLaunch(textViewsCitas: ArrayList<TextView>, contadorBloques: Int, listaCitasLocal: ArrayList<Cita>,
//                                           contadorCitas:Int) = CoroutineScope(Dispatchers.Main).launch{
//        establecerDatosCita(textViewsCitas, contadorBloques, listaCitasLocal,  contadorCitas)
//    }

    private fun establecerDatosCita(textViewsCitas: ArrayList<TextView>, contadorBloques: Int, listaCitasLocal: ArrayList<Cita>,
                                     contadorCitas:Int) {
        textViewsCitas[contadorBloques].text = listaCitasLocal[contadorCitas].cliente.nombre
        textViewsCitas[contadorBloques].setOnClickListener {
            var dialog: EliminarCitaDialog = EliminarCitaDialog()
            dialog.idDocumentoEliminar = listaCitasLocal[contadorCitas].idDocumento
            dialog.show(CitasFragment.adminFragmento!!, "CitaCancelacionDialog")
        }
    }

}