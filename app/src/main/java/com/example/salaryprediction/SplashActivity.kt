package com.example.salaryprediction

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    private val SPLASH_DELAY = 2500L // 2.5 detik

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Hide action bar
        supportActionBar?.hide()

        // Navigate after delay
        Handler(Looper.getMainLooper()).postDelayed({
            navigateToNextScreen()
        }, SPLASH_DELAY)
    }

    private fun navigateToNextScreen() {
        // Check if first time user using SharedPreferences
        val sharedPref = getSharedPreferences("JobspotPrefs", MODE_PRIVATE)
        val isFirstTime = sharedPref.getBoolean("isFirstTime", true)

        val intent = if (isFirstTime) {
            // First time user → Go to Onboarding
            Intent(this, OnboardingActivity::class.java)
        } else {
            // Returning user → Go to Login (akan auto-redirect jika sudah login)
            Intent(this, com.example.salaryprediction.auth.LoginActivity::class.java)
        }

        startActivity(intent)
        finish()
    }
}