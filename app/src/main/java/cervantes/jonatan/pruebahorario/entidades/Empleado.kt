package cervantes.jonatan.pruebahorario.entidades

import cervantes.jonatan.pruebahorario.utilidades.Disponibilidades

data class Empleado ( var idEmpleado: Int = 0,
                      var nombre:String = "Nombre Empleado",
                     var email:String = "correoEmpleado@gmail.com",
                     var horario:String = "horarioDefault",
                      var disponibilidad:String = Disponibilidades.FUERADETURNO.name,
                     var fotoPerfil: String = "") {}