package com.example.salaryprediction.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.salaryprediction.MainActivity
import com.example.salaryprediction.R
import com.example.salaryprediction.auth.AuthRepository
import com.example.salaryprediction.dashboard.DashboardActivity
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private lateinit var authRepository: AuthRepository
    
    private lateinit var greetingText: TextView
    private lateinit var userNameText: TextView
    private lateinit var cardSalary: MaterialCardView
    private lateinit var cardNews: MaterialCardView
    private lateinit var cardProfile: MaterialCardView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        
        authRepository = AuthRepository.getInstance()
        
        initViews(view)
        loadUserData()
        setupClickListeners()
        
        return view
    }

    private fun initViews(view: View) {
        greetingText = view.findViewById(R.id.greetingText)
        userNameText = view.findViewById(R.id.userNameText)
        cardSalary = view.findViewById(R.id.cardSalaryPrediction)
        cardNews = view.findViewById(R.id.cardNews)
        cardProfile = view.findViewById(R.id.cardProfile)
    }

    private fun loadUserData() {
        lifecycleScope.launch {
            val result = authRepository.getCurrentUserData()
            
            result.onSuccess { user ->
                // Set greeting based on time
                val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
                val greeting = when (hour) {
                    in 0..11 -> "Selamat Pagi"
                    in 12..14 -> "Selamat Siang"
                    in 15..18 -> "Selamat Sore"
                    else -> "Selamat Malam"
                }
                greetingText.text = greeting
                userNameText.text = user.displayName
            }
        }
    }

    private fun setupClickListeners() {
        // Card 1: Salary Prediction - ke MainActivity
        cardSalary.setOnClickListener {
            val intent = Intent(requireContext(), MainActivity::class.java)
            startActivity(intent)
        }

        // Card 2: News - switch ke NewsFragment
        cardNews.setOnClickListener {
            (activity as? DashboardActivity)?.navigateToFragment(
                NewsFragment(),
                R.id.nav_news
            )
        }

        // Card 3: Profile - switch ke ProfileFragment
        cardProfile.setOnClickListener {
            (activity as? DashboardActivity)?.navigateToFragment(
                ProfileFragment(),
                R.id.nav_profile
            )
        }
    }
}
