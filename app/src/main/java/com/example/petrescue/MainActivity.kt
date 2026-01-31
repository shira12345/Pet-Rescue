package com.example.petrescue

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.petrescue.databinding.ActivityMainBinding
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            FirebaseApp.getInstance()
        } catch (e: Exception) {
            val options = FirebaseOptions.Builder()
                .setApplicationId("APP_ID")
                .setApiKey("API_KEY")
                .setProjectId("PROJECT_ID")
                .build()
            FirebaseApp.initializeApp(this, options)
        }
        
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}