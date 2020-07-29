package cervantes.jonatan.pruebahorario.entidades

import com.google.firebase.Timestamp
import java.util.*

data class Cita (var idCita: Int = -1,
                 var cliente:Usuario = Usuario(idUsuario = "-1"),
                 var empleado:Empleado = Empleado(idEmpleado = -1),
                 var servicio:Servicio = Servicio(-1),
                 var fecha: Timestamp = Timestamp(0,0),
                 var idDocumento: String = ""
)
