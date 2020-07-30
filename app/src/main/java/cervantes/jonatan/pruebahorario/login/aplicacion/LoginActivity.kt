package cervantes.jonatan.pruebahorario.login.aplicacion

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.view.isVisible
import cervantes.jonatan.pruebahorario.main.aplicacion.MainActivity
import cervantes.jonatan.pruebahorario.R
import cervantes.jonatan.pruebahorario.usuarios.dominio.TiposUsuario
import cervantes.jonatan.pruebahorario.login.infraestructura.ClavesRepository
import cervantes.jonatan.pruebahorario.usuarios.infraestructura.UsuariosRepository
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.lang.Exception

const val REQUEST_CODE_SIGN_IN = 999
const val REQUEST_CODE_CANCELLED = 12501

class LoginActivity : AppCompatActivity() {

    lateinit var auth: FirebaseAuth
    lateinit var account: GoogleSignInAccount
    private var empleadoVerificado = false
    private val key = "ROL_USUARIO"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        btnGoogleSignIn.setOnClickListener {
            val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.web_client_id))
                .requestEmail()
                .build()
            val signInClient = GoogleSignIn.getClient(this, options)
            signInClient.signOut()

            signInClient.signInIntent.also {
                startActivityForResult(it,
                    REQUEST_CODE_SIGN_IN
                )
            }
        }

        cb_soyUnEmpleado.setOnClickListener {
            if(cb_soyUnEmpleado.isChecked) {
                rl_verificarEmpleado.isEnabled = true
                rl_verificarEmpleado.isVisible = true
            } else {
                rl_verificarEmpleado.isEnabled = false
                rl_verificarEmpleado.isVisible = false
            }
        }


        btn_verificarEmpleado.setOnClickListener {
            val clave = et_claveEmpleado.text.toString()

            val empleadoVerificadoDeferred = ClavesRepository.verificarClaveEmpleadoAsync(clave)

            CoroutineScope(Dispatchers.Default).launch {
                if(empleadoVerificadoDeferred.await()) {
                    empleadoVerificado = true
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@LoginActivity, "Usuario verificado correctamente", Toast.LENGTH_LONG).show()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@LoginActivity, "Clave incorrecta", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        checkLoggedInState()

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == REQUEST_CODE_SIGN_IN) {
            try {
                account  = GoogleSignIn.getSignedInAccountFromIntent(data).result!!

                account?.let {
                    crearPreferenciasCompartidasLaunch(empleadoVerificado)
                    googleAuthForFirebase(it)
                }
            } catch (e: Exception) {
                Log.d("LoginActivity", e.message)
            }
        }
    }

    private fun checkLoggedInState() {
        if(auth.currentUser != null) {
            enviarAHome()
        }
    }

    private fun enviarAHome() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }


    private fun googleAuthForFirebase(account:GoogleSignInAccount) {
        val credentials = GoogleAuthProvider.getCredential(account.idToken, null)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                auth.signInWithCredential(credentials).await()

                val usuarioRegistrado = UsuariosRepository.verificarUsuarioNoRegistradoAsync(auth.uid!!)
                if(usuarioRegistrado.await()) {
                    UsuariosRepository.registrarUsuario(account, auth.uid!!, empleadoVerificado)
                }
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@LoginActivity, "Logeado correctamente", Toast.LENGTH_LONG).show()
                }
                enviarAHome()
            } catch (e:Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@LoginActivity, e.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun crearPreferenciasCompartidasLaunch(empleadoVerificado: Boolean) = CoroutineScope(Dispatchers.IO).launch {
        crearPreferenciasCompartidas(empleadoVerificado)
    }

    private suspend fun crearPreferenciasCompartidas(empleadoVerificado:Boolean) {
        var tipoEmpleado:String = if(empleadoVerificado) {
            TiposUsuario.EMPLEADO.name
        } else {
            TiposUsuario.CLIENTE.name
        }

        try {
            val sharedPref = this?.getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE)

            with (sharedPref.edit()) {
                putString(getString(R.string.rol_usuario), tipoEmpleado)
                apply()
            }

        } catch (e:Exception) {
            Log.d("LoginActivity", e.message)
        }
    }

}