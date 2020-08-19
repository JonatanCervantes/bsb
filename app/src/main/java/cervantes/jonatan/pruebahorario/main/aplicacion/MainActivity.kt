package cervantes.jonatan.pruebahorario.main.aplicacion

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.navigation.Navigation.findNavController
import cervantes.jonatan.pruebahorario.R
import cervantes.jonatan.pruebahorario.citas.aplicacion.ui.CitasFragment
import cervantes.jonatan.pruebahorario.login.aplicacion.LoginActivity
import cervantes.jonatan.pruebahorario.notificaciones.FirebaseService
import cervantes.jonatan.pruebahorario.usuarios.infraestructura.UsuariosRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val job = CoroutineScope(Dispatchers.IO).launch {
            auth = FirebaseAuth.getInstance()

            FirebaseService.sharedPref = getSharedPreferences("sharedPref", Context.MODE_PRIVATE)
            FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener {
                FirebaseService.token = it.token
            }

        }

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(setOf(
            R.id.nav_home,
//            R.id.nav_citas,
            R.id.nav_empleados,
            R.id.nav_servicios,
            R.id.nav_home
        ), drawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        navView.itemIconTintList = null

        CoroutineScope(Dispatchers.Main).launch {
            job.join()
            Log.d("MainActivity", auth.currentUser!!.displayName.toString())

            val headerView : View = navView.getHeaderView(0)
            val nombreUsuario = headerView.findViewById<TextView>(R.id.tv_nombreUsuario)
            val emailUsuario = headerView.findViewById<TextView>(R.id.tv_emailUsuario)

            nombreUsuario.text = auth.currentUser!!.displayName
            emailUsuario.text = auth.currentUser!!.email
        }

    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    fun cerrarSesion(item: MenuItem) {
        auth.signOut()
        UsuariosRepository.olvidarUsuarioActual()
        this.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE).edit().clear().apply()
        val contexto = this

        var intent = Intent(contexto, LoginActivity::class.java)
        intent.clearStack()
        startActivity(intent)
    }

//    fun startCitasFragment(view:View) {
////        val contexto = this
////        var intent = Intent(contexto, CitasFragment::class.java)
////        startActivity(intent)
//        val navController = findNavController(R.id.nav_host_fragment)
//        navController.navigate(R.id.nav_citas)
//    }

    fun Intent.clearStack() {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }







}
