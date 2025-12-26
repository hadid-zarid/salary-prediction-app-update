package com.example.salaryprediction

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.salaryprediction.databinding.ActivityOnboardingBinding

class OnboardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Hide action bar for full screen experience
        supportActionBar?.hide()

        // Set click listener for FAB button
        binding.fabNext.setOnClickListener {
            // Mark that user has completed onboarding
            val sharedPref = getSharedPreferences("JobspotPrefs", MODE_PRIVATE)
            sharedPref.edit().putBoolean("isFirstTime", false).apply()

            // Navigate to LoginActivity
            val intent = Intent(this, com.example.salaryprediction.auth.LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}