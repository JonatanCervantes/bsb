package cervantes.jonatan.pruebahorario.empleados.aplicacion

import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import cervantes.jonatan.pruebahorario.R
import cervantes.jonatan.pruebahorario.utilidades.Par
import kotlinx.android.synthetic.main.dialog_empleado_agregar.*
import kotlinx.android.synthetic.main.dialog_seleccionar_horario.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class SeleccionarHorarioDialog(val dialogPadre:AgregarEmpleadoDialog) : DialogFragment() {

    lateinit var contexto: Context
    var horariosMap:HashMap<String, ArrayList<Par>> = HashMap()
    var horariosMapInicializado = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.dialog_seleccionar_horario, container, false)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        contexto = view.context

        et_horaInicio.setOnClickListener {
            val cal = Calendar.getInstance()
            val timeSetListener = TimePickerDialog.OnTimeSetListener { timePicker, hour, minute ->
                cal.set(Calendar.HOUR_OF_DAY, hour)
                cal.set(Calendar.MINUTE, minute)

                et_horaInicio.text = Editable.Factory.getInstance().newEditable(SimpleDateFormat("HH:mm").format(cal.time))
            }
            TimePickerDialog(context,R.style.TimePickerTheme, timeSetListener, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), false).show()
        }

        et_horaFin.setOnClickListener {
            val cal = Calendar.getInstance()
            val timeSetListener = TimePickerDialog.OnTimeSetListener { timePicker, hour, minute ->
                cal.set(Calendar.HOUR_OF_DAY, hour)
                cal.set(Calendar.MINUTE, minute)

                et_horaFin.text = Editable.Factory.getInstance().newEditable(SimpleDateFormat("HH:mm").format(cal.time))
            }

            TimePickerDialog(context,R.style.TimePickerTheme, timeSetListener, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), false).show()
        }

        btn_agregarSemiHorario.setOnClickListener {
            var horaEntrada = et_horaInicio.text.toString()
            var horaSalida = et_horaFin.text.toString()
            if(horaEntrada.isEmpty() || horaSalida.isEmpty()) {
                Toast.makeText(context, "Porfavor seleccione las horas de entrada y salida", Toast.LENGTH_SHORT).show()
            } else if(!checkMonday.isChecked && !checkTuesday.isChecked && !checkWednesday.isChecked &&
                !checkThursday.isChecked && !checkFriday.isChecked && !checkSaturday.isChecked && !checkSunday.isChecked) {
                Toast.makeText(context, "Porfavor seleccione los dias", Toast.LENGTH_SHORT).show()
            } else {
                var soloHoraEntrada = Integer.parseInt(horaEntrada.substring(0,2))
                var soloHoraSalida = Integer.parseInt(horaSalida.substring(0,2))

                var soloMinutoEntrada = Integer.parseInt(horaEntrada.substring(3,5))
                var soloMinutoSalida = Integer.parseInt(horaSalida.substring(3,5))

                if((soloMinutoEntrada == 0 || soloMinutoEntrada == 30) && (soloMinutoSalida == 0 || soloMinutoSalida == 30)) {
                    var horario:String = tv_horarioSeleccionado.text.toString()
                    if(horario.isNotEmpty()) {
                        horario = horario.plus(", ")
                    }

                    var semiHorario:String = ""
                    inicializarHorariosMap()

                    if(checkMonday.isChecked) {
                        semiHorario = semiHorario.plus("Lu")
                        horariosMap["2"] = calcularHoras("2",soloHoraEntrada, soloHoraSalida, soloMinutoEntrada, soloMinutoSalida)
                    }
                    if (checkTuesday.isChecked) {
                        semiHorario = semiHorario.plus("Ma")
                        horariosMap["3"] = calcularHoras("3",soloHoraEntrada, soloHoraSalida, soloMinutoEntrada, soloMinutoSalida)
                    }
                    if(checkWednesday.isChecked){
                        semiHorario = semiHorario.plus("Mi")
                        horariosMap["4"] = calcularHoras("4",soloHoraEntrada, soloHoraSalida, soloMinutoEntrada, soloMinutoSalida)
                    }
                    if(checkThursday.isChecked){
                        semiHorario = semiHorario.plus("Ju")
                        horariosMap["5"] = calcularHoras("5",soloHoraEntrada, soloHoraSalida, soloMinutoEntrada, soloMinutoSalida)
                    }
                    if(checkFriday.isChecked){
                        semiHorario = semiHorario.plus("Vi")
                        horariosMap["6"] = calcularHoras("6",soloHoraEntrada, soloHoraSalida, soloMinutoEntrada, soloMinutoSalida)
                    }
                    if(checkSunday.isChecked){
                        semiHorario = semiHorario.plus("Do")
                        horariosMap["1"] = calcularHoras("1", soloHoraEntrada, soloHoraSalida, soloMinutoEntrada, soloMinutoSalida)
                    }

                    semiHorario =  semiHorario.plus(" ").plus(horaEntrada).plus("-").plus(horaSalida)
                    horario = horario.plus(semiHorario)

                    tv_horarioSeleccionado.text = horario
                } else {
                    Toast.makeText(context, "Porfavor estableza los minutos a una hora exacta o media hora", Toast.LENGTH_SHORT).show()
                }

            }

        }

        btn_cancelarHorario.setOnClickListener {
            dialog!!.dismiss()
        }

        btn_agregarHorario.setOnClickListener {
            var horario:String = tv_horarioSeleccionado.text.toString()
            dialogPadre.establecerHorario(horario, horariosMap)
            dialog!!.dismiss()
        }
    }

    private fun calcularHoras(claveMapa:String, horaEntrada:Int, horaSalida:Int, minutoEntrada:Int, minutoSalida:Int):ArrayList<Par> {
        lateinit var arregloHorarios:ArrayList<Par>
        var listaNueva = false
        var horaSalidaCopia = horaSalida
        var arrayListAnterior = horariosMap[claveMapa]!!.get(0)

        if(arrayListAnterior!!.first == "-1") {
            arregloHorarios = ArrayList<Par>()
            listaNueva = true
        } else {
            arregloHorarios = horariosMap[claveMapa]!!.clone() as ArrayList<Par>
        }

        if(!listaNueva) {
            var horarioAnteriorUltimaHora = horariosMap[claveMapa]!!.last()
            for (i in (Integer.valueOf(horarioAnteriorUltimaHora.first)+1) until horaEntrada) {
                arregloHorarios.add(Par(i.toString(), "00", "-2"))
            }
        }

        if(minutoSalida == 0 ) {
            horaSalidaCopia -= 1
        }

        for (i in horaEntrada..horaSalidaCopia) {
            Log.d("SeleccionarHorario", i.toString())
            if(i == horaEntrada) {
                if(minutoEntrada == 30) {
                    arregloHorarios.add(Par(i.toString(), minutoEntrada.toString(), tipoHora = "-3", horaTruncada = "-2"))
                }else {
                    arregloHorarios.add(Par(i.toString(), minutoEntrada.toString()))
                }
            } else if(i == horaSalidaCopia) {
                if(minutoSalida == 30) {
                    arregloHorarios.add(Par(i.toString(), minutoSalida.toString(), tipoHora = "-4", horaTruncada = "-2"))
                } else {
                    arregloHorarios.add(Par(i.toString(), minutoSalida.toString()))
                }
            } else {
                arregloHorarios.add(Par(i.toString(), "00"))
            }

        }
        return arregloHorarios
    }

    private fun inicializarHorariosMap() {
        if(!horariosMapInicializado) {
            horariosMap["1"] = arrayListOf(Par("-1", "-1"))
            horariosMap["2"] = arrayListOf(Par("-1", "-1"))
            horariosMap["3"] = arrayListOf(Par("-1", "-1"))
            horariosMap["4"] = arrayListOf(Par("-1", "-1"))
            horariosMap["5"] = arrayListOf(Par("-1", "-1"))
            horariosMap["6"] = arrayListOf(Par("-1", "-1"))
            horariosMap["7"] = arrayListOf(Par("-1", "-1"))
        }
        horariosMapInicializado = true
    }


}