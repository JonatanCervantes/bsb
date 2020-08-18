package cervantes.jonatan.pruebahorario.empleados.dominio

import cervantes.jonatan.pruebahorario.utilidades.Disponibilidades
import cervantes.jonatan.pruebahorario.utilidades.Par

data class Empleado ( var idEmpleado: Int = 0,
                      var nombre:String = "Nombre Empleado",
                     var email:String = "correoEmpleado@gmail.com",
                     var horario:String = "horarioDefault",
                      var horariosMap:HashMap<String, ArrayList<Par>> = HashMap<String, ArrayList<Par>>().apply {
                          this.put("-1",  arrayListOf(Par("-1", "-1")))
                      },
                      var disponibilidad:String = Disponibilidades.FUERADETURNO.name,
                     var fotoPerfil: String = "",
                      var idDocumento:String = ""
) {



}