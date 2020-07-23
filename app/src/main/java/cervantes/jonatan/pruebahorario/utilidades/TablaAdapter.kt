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

const val PERIODO_SEPARACION_MINUTOS = 10

class TablaAdapter  {

    private var citasRv: List<CitaRV>
    private var view: View
    private var fechaActual = Calendar.getInstance(TimeZone.getTimeZone("GMT-7"))

    companion object {
        private var contadorPeriodosPendientes = 0
    }

    constructor(citasRv: List<CitaRV>, view: View) {
        this.citasRv = citasRv
        this.view = view
        contadorPeriodosPendientes = 0
        inicializarFechas()

        view.rv_citas.removeAllViews()
    }

    fun inflarVista(){

        for (position in view.resources.getStringArray(R.array.horarios).indices) {

            Log.d("ListaAdapter", "contador periodos" + contadorPeriodosPendientes.toString())

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

                //Se llenan los textviws de la vista que contienen las citas
                var listaCitasLocal = cita.listaCitas
                if(listaCitasLocal.isNotEmpty()) {
                    for (contadorCitas in listaCitasLocal.indices) {
                        var date = listaCitasLocal[contadorCitas].fecha.toDate()
                        var calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT-7"))
                        calendar.time = date
                        var minutoInicio = calendar.get(Calendar.MINUTE)
                        var duracion = listaCitasLocal[contadorCitas].servicio.duracion

                        var inicioCitaFloat = (minutoInicio.toFloat() / PERIODO_SEPARACION_MINUTOS)
                        var inicioCita = inicioCitaFloat.roundToInt()

                        var duracionCitaFloat = (duracion.toFloat() / PERIODO_SEPARACION_MINUTOS)
                        var duracionCita = duracionCitaFloat.roundToInt()

                        var terminacionCita = inicioCita + duracionCita

                        var compensacionParaNoDesborde = 0
                        if(terminacionCita < textViewsCitas.size) {
                            compensacionParaNoDesborde = (textViewsCitas.size - terminacionCita)
                        }

                        for (contadorBloques in 0 until (terminacionCita+compensacionParaNoDesborde)) {
                            if(contadorBloques < inicioCita && contadorCitas == 0 && contadorPeriodosPendientes == 0) {
                                establecerHorarioDisponible(textViewsCitas, contadorBloques,
                                    Integer.valueOf(citasRv[position].hora), Integer.valueOf(horariosMinutos[contadorBloques]))
                                //textViewsCitas[contadorBloques].setBackgroundResource(R.drawable.background_citas_disponible)
                            }
                            if(contadorPeriodosPendientes > 0 && contadorBloques<textViewsCitas.size) {
                                establecerHorarioOcupado(textViewsCitas, contadorBloques)
                                //textViewsCitas[contadorBloques].setBackgroundResource(R.drawable.background_citas_ocupado)
                                contadorPeriodosPendientes--
                            }
                            if(contadorBloques in inicioCita..(terminacionCita+compensacionParaNoDesborde)) {
                                //ESTO SIRVE PARA CUANDO LA CITA SE DESBORDA DE LA HORA
                                if(terminacionCita >= textViewsCitas.size) {
                                    if (contadorBloques >= textViewsCitas.size) {
                                        contadorPeriodosPendientes++
                                    } else {
                                        establecerHorarioOcupado(textViewsCitas, contadorBloques)
                                        //textViewsCitas[contadorBloques].setBackgroundResource(R.drawable.background_citas_ocupado)
                                    }
                                }

                                //ESTO SIRVE PARA CUANDO LA CITA NO SE DESBORDA DE LA HORA
                                if(terminacionCita < textViewsCitas.size) {
                                    if(contadorBloques >= terminacionCita) {
//                                        if(contadorBloques<textViewsCitas.size) {
                                        establecerHorarioDisponible(textViewsCitas, contadorBloques,
                                            Integer.valueOf(citasRv[position].hora), Integer.valueOf(horariosMinutos[contadorBloques]))
                                            //textViewsCitas[contadorBloques].setBackgroundResource(R.drawable.background_citas_disponible)
//                                        }//
                                    } else {
                                        establecerHorarioOcupado(textViewsCitas, contadorBloques)
                                        //textViewsCitas[contadorBloques].setBackgroundResource(R.drawable.background_citas_ocupado)
                                    }
                                }
                            }

                            if(contadorBloques == inicioCita) {
                                establecerDatosCita(textViewsCitas, contadorBloques, listaCitasLocal, cita.listaIdDocumento, contadorCitas)
                            }
                        }
                    }
                } else {
                    for (i in textViewsCitas.indices) {
                        if(contadorPeriodosPendientes > 0 ) {
                            establecerHorarioOcupado(textViewsCitas, i)
                            //textViewsCitas[i].setBackgroundResource(R.drawable.background_citas_ocupado)
                            contadorPeriodosPendientes--
                        } else {
                            establecerHorarioDisponible(textViewsCitas, i,
                                Integer.valueOf(citasRv[position].hora), Integer.valueOf(horariosMinutos[i]))
                            //textViewsCitas[i].setBackgroundResource(R.drawable.background_citas_disponible)
                        }
                        //textViewsCitas[i].text = ""
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

    private fun establecerHorarioDisponibleLaunch(textViewsCitas: ArrayList<TextView>, contador: Int, hora:Int, minutos: Int ) = CoroutineScope(Dispatchers.Main).launch{
        establecerHorarioDisponible(textViewsCitas, contador, hora, minutos)
    }

    private fun establecerHorarioDisponible(textViewsCitas: ArrayList<TextView>, contador: Int, hora:Int, minutos: Int ) {
        if(CitasFragment.fechaSeleccionada.after(fechaActual)) {
            textViewsCitas[contador].setBackgroundResource(R.drawable.background_citas_disponible)
            textViewsCitas[contador].setOnClickListener {
                CitasFragment.fechaSeleccionada.set(Calendar.HOUR_OF_DAY, hora)
                CitasFragment.fechaSeleccionada.set(Calendar.MINUTE, minutos)
                CitasFragment.fechaSeleccionada.set(Calendar.SECOND, 0)

                var dialog: AgregarCitaDialog =
                    AgregarCitaDialog()
                dialog.show(CitasFragment.adminFragmento!!, "CustomDialog")
            }
        } else {
            establecerHorarioNoDisonible(textViewsCitas, contador)
        }
    }

    private fun establecerHorarioNoDisonible(textViewsCitas: ArrayList<TextView>, contador: Int){
        textViewsCitas[contador].setBackgroundResource(R.drawable.background_citas_no_disponible)
    }

    private fun establecerHorarioOcupado(textViewsCitas: ArrayList<TextView>, contador: Int) {
        textViewsCitas[contador].setBackgroundResource(R.drawable.background_citas_ocupado)
    }

    private fun establecerDatosCitaLaunch(textViewsCitas: ArrayList<TextView>, contadorBloques: Int, listaCitasLocal: ArrayList<Cita>,
                                          listaIdsDocumentos:  ArrayList<String>, contadorCitas:Int) = CoroutineScope(Dispatchers.Main).launch{
        establecerDatosCita(textViewsCitas, contadorBloques, listaCitasLocal, listaIdsDocumentos, contadorCitas)
    }

    private fun establecerDatosCita(textViewsCitas: ArrayList<TextView>, contadorBloques: Int, listaCitasLocal: ArrayList<Cita>,
                                    listaIdsDocumentos:  ArrayList<String>, contadorCitas:Int) {
        textViewsCitas[contadorBloques].text = listaCitasLocal[contadorCitas].cliente.nombre
        textViewsCitas[contadorBloques].setOnClickListener {
            var dialog: EliminarCitaDialog = EliminarCitaDialog()
            dialog.idDocumentoEliminar = listaIdsDocumentos[contadorCitas]
            dialog.show(CitasFragment.adminFragmento!!, "CitaCancelacionDialog")
        }
    }

    private fun inicializarFechas() {
        fechaActual.set(Calendar.HOUR_OF_DAY, 0)
        fechaActual.set(Calendar.MINUTE, 0)
        fechaActual.set(Calendar.SECOND, 0)
        fechaActual.set(Calendar.MILLISECOND, 0)
    }


}