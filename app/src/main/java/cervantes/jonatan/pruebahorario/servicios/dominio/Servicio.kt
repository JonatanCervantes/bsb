package cervantes.jonatan.pruebahorario.servicios.dominio

data class Servicio (var idServicio:Int = 0,
                     var nombre:String = "ServicioX",
                     var precio:Float = 100.0f,
                     var duracion:Int = 45,
                     var imagen:String = "",
                     var idDocumento:String = "")