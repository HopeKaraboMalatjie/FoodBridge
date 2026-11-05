package com.foodbridge.app

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.foodbridge.app.databinding.ActivityMainBinding
import com.foodbridge.app.util.LocaleHelper

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        val topLevelDestinations = setOf(
            R.id.nav_home_feed,
            R.id.nav_map_view,
            R.id.nav_post_listing,
            R.id.nav_impact_profile
        )

        val appBarConfiguration = AppBarConfiguration(topLevelDestinations)
        binding.toolbar.setupWithNavController(navController, appBarConfiguration)
        binding.bottomNavigation.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.nav_login, R.id.nav_register -> {
                    binding.toolbar.visibility = View.GONE
                    binding.bottomNavigation.visibility = View.GONE
                }
                R.id.nav_home_feed, R.id.nav_map_view, R.id.nav_post_listing, R.id.nav_impact_profile -> {
                    binding.toolbar.visibility = View.VISIBLE
                    binding.bottomNavigation.visibility = View.VISIBLE
                }
                else -> {
                    binding.toolbar.visibility = View.VISIBLE
                    binding.bottomNavigation.visibility = View.GONE
                }
            }
        }
    }
}
