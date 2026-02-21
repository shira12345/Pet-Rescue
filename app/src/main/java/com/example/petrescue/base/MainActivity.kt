package com.example.petrescue.base

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.petrescue.R
import com.example.petrescue.databinding.ActivityMainBinding
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

class MainActivity : AppCompatActivity() {
  private lateinit var binding: ActivityMainBinding

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    try {
      FirebaseApp.getInstance()
    } catch (exception: Exception) {
      val options = FirebaseOptions.Builder()
        .setApplicationId("APP_ID")
        .setApiKey("API_KEY")
        .setProjectId("PROJECT_ID")
        .build()

      FirebaseApp.initializeApp(this, options)
    }

    enableEdgeToEdge()

    binding = ActivityMainBinding.inflate(layoutInflater)
    setContentView(binding.root)

    ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
      val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
      v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
      insets
    }

    val navHostFragment =
      supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment

    val navController = navHostFragment.navController

    // Connect BottomNavigationView with NavController
    binding.bottomNavigation.setupWithNavController(navController)

    // Show/Hide bottom navigation based on the current screen
    navController.addOnDestinationChangedListener { _, destination, _ ->
      when (destination.id) {
        R.id.loginFragment, R.id.registerFragment -> {
          binding.bottomNavigation.visibility = View.GONE
        }

        else -> {
          binding.bottomNavigation.visibility = View.VISIBLE
        }
      }
    }
  }
}