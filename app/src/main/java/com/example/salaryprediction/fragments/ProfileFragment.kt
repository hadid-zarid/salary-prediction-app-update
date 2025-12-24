package com.example.salaryprediction.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.salaryprediction.R
import com.example.salaryprediction.auth.AuthRepository
import com.example.salaryprediction.auth.LoginActivity
import com.example.salaryprediction.models.User
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProfileFragment : Fragment() {

    private lateinit var authRepository: AuthRepository
    private var currentUser: User? = null
    
    private lateinit var userNameText: MaterialTextView
    private lateinit var userEmailText: MaterialTextView
    private lateinit var userRoleText: MaterialTextView
    private lateinit var memberSinceText: MaterialTextView
    private lateinit var btnEditName: MaterialButton
    private lateinit var btnChangePassword: MaterialButton
    private lateinit var btnLogout: MaterialButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        
        authRepository = AuthRepository.getInstance()
        
        initViews(view)
        loadUserData()
        setupClickListeners()
        
        return view
    }

    private fun initViews(view: View) {
        userNameText = view.findViewById(R.id.userNameText)
        userEmailText = view.findViewById(R.id.userEmailText)
        userRoleText = view.findViewById(R.id.userRoleText)
        memberSinceText = view.findViewById(R.id.memberSinceText)
        btnEditName = view.findViewById(R.id.btnEditName)
        btnChangePassword = view.findViewById(R.id.btnChangePassword)
        btnLogout = view.findViewById(R.id.btnLogout)
    }

    private fun loadUserData() {
        lifecycleScope.launch {
            val result = authRepository.getCurrentUserData()
            
            result.onSuccess { user ->
                currentUser = user
                displayUserData(user)
            }.onFailure {
                Toast.makeText(requireContext(), "Gagal memuat data", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun displayUserData(user: User) {
        userNameText.text = user.displayName
        userEmailText.text = user.email
        userRoleText.text = if (user.role == "admin") "Administrator" else "User"
        
        user.createdAt?.let { timestamp ->
            val date = timestamp.toDate()
            val formatter = java.text.SimpleDateFormat("dd MMMM yyyy", java.util.Locale("id", "ID"))
            memberSinceText.text = "Bergabung sejak ${formatter.format(date)}"
        }
    }

    private fun setupClickListeners() {
        btnEditName.setOnClickListener {
            showEditNameDialog()
        }
        
        btnChangePassword.setOnClickListener {
            showChangePasswordDialog()
        }
        
        btnLogout.setOnClickListener {
            showLogoutConfirmation()
        }
    }

    private fun showEditNameDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_name, null)
        val nameInput = dialogView.findViewById<TextInputEditText>(R.id.nameInput)
        
        nameInput.setText(currentUser?.displayName)
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Edit Nama")
            .setView(dialogView)
            .setPositiveButton("Simpan") { _, _ ->
                val newName = nameInput.text.toString().trim()
                if (newName.isNotEmpty() && newName.length >= 3) {
                    updateDisplayName(newName)
                } else {
                    Toast.makeText(requireContext(), "Nama minimal 3 karakter", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun updateDisplayName(newName: String) {
        lifecycleScope.launch {
            try {
                val uid = authRepository.getCurrentUser()?.uid ?: return@launch
                
                val result = authRepository.updateUserProfile(uid, newName)
                
                result.onSuccess {
                    Toast.makeText(requireContext(), "Nama berhasil diupdate", Toast.LENGTH_SHORT).show()
                    loadUserData() // Reload data
                }.onFailure {
                    Toast.makeText(requireContext(), "Gagal update nama", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showChangePasswordDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_change_password, null)
        val oldPasswordInput = dialogView.findViewById<TextInputEditText>(R.id.oldPasswordInput)
        val newPasswordInput = dialogView.findViewById<TextInputEditText>(R.id.newPasswordInput)
        val confirmPasswordInput = dialogView.findViewById<TextInputEditText>(R.id.confirmPasswordInput)
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Ganti Password")
            .setView(dialogView)
            .setPositiveButton("Simpan") { _, _ ->
                val oldPassword = oldPasswordInput.text.toString()
                val newPassword = newPasswordInput.text.toString()
                val confirmPassword = confirmPasswordInput.text.toString()
                
                if (validatePasswordChange(oldPassword, newPassword, confirmPassword)) {
                    changePassword(oldPassword, newPassword)
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun validatePasswordChange(old: String, new: String, confirm: String): Boolean {
        if (old.isEmpty() || new.isEmpty() || confirm.isEmpty()) {
            Toast.makeText(requireContext(), "Semua field harus diisi", Toast.LENGTH_SHORT).show()
            return false
        }
        
        if (new.length < 6) {
            Toast.makeText(requireContext(), "Password baru minimal 6 karakter", Toast.LENGTH_SHORT).show()
            return false
        }
        
        if (new != confirm) {
            Toast.makeText(requireContext(), "Password baru tidak sama", Toast.LENGTH_SHORT).show()
            return false
        }
        
        return true
    }

    private fun changePassword(oldPassword: String, newPassword: String) {
        lifecycleScope.launch {
            try {
                val user = FirebaseAuth.getInstance().currentUser
                val email = user?.email ?: return@launch
                
                // Re-authenticate user
                val credential = EmailAuthProvider.getCredential(email, oldPassword)
                user.reauthenticate(credential).await()
                
                // Update password
                user.updatePassword(newPassword).await()
                
                Toast.makeText(requireContext(), "Password berhasil diubah", Toast.LENGTH_SHORT).show()
                
            } catch (e: Exception) {
                val message = when {
                    e.message?.contains("password is invalid") == true -> 
                        "Password lama salah"
                    else -> 
                        "Gagal mengubah password: ${e.message}"
                }
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showLogoutConfirmation() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Logout")
            .setMessage("Apakah Anda yakin ingin keluar?")
            .setPositiveButton("Ya") { _, _ ->
                performLogout()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun performLogout() {
        authRepository.logout()
        
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }
}
