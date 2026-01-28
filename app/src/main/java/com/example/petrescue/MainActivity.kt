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
        
        // IMPORTANT: Replace these with your actual Firebase project values 
        // until you add the google-services.json file.
        // You can find these values in the Firebase Console under Project Settings.
        try {
            FirebaseApp.getInstance()
        } catch (e: Exception) {
            val options = FirebaseOptions.Builder()
                .setApplicationId("YOUR_APP_ID") // e.g., "1:123456789:android:abc123"
                .setApiKey("YOUR_API_KEY")       // e.g., "AIzaSy..."
                .setProjectId("YOUR_PROJECT_ID") // e.g., "petrescue-123"
                .build()
            FirebaseApp.initializeApp(this, options)
        }
        
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}