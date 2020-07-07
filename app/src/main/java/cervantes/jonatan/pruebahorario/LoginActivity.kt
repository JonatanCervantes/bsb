package cervantes.jonatan.pruebahorario

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import cervantes.jonatan.pruebahorario.utilidades.TiposUsuario
import cervantes.jonatan.pruebahorario.entidades.Usuario
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

const val REQUEST_CODE_SIGN_IN = 999

class LoginActivity : AppCompatActivity() {

    lateinit var auth: FirebaseAuth
    private val usuariosCollectionRef = Firebase.firestore.collection("usuarios")

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
    }

    override fun onStart() {
        super.onStart()
        checkLoggedInState()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == REQUEST_CODE_SIGN_IN) {
            val account  = GoogleSignIn.getSignedInAccountFromIntent(data).result

            account?.let {
                googleAuthForFirebase(it)

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
                var querySnapshot = usuariosCollectionRef.whereEqualTo("idUsuario", auth.uid).get().await()
                usuarioNoRegistrado = querySnapshot.isEmpty
            } catch (e:Exception) {
               Log.d("LoginActivity", e.message)
            }
        }

       runBlocking {
            coincidencias.await()
            Log.d("LoginActivity", "Termino de esperar coincidencias")
        }

        Log.d("LoginActivity", "Regreso $usuarioNoRegistrado")

        return usuarioNoRegistrado
    }

    private fun registrarUsuario(account: GoogleSignInAccount) {
        val nombre = account.displayName
        val email = account.email
        val idUsuario = auth.uid
        val tipoUsuario = TiposUsuario.CLIENTE.name

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