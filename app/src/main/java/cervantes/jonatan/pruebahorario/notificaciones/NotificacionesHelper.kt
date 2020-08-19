package cervantes.jonatan.pruebahorario.notificaciones

import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object NotificacionesHelper {


    /**
     * Envia notificaciones al usuario y empleado
     */
    fun enviarNotificacionesLaunch(title:String, message:String, tokens: ArrayList<String>) = CoroutineScope(Dispatchers.Default).launch{
        tokens.forEach { token ->
            PushNotification(NotificationData(title, message), token).also {
                sendNotification(it)
            }
        }
    }

    private fun sendNotification(notification:PushNotification) = CoroutineScope(Dispatchers.IO).launch {
        try {
            val response = RetrofitInstance.api.postNotification(notification)
            if(response.isSuccessful) {
                Log.d("ServiciosFragment", "Response: ${Gson().toJson(response)}")
            } else {
                Log.d("ServiciosFragment", response.errorBody().toString())
            }
        } catch (e:Exception) {
            Log.d("ServiciosFragment", e.toString())
        }
    }



}