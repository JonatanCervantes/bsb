package cervantes.jonatan.pruebahorario.citas.aplicacion

import android.view.LayoutInflater
import android.view.View
import android.widget.GridLayout
import android.widget.TextView
import cervantes.jonatan.pruebahorario.R
import cervantes.jonatan.pruebahorario.citas.aplicacion.ui.CitasFragment
import cervantes.jonatan.pruebahorario.citas.dominio.Cita
import cervantes.jonatan.pruebahorario.citas.dominio.CitaRV
import cervantes.jonatan.pruebahorario.empleados.infraestructura.EmpleadosRepository
import cervantes.jonatan.pruebahorario.usuarios.infraestructura.UsuariosRepository
import cervantes.jonatan.pruebahorario.utilidades.FechasHelper
import cervantes.jonatan.pruebahorario.utilidades.Par
import kotlinx.android.synthetic.main.fragment_citas.view.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.roundToInt
import kotlin.properties.Delegates

const val PERIODO_SEPARACION_MINUTOS = 10

class TablaAdapter  {

    private var citasRv: List<CitaRV>
    private var view: View
    private var idEmpleadoSeleccionado:String
    private lateinit var horarioDiaEmpleado:ArrayList<Par>
    private var posicion: Int = -5

    companion object {
        private var contadorPeriodosPendientes = 0
    }

    constructor(citasRv: List<CitaRV>, view: View, idEmpleadoSeleccionado: String) {
        this.citasRv = citasRv
        this.view = view
        this.idEmpleadoSeleccionado = idEmpleadoSeleccionado
        contadorPeriodosPendientes = 0
        horarioDiaEmpleado= EmpleadosRepository.obtenerEmpleado(idEmpleadoSeleccionado)!!.horariosMap[FechasHelper.obtenerDayOfWeekFechaParaCitas().toString()]!!
        view.rv_citas.removeAllViews()
    }

    fun inflarVista(){

        if(horarioDiaEmpleado.isNotEmpty() && horarioDiaEmpleado[0].first != "-1") {
            for (position in horarioDiaEmpleado.indices) {
                posicion = position
//                if(position != horarios.lastIndex) {
                    var cita = citasRv[position]

                    var inflador = LayoutInflater.from(view.context)
                    var vista = inflador.inflate(R.layout.cita_view, null)

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
                                date = listaCitasLocal[contadorCitas].fechaInicio.toDate()
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

//                }

            }
        } else {
            var inflador = LayoutInflater.from(view.context)
            var vista = inflador.inflate(R.layout.sinhorarios_view, null)
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

    private fun establecerHorarioDisponible(textViewsCitas: ArrayList<TextView>, contador: Int, hora:Int, minutos: Int ) {
        if(FechasHelper.obtenerFechaParaCitas().before(FechasHelper.obtenerFechaActualReal())) {
            establecerHorarioNoDisponible(textViewsCitas, contador)
        }
//        else if(Integer.valueOf(horarioDiaEmpleado[0].first) == hora && minutos < Integer.valueOf(horarioDiaEmpleado[0].second)) {
//            establecerHorarioNoDisponible(textViewsCitas, contador)
//        }
//        else if(Integer.valueOf(horarioDiaEmpleado[horarioDiaEmpleado.lastIndex].first) == hora &&
//            minutos > Integer.valueOf(horarioDiaEmpleado[horarioDiaEmpleado.lastIndex].second)
//            && horarioDiaEmpleado[horarioDiaEmpleado.lastIndex].horaTruncada == "-2") {
//            establecerHorarioNoDisponible(textViewsCitas, contador)        }
        else if ((horarioDiaEmpleado[posicion]).tipoHora == "-2") {
            establecerHorarioNoDisponible(textViewsCitas, contador)
        } else if (horarioDiaEmpleado[posicion].tipoHora == "-3" && minutos < Integer.valueOf(horarioDiaEmpleado[posicion].second)) {
            establecerHorarioNoDisponible(textViewsCitas, contador)
        }  else if (horarioDiaEmpleado[posicion].tipoHora == "-4" && minutos >= Integer.valueOf(horarioDiaEmpleado[posicion].second)) {
            establecerHorarioNoDisponible(textViewsCitas, contador)
         }else {
            textViewsCitas[contador].setBackgroundResource(R.drawable.background_citas_disponible)
            textViewsCitas[contador].setOnClickListener {
                val copiaFechaCita = FechasHelper.obtenerCopiaFechaParaCitas()
                copiaFechaCita.set(Calendar.HOUR_OF_DAY, hora)
                copiaFechaCita.set(Calendar.MINUTE, minutos)
                copiaFechaCita.set(Calendar.SECOND, 0)
                var dialog: AgregarCitaDialog =
                    AgregarCitaDialog(
                        copiaFechaCita,
                        idEmpleadoSeleccionado
                    )
                dialog.show(CitasFragment.adminFragmento!!, "CustomDialog")
            }
        }
    }

    private fun establecerHorarioNoDisponible(textViewsCitas: ArrayList<TextView>, contador: Int){
        textViewsCitas[contador].setBackgroundResource(R.drawable.background_citas_no_disponible)
    }

    private fun establecerHorarioOcupado(textViewsCitas: ArrayList<TextView>, contador: Int) {
        textViewsCitas[contador].setBackgroundResource(R.drawable.background_citas_ocupado)
    }

    private fun establecerDatosCita(textViewsCitas: ArrayList<TextView>, contadorBloques: Int, listaCitasLocal: ArrayList<Cita>,
                                    contadorCitas:Int) {
        textViewsCitas[contadorBloques].text = "Apartado: ".plus(listaCitasLocal[contadorCitas].cliente.nombre)

        if(UsuariosRepository.verificarUsuarioActualPerteneceCita(listaCitasLocal[contadorCitas].cliente)) {
            textViewsCitas[contadorBloques].setOnClickListener {
                var dialog: EliminarCitaDialog =
                    EliminarCitaDialog()
                dialog.idDocumentoEliminar = listaCitasLocal[contadorCitas].idDocumento
                dialog.show(CitasFragment.adminFragmento!!, "CitaCancelacionDialog")
            }
        }

    }

}