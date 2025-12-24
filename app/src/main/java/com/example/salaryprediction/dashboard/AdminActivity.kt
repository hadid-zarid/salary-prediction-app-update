package com.example.salaryprediction.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.salaryprediction.R
import com.example.salaryprediction.auth.AuthRepository
import com.example.salaryprediction.auth.LoginActivity
import com.example.salaryprediction.models.User
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.textview.MaterialTextView
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.activity.OnBackPressedCallback

class AdminActivity : AppCompatActivity() {

    private lateinit var authRepository: AuthRepository
    private var currentAdmin: User? = null
    private var allUsers: List<User> = emptyList()

    // UI Components
    private lateinit var adminNameText: MaterialTextView
    private lateinit var totalUsersText: MaterialTextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var logoutButton: MaterialButton
    private lateinit var refreshButton: MaterialButton
    private lateinit var progressIndicator: CircularProgressIndicator
    private lateinit var contentLayout: View
    private lateinit var emptyStateText: MaterialTextView

    private lateinit var userAdapter: UserAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finishAffinity()
            }
        })

        // Initialize repository
        authRepository = AuthRepository.getInstance()

        // Initialize views
        initViews()
        setupRecyclerView()
        
        // Load data
        loadAdminData()
        loadAllUsers()

        // Setup listeners
        setupListeners()
    }

    private fun initViews() {
        adminNameText = findViewById(R.id.adminNameText)
        totalUsersText = findViewById(R.id.totalUsersText)
        recyclerView = findViewById(R.id.recyclerView)
        logoutButton = findViewById(R.id.logoutButton)
        refreshButton = findViewById(R.id.refreshButton)
        progressIndicator = findViewById(R.id.progressIndicator)
        contentLayout = findViewById(R.id.contentLayout)
        emptyStateText = findViewById(R.id.emptyStateText)
    }

    private fun setupRecyclerView() {
        userAdapter = UserAdapter(emptyList())
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@AdminActivity)
            adapter = userAdapter
        }
    }

    private fun setupListeners() {
        logoutButton.setOnClickListener {
            performLogout()
        }

        refreshButton.setOnClickListener {
            loadAllUsers()
        }
    }

    private fun loadAdminData() {
        lifecycleScope.launch {
            val result = authRepository.getCurrentUserData()

            result.onSuccess { user ->
                currentAdmin = user
                adminNameText.text = "Admin: ${user.displayName}"
            }.onFailure {
                performLogout()
            }
        }
    }

    private fun loadAllUsers() {
        setLoading(true)

        lifecycleScope.launch {
            val result = authRepository.getAllUsers()

            setLoading(false)

            result.onSuccess { users ->
                allUsers = users
                updateUserList(users)
            }.onFailure { exception ->
                Toast.makeText(
                    this@AdminActivity,
                    "Gagal memuat data user: ${exception.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun updateUserList(users: List<User>) {
        if (users.isEmpty()) {
            recyclerView.visibility = View.GONE
            emptyStateText.visibility = View.VISIBLE
            totalUsersText.text = "Total: 0 user"
        } else {
            recyclerView.visibility = View.VISIBLE
            emptyStateText.visibility = View.GONE
            totalUsersText.text = "Total: ${users.size} user"
            userAdapter.updateData(users)
        }
    }

    private fun performLogout() {
        authRepository.logout()
        
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun setLoading(isLoading: Boolean) {
        if (isLoading) {
            progressIndicator.visibility = View.VISIBLE
            refreshButton.isEnabled = false
        } else {
            progressIndicator.visibility = View.GONE
            refreshButton.isEnabled = true
        }
    }


    // ===== RecyclerView Adapter =====
    inner class UserAdapter(private var users: List<User>) : 
        RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

        fun updateData(newUsers: List<User>) {
            users = newUsers
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_user, parent, false)
            return UserViewHolder(view)
        }

        override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
            holder.bind(users[position])
        }

        override fun getItemCount(): Int = users.size

        inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val nameText: MaterialTextView = itemView.findViewById(R.id.userNameText)
            private val emailText: MaterialTextView = itemView.findViewById(R.id.userEmailText)
            private val roleText: MaterialTextView = itemView.findViewById(R.id.userRoleText)
            private val dateText: MaterialTextView = itemView.findViewById(R.id.joinDateText)
            private val roleIndicator: View = itemView.findViewById(R.id.roleIndicator)

            fun bind(user: User) {
                nameText.text = user.displayName
                emailText.text = user.email
                
                // Set role badge
                if (user.role == "admin") {
                    roleText.text = "ADMIN"
                    roleText.setBackgroundResource(R.drawable.bg_role_admin)
                    roleIndicator.setBackgroundResource(R.drawable.indicator_admin)
                } else {
                    roleText.text = "USER"
                    roleText.setBackgroundResource(R.drawable.bg_role_user)
                    roleIndicator.setBackgroundResource(R.drawable.indicator_user)
                }

                // Format join date
                user.createdAt?.let { timestamp ->
                    val date = timestamp.toDate()
                    val formatter = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
                    dateText.text = "Bergabung: ${formatter.format(date)}"
                } ?: run {
                    dateText.text = "Baru bergabung"
                }
            }
        }
    }
}
