package cervantes.jonatan.pruebahorario.utilidades

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import cervantes.jonatan.pruebahorario.R
import cervantes.jonatan.pruebahorario.login.aplicacion.LoginActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.lang.Exception

class SplashScreenActivity : AppCompatActivity() {
    private val MILIS_DELAY = 500L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        try {
            Handler().postDelayed(Runnable {
                cambiarActivity()
                finish()
            }, MILIS_DELAY)
        } catch (e:Exception) {
            Log.d("SplashScreenActivity", e.message)
        }

    }

    fun cambiarActivity(){
        var intent = Intent(this@SplashScreenActivity, LoginActivity::class.java)
            startActivity(intent)
    }



}