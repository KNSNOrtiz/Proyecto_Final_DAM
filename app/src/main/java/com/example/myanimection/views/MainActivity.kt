package com.example.myanimection.views

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.myanimection.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

/**
 * Actividad principal que representa la pantalla principal de la aplicación.
 * Gestiona la navegación entre los fragments mediante Navigation Component.
 */
class MainActivity : AppCompatActivity() {

    //  Barra inferior de navegación que contiene las tres vistas principales de la aplicación.
    lateinit var navigationView: BottomNavigationView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        navigationView = findViewById(R.id.navView)

        //  Asignación del controlador de navegación al contenedor que contiene las vistas en el MainActivity.
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        val navController = navHostFragment.navController

        //  Asignación de los diferentes destinos que tiene la navegación en la barra inferior.
        val destinations = setOf(R.id.HomeFragment, R.id.SearchFragment, R.id.ProfileFragment)
        val appBarConfiguration = AppBarConfiguration(destinations)

        // Listener que detecta las navegaciones para decidir si se resalta el icono correspondiente en la barra inferior.
        navController.addOnDestinationChangedListener { controller,  destination, bundle ->
            if (destination.id in arrayOf(R.id.HomeFragment, R.id.SearchFragment, R.id.ProfileFragment)) {
                navigationView.menu.findItem(destination.id)?.isChecked = true
                navigationView.menu.setGroupCheckable(0, true, true)
            } else {
                navigationView.menu.setGroupCheckable(0, false, true)
            }
        }
        setupActionBarWithNavController(navController, appBarConfiguration)
        navigationView.setupWithNavController(navController)
    }
}