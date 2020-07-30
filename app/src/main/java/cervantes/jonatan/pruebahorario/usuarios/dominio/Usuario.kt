package cervantes.jonatan.pruebahorario.usuarios.dominio

data class Usuario(var nombre:String = "Nombre Default",
                   var email:String = "correoDefault@gmail.com",
                   var tipo:String = "tipoDefault",
                   var idUsuario: String = "") {}