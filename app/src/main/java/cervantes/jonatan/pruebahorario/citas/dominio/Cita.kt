package cervantes.jonatan.pruebahorario.citas.dominio

import cervantes.jonatan.pruebahorario.empleados.dominio.Empleado
import cervantes.jonatan.pruebahorario.usuarios.dominio.Usuario
import cervantes.jonatan.pruebahorario.servicios.dominio.Servicio
import com.google.firebase.Timestamp

data class Cita (var idCita: Int = -1,
                 var cliente: Usuario = Usuario(
                     idUsuario = "-1"
                 ),
                 var empleado: Empleado = Empleado(
                     idEmpleado = -1
                 ),
                 var servicio: Servicio = Servicio(
                     -1
                 ),
                 var fechaInicio: Timestamp = Timestamp(0,0),
                 var fechaTermino: Timestamp = Timestamp(0, 0),
                 var idDocumento: String = ""
)
