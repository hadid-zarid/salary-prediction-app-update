package com.example.salaryprediction.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.salaryprediction.MainActivity
import com.example.salaryprediction.R
import com.example.salaryprediction.adapters.TopSalaryAdapter
import com.example.salaryprediction.adapters.TrendingJobAdapter
import com.example.salaryprediction.auth.AuthRepository
import com.example.salaryprediction.dashboard.DashboardActivity
import com.example.salaryprediction.models.TopSalaryLocation
import com.example.salaryprediction.models.TrendingJob
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.launch
import org.json.JSONObject

class HomeFragment : Fragment() {

    private lateinit var authRepository: AuthRepository

    private lateinit var greetingText: TextView
    private lateinit var userNameText: TextView
    private lateinit var cardSalary: MaterialCardView
    private lateinit var cardProfile: MaterialCardView
    private lateinit var rvTrendingJob: RecyclerView
    private lateinit var rvTopSalary: RecyclerView

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
        loadDataFromAssets()

        return view
    }

    private fun initViews(view: View) {
        greetingText = view.findViewById(R.id.greetingText)
        userNameText = view.findViewById(R.id.userNameText)
        cardSalary = view.findViewById(R.id.cardSalaryPrediction)
        cardProfile = view.findViewById(R.id.cardProfile)
        rvTrendingJob = view.findViewById(R.id.rvTrendingJob)
        rvTopSalary = view.findViewById(R.id.rvTopSalary)

        // Setup RecyclerView layouts
        rvTrendingJob.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.HORIZONTAL,
            false
        )
        rvTopSalary.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun loadUserData() {
        lifecycleScope.launch {
            val result = authRepository.getCurrentUserData()

            result.onSuccess { user ->
                // Set greeting based on time
                val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
                val greeting = when (hour) {
                    in 0..11 -> "Good Morning"
                    in 12..14 -> "Good Afternoon"
                    in 15..18 -> "Good Evening"
                    else -> "Good Night"
                }
                greetingText.text = greeting
                userNameText.text = "${user.displayName}."
            }
        }
    }

    private fun setupClickListeners() {
        // Card Salary Prediction - ke MainActivity
        cardSalary.setOnClickListener {
            val intent = Intent(requireContext(), MainActivity::class.java)
            startActivity(intent)
        }

        // Card Profile - switch ke ProfileFragment
        cardProfile.setOnClickListener {
            (activity as? DashboardActivity)?.navigateToFragment(
                ProfileFragment(),
                R.id.nav_profile
            )
        }
    }

    private fun loadDataFromAssets() {
        try {
            // Baca file home_data.json dari assets
            val jsonString = requireContext().assets
                .open("home_data.json")
                .bufferedReader()
                .use { it.readText() }

            val jsonObject = JSONObject(jsonString)

            // Load Trending Jobs
            loadTrendingJobs(jsonObject)

            // Load Top Salary Locations
            loadTopSalaryLocations(jsonObject)

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Error loading data", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadTrendingJobs(jsonObject: JSONObject) {
        val trendingJobsArray = jsonObject.getJSONArray("trending_jobs")
        val jobList = mutableListOf<TrendingJob>()

        for (i in 0 until trendingJobsArray.length()) {
            val jobObj = trendingJobsArray.getJSONObject(i)
            jobList.add(
                TrendingJob(
                    title = jobObj.getString("title"),
                    salary = jobObj.getDouble("salary"),
                    salaryFormatted = formatSalary(jobObj.getDouble("salary"))
                )
            )
        }

        // Setup adapter
        val adapter = TrendingJobAdapter(jobList) { job ->
            // Klik item -> buka MainActivity dengan pre-fill job title
            val intent = Intent(requireContext(), MainActivity::class.java).apply {
                putExtra("JOB_TITLE", job.title)
            }
            startActivity(intent)
        }
        rvTrendingJob.adapter = adapter
    }

    private fun loadTopSalaryLocations(jsonObject: JSONObject) {
        val locationsArray = jsonObject.getJSONArray("top_salary_locations")
        val locationList = mutableListOf<TopSalaryLocation>()

        for (i in 0 until locationsArray.length()) {
            val locObj = locationsArray.getJSONObject(i)
            locationList.add(
                TopSalaryLocation(
                    location = locObj.getString("location"),
                    salary = locObj.getDouble("avgSalary"),
                    salaryFormatted = formatSalaryShort(locObj.getDouble("avgSalary")),
                    topJobTitle = locObj.getString("topJobTitle")
                )
            )
        }

        // Setup adapter
        val adapter = TopSalaryAdapter(locationList) { location ->
            // Klik item -> buka MainActivity dengan pre-fill location
            val intent = Intent(requireContext(), MainActivity::class.java).apply {
                putExtra("LOCATION", location.location)
            }
            startActivity(intent)
        }
        rvTopSalary.adapter = adapter
    }

    private fun formatSalary(salary: Double): String {
        val millions = salary / 1_000_000
        return if (millions >= 1) {
            String.format("%.1f jt/bln", millions)
        } else {
            val thousands = salary / 1_000
            String.format("%.0f rb/bln", thousands)
        }
    }

    private fun formatSalaryShort(salary: Double): String {
        val millions = salary / 1_000_000
        return if (millions >= 1) {
            String.format("%.1fM", millions)
        } else {
            val thousands = salary / 1_000
            String.format("%.0fK", thousands)
        }
    }
}