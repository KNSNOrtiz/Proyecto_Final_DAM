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

class MainActivity : AppCompatActivity() {

    private lateinit var navigationView: BottomNavigationView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        navigationView = findViewById(R.id.navView)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        val navController = navHostFragment.navController

        val destinations = setOf(R.id.HomeFragment, R.id.SearchFragment, R.id.ProfileFragment)
        val appBarConfiguration = AppBarConfiguration(destinations)
        navController.addOnDestinationChangedListener { controller,  destination, bundle ->
            if (destination.id in arrayOf(R.id.HomeFragment, R.id.SearchFragment, R.id.ProfileFragment)) {
                navigationView.menu.findItem(destination.id)?.isChecked = true
                navigationView.menu.setGroupCheckable(0, true, true)
            } else {
                navigationView.menu.setGroupCheckable(0, false, true)
            }
        }
        setupActionBarWithNavController(navController, appBarConfiguration)
        setupActionBarWithNavController(navController, appBarConfiguration)

        navigationView.setupWithNavController(navController)
    }
}