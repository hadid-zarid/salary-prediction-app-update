package com.example.salaryprediction.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.salaryprediction.R
import com.example.salaryprediction.dashboard.AdminActivity
import com.example.salaryprediction.dashboard.DashboardActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import com.google.android.material.progressindicator.CircularProgressIndicator
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var authRepository: AuthRepository

    // UI Components
    private lateinit var emailInput: TextInputEditText
    private lateinit var passwordInput: TextInputEditText
    private lateinit var loginButton: MaterialButton
    private lateinit var registerButton: MaterialTextView
    private lateinit var progressIndicator: CircularProgressIndicator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Handle back press
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finishAffinity()
            }
        })

        // Initialize repository
        authRepository = AuthRepository.getInstance()

        // ✅ PINDAHKAN initViews() KE ATAS
        initViews()
        setupListeners()

        // ✅ SEKARANG CEK LOGIN DI BAWAH
        if (authRepository.isUserLoggedIn()) {
            checkUserRoleAndNavigate()
        }
    }

    private fun initViews() {
        emailInput = findViewById(R.id.emailInput)
        passwordInput = findViewById(R.id.passwordInput)
        loginButton = findViewById(R.id.loginButton)
        registerButton = findViewById(R.id.registerButton)
        progressIndicator = findViewById(R.id.progressIndicator)
    }

    private fun setupListeners() {
        loginButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (validateInput(email, password)) {
                performLogin(email, password)
            }
        }

        registerButton.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun validateInput(email: String, password: String): Boolean {
        if (email.isEmpty()) {
            emailInput.error = "Email tidak boleh kosong"
            emailInput.requestFocus()
            return false
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.error = "Format email tidak valid"
            emailInput.requestFocus()
            return false
        }

        if (password.isEmpty()) {
            passwordInput.error = "Password tidak boleh kosong"
            passwordInput.requestFocus()
            return false
        }

        if (password.length < 6) {
            passwordInput.error = "Password minimal 6 karakter"
            passwordInput.requestFocus()
            return false
        }

        return true
    }

    private fun performLogin(email: String, password: String) {
        setLoading(true)

        lifecycleScope.launch {
            val result = authRepository.login(email, password)

            setLoading(false)

            result.onSuccess { user ->
                Toast.makeText(
                    this@LoginActivity,
                    "Login berhasil! Selamat datang ${user.displayName}",
                    Toast.LENGTH_SHORT
                ).show()

                // Navigate berdasarkan role
                navigateBasedOnRole(user.role)
            }.onFailure { exception ->
                val errorMessage = when {
                    exception.message?.contains("password") == true ->
                        "Email atau password salah"
                    exception.message?.contains("network") == true ->
                        "Tidak ada koneksi internet"
                    else ->
                        "Login gagal: ${exception.message}"
                }
                Toast.makeText(this@LoginActivity, errorMessage, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun checkUserRoleAndNavigate() {
        setLoading(true)

        lifecycleScope.launch {
            val result = authRepository.getCurrentUserData()

            setLoading(false)

            result.onSuccess { user ->
                navigateBasedOnRole(user.role)
            }.onFailure {
                // Jika gagal get user data, logout dan stay di login
                authRepository.logout()
            }
        }
    }

    private fun navigateBasedOnRole(role: String) {
        val intent = if (role == "admin") {
            Intent(this, AdminActivity::class.java)
        } else {
            Intent(this, DashboardActivity::class.java)
        }

        // Clear back stack supaya tidak bisa kembali ke login
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun setLoading(isLoading: Boolean) {
        if (isLoading) {
            progressIndicator.visibility = View.VISIBLE
            loginButton.isEnabled = false
            registerButton.isEnabled = false
            emailInput.isEnabled = false
            passwordInput.isEnabled = false
        } else {
            progressIndicator.visibility = View.GONE
            loginButton.isEnabled = true
            registerButton.isEnabled = true
            emailInput.isEnabled = true
            passwordInput.isEnabled = true
        }
    }
}