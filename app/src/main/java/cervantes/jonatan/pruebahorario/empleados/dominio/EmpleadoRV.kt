package cervantes.jonatan.pruebahorario.empleados.dominio

data class EmpleadoRV ( var nombre:String,
                      var email:String,
                      var horario:String,
                      var idDocumento:String,
                        var disponibilidad:String,
                        var animacionDisponibilidad:Int,
                      var fotoPerfil:String) {}