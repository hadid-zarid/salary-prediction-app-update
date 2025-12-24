package com.example.salaryprediction.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.salaryprediction.R
import com.example.salaryprediction.dashboard.DashboardActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import com.google.android.material.progressindicator.CircularProgressIndicator
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var authRepository: AuthRepository
    
    // UI Components
    private lateinit var nameInput: TextInputEditText
    private lateinit var emailInput: TextInputEditText
    private lateinit var passwordInput: TextInputEditText
    private lateinit var confirmPasswordInput: TextInputEditText
    private lateinit var registerButton: MaterialButton
    private lateinit var loginButton: MaterialTextView
    private lateinit var progressIndicator: CircularProgressIndicator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Initialize repository
        authRepository = AuthRepository.getInstance()

        // Initialize views
        initViews()
        setupListeners()
    }

    private fun initViews() {
        nameInput = findViewById(R.id.nameInput)
        emailInput = findViewById(R.id.emailInput)
        passwordInput = findViewById(R.id.passwordInput)
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput)
        registerButton = findViewById(R.id.registerButton)
        loginButton = findViewById(R.id.loginButton)
        progressIndicator = findViewById(R.id.progressIndicator)
    }

    private fun setupListeners() {
        registerButton.setOnClickListener {
            val name = nameInput.text.toString().trim()
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()
            val confirmPassword = confirmPasswordInput.text.toString().trim()

            if (validateInput(name, email, password, confirmPassword)) {
                performRegister(name, email, password)
            }
        }

        loginButton.setOnClickListener {
            finish() // Kembali ke LoginActivity
        }
    }

    private fun validateInput(
        name: String,
        email: String,
        password: String,
        confirmPassword: String
    ): Boolean {
        // Validate name
        if (name.isEmpty()) {
            nameInput.error = "Nama tidak boleh kosong"
            nameInput.requestFocus()
            return false
        }

        if (name.length < 3) {
            nameInput.error = "Nama minimal 3 karakter"
            nameInput.requestFocus()
            return false
        }

        // Validate email
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

        // Validate password
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

        // Validate confirm password
        if (confirmPassword.isEmpty()) {
            confirmPasswordInput.error = "Konfirmasi password tidak boleh kosong"
            confirmPasswordInput.requestFocus()
            return false
        }

        if (password != confirmPassword) {
            confirmPasswordInput.error = "Password tidak sama"
            confirmPasswordInput.requestFocus()
            return false
        }

        return true
    }

    private fun performRegister(name: String, email: String, password: String) {
        setLoading(true)

        lifecycleScope.launch {
            val result = authRepository.register(email, password, name)

            setLoading(false)

            result.onSuccess { user ->
                Toast.makeText(
                    this@RegisterActivity,
                    "Registrasi berhasil! Selamat datang ${user.displayName}",
                    Toast.LENGTH_SHORT
                ).show()

                // Auto login dan navigate ke dashboard
                navigateToDashboard()
            }.onFailure { exception ->
                val errorMessage = when {
                    exception.message?.contains("already in use") == true -> 
                        "Email sudah terdaftar"
                    exception.message?.contains("network") == true -> 
                        "Tidak ada koneksi internet"
                    exception.message?.contains("weak-password") == true -> 
                        "Password terlalu lemah"
                    else -> 
                        "Registrasi gagal: ${exception.message}"
                }
                Toast.makeText(this@RegisterActivity, errorMessage, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun navigateToDashboard() {
        val intent = Intent(this, DashboardActivity::class.java)
        // Clear back stack
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun setLoading(isLoading: Boolean) {
        if (isLoading) {
            progressIndicator.visibility = View.VISIBLE
            registerButton.isEnabled = false
            loginButton.isEnabled = false
            nameInput.isEnabled = false
            emailInput.isEnabled = false
            passwordInput.isEnabled = false
            confirmPasswordInput.isEnabled = false
        } else {
            progressIndicator.visibility = View.GONE
            registerButton.isEnabled = true
            loginButton.isEnabled = true
            nameInput.isEnabled = true
            emailInput.isEnabled = true
            passwordInput.isEnabled = true
            confirmPasswordInput.isEnabled = true
        }
    }
}
