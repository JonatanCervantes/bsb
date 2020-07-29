package cervantes.jonatan.pruebahorario.utilidades

import java.util.*

object FechasHelper {

    private lateinit var fechaActualReal:Calendar
    private var fechaParaCitas:Calendar
    private lateinit var fechaQueryActual:Calendar
    private lateinit var fechaQueryFutura:Calendar

    init {
        fechaParaCitas = Calendar.getInstance(TimeZone.getTimeZone("GMT-7"))
        inicializarFechasCeros()
        inicializarFechaActualReal()
    }

    fun inicializarFechasCeros() {
        fechaParaCitas.set(Calendar.HOUR_OF_DAY, 0)
        fechaParaCitas.set(Calendar.MINUTE, 0)
        fechaParaCitas.set(Calendar.SECOND, 1)
        fechaParaCitas.set(Calendar.MILLISECOND, 0)
        ajustarFechasQuerys()
    }

    fun ajustarFechasQuerys() {
        fechaQueryActual = obtenerCopiaFechaParaCitas()
        fechaQueryFutura = obtenerCopiaFechaParaCitas()
        fechaQueryFutura.add(Calendar.DAY_OF_YEAR, 1)
    }

    fun inicializarFechaActualReal() {
        fechaActualReal = obtenerCopiaFechaParaCitas()
        fechaActualReal.set(Calendar.SECOND, 0)
    }

    fun obtenerFechaParaCitas():Calendar {
        return fechaParaCitas
    }

    fun obtenerCopiaFechaParaCitas():Calendar {
        return fechaParaCitas.clone() as Calendar
    }

    fun modificarFechaParaCitas(nuevaFecha:Calendar) {
        fechaParaCitas = nuevaFecha.clone() as Calendar
    }

    fun modificarFechaParaCitas(diasModificados:Int) {
        fechaParaCitas.add(Calendar.DAY_OF_YEAR, diasModificados)
        ajustarFechasQuerys()
    }

    fun obtenerFechaQueryActual():Calendar{
        return fechaQueryActual
    }

    fun obtenerFechaQueryFutura():Calendar{
        return fechaQueryFutura
    }

    fun obtenerFechaActualReal():Calendar{
        return fechaActualReal
    }




}