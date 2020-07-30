package cervantes.jonatan.pruebahorario.utilidades

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import cervantes.jonatan.pruebahorario.R
import cervantes.jonatan.pruebahorario.login.aplicacion.LoginActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

class SplashScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

    }

    override fun onStart() {
        super.onStart()
        cambiarActivity()
    }

    fun cambiarActivity(){

        runBlocking {
            var intent = Intent(this@SplashScreenActivity, LoginActivity::class.java)
            delay(2000)
            startActivity(intent)
        }

    }



}