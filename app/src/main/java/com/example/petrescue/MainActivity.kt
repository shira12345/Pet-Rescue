package com.example.petrescue

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.petrescue.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // The NavHostFragment in activity_main.xml will automatically load 
        // the startDestination from nav_graph.xml (which is LoginFragment)
    }
}