package cervantes.jonatan.pruebahorario

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.view.isVisible
import cervantes.jonatan.pruebahorario.utilidades.TiposUsuario
import cervantes.jonatan.pruebahorario.entidades.Usuario
import cervantes.jonatan.pruebahorario.utilidades.RolUsuario
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.lang.Exception
import kotlin.coroutines.CoroutineContext

const val REQUEST_CODE_SIGN_IN = 999
const val REQUEST_CODE_CANCELLED = 12501

class LoginActivity : AppCompatActivity() {

    lateinit var auth: FirebaseAuth
    lateinit var account: GoogleSignInAccount

    private val usuariosCollectionRef = Firebase.firestore.collection("usuarios")
    private val clavesCollectionRef = Firebase.firestore.collection("claves")
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
                startActivityForResult(it, REQUEST_CODE_SIGN_IN)
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

        val sharedPref = this?.getSharedPreferences(
            getString(R.string.preference_file_key), Context.MODE_PRIVATE)

        btn_verificarEmpleado.setOnClickListener {
            val clave = et_claveEmpleado.text.toString()

            //MANERA CORRECTA (APARENTEMENTE) DE MANEJAR LAS EJECUCIONES ASINCRONAS ...
            val empleadoVerificadoDeferred = verificarClaveEmpleadoAsync(clave)

           CoroutineScope(Dispatchers.IO).launch {
                if(empleadoVerificadoDeferred.await()) {
                    empleadoVerificado = true
                    //crearPreferenciasCompartidasLaunch(empleadoVerificado)
                }

            }
            //HASTA AQUI
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

                if(verificarUsuarioNoRegistrado()) {
                    registrarUsuario(account)
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

    private fun verificarUsuarioNoRegistrado() : Boolean{
        var usuarioNoRegistrado = true
        val coincidencias = CoroutineScope(Dispatchers.IO).async {
            try {
                val querySnapshot = usuariosCollectionRef.whereEqualTo("idUsuario", auth.uid).get().await()
                usuarioNoRegistrado = querySnapshot.isEmpty
            } catch (e:Exception) {
               Log.d("LoginActivity", e.message)
            }
        }

       runBlocking {
            coincidencias.await()
            Log.d("LoginActivity", "Termino de esperar coincidencias para usuarios registrados")
        }

        Log.d("LoginActivity", "Regreso $usuarioNoRegistrado")

        return usuarioNoRegistrado
    }

    private fun verificarClaveEmpleadoAsync(clave:String) = CoroutineScope(Dispatchers.IO).async {
        verificarClaveEmpleado(clave)
    }

    private suspend fun verificarClaveEmpleado(clave:String) : Boolean {
            try {
                val query = clavesCollectionRef.whereEqualTo("clave", clave).get().await()
                if(!query.isEmpty) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@LoginActivity, "Empleado verificado correctamente", Toast.LENGTH_LONG).show()
                    }
                    return true
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@LoginActivity, "Clave incorrecta", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e:Exception) {
                Log.d("LoginActivity", e.message)
            }
        return false
    }

    private fun crearPreferenciasCompartidasLaunch(empleadoVerificado: Boolean) = CoroutineScope(Dispatchers.IO).launch {
        crearPreferenciasCompartidas(empleadoVerificado)
    }

    private suspend fun crearPreferenciasCompartidas(empleadoVerificado:Boolean) {
        lateinit var tipoEmpleado:String
        if(empleadoVerificado) {
            tipoEmpleado = TiposUsuario.EMPLEADO.name
        } else {
            tipoEmpleado = TiposUsuario.CLIENTE.name
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


    private fun registrarUsuario(account: GoogleSignInAccount) {
        val nombre = account.displayName
        val email = account.email
        val idUsuario = auth.uid
        lateinit var tipoUsuario:String

        if(empleadoVerificado) {
            tipoUsuario = TiposUsuario.EMPLEADO.name
        } else {
            tipoUsuario = TiposUsuario.CLIENTE.name
        }


        var usuario = Usuario(nombre!!, email!!, tipoUsuario, idUsuario!!)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                usuariosCollectionRef.add(usuario).await()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@LoginActivity, "Usuario registrado correctamente", Toast.LENGTH_LONG).show()
                }
            } catch (e:Exception) {
                Log.d("LoginActivity", e.message)
            }
        }
    }









}