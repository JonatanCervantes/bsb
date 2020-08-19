package cervantes.jonatan.pruebahorario.notificaciones

data class PushNotification (
    val data: NotificationData,
    val to:String
) {
}