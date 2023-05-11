package com.example.myanimection.views

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
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
        val appBarConfiguration = AppBarConfiguration(setOf(R.id.HomeFragment, R.id.SearchFragment))
        val navController = navHostFragment.navController

        setupActionBarWithNavController(navController, appBarConfiguration)

        navigationView.setupWithNavController(navController)



        /*navigationView.setOnItemSelectedListener {
            when(it.itemId) {
                R.id.HomeNavigationButton -> {
                    navController.navigate(R.id.action_HomeFragment_to_SearchFragment)
                    /*fragmentContainerView.removeAllViews()
                    supportFragmentManager.beginTransaction().add(R.id.fragmentContainerView, HomeFragment::class.java, null).commit()*/
                    true
                }
                R.id.SearchNavigationButton -> {
                    navController.navigate(R.id.action_SearchFragment_to_HomeFragment)
                    /*fragmentContainerView.removeAllViews()
                    supportFragmentManager.beginTransaction().add(R.id.fragmentContainerView, SearchFragment::class.java, null).commit()*/
                    true
                }
                else -> false
            }
        }*/
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater : MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_action_home, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId){
            R.id.itemLogOut -> {
                FirebaseAuth.getInstance().signOut()
                onBackPressedDispatcher.onBackPressed()
                true
            } else -> super.onOptionsItemSelected(item)

        }
    }
}