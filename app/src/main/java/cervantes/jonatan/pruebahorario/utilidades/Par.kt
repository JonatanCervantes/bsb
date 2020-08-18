package cervantes.jonatan.pruebahorario.utilidades

data class Par(var first: String, var second: String, var tipoHora:String = "-1", var horaTruncada:String ="-1") {

    constructor() : this(first = "-1", second="-1", tipoHora = "-1", horaTruncada = "-1")

    /**
     * var horaTruncada, puede tener 2 valores:
     * "-1" si no se encuentra truncada
     * "-2" si se encuentra truncada
     */

    /**
     * var tipoHora, puede tener 3 valores:
     * "-1" si es una hora completa normal de trabajo
     * "-2" si es una hora donde el empleado no esta disponible
     * "-3" si es una hora truncada de trabajo con espacio libre hacia abajo
     * "-4" si es una hora truncada de trabajo con espacio libre hacia arriba
     */


}