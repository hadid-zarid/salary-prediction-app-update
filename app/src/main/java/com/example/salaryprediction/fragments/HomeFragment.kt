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
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.text.NumberFormat
import java.util.Locale

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
            // Baca file JSON dari assets
            val jsonString = requireContext().assets
                .open("lookup_data.json")
                .bufferedReader()
                .use { it.readText() }

            val jsonObject = JSONObject(jsonString)

            // Load Trending Jobs (dari judul_mean - top 10 gaji tertinggi)
            loadTrendingJobs(jsonObject)

            // Load Top Salary Locations (dari lokasi_mean - top 10 lokasi gaji tertinggi)
            loadTopSalaryLocations(jsonObject)

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Error loading data", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadTrendingJobs(jsonObject: JSONObject) {
        val judulMean = jsonObject.getJSONObject("judul_mean")
        val jobList = mutableListOf<TrendingJob>()

        // Iterate dan ambil semua job titles
        val keys = judulMean.keys()
        while (keys.hasNext()) {
            val title = keys.next()
            val salary = judulMean.getDouble(title)
            jobList.add(TrendingJob(title, salary, formatSalary(salary)))
        }

        // Sort descending by salary dan ambil top 10
        val top10Jobs = jobList
            .sortedByDescending { it.salary }
            .take(10)

        // Setup adapter
        val adapter = TrendingJobAdapter(top10Jobs) { job ->
            // Klik item -> buka MainActivity dengan pre-fill job title
            val intent = Intent(requireContext(), MainActivity::class.java).apply {
                putExtra("JOB_TITLE", job.title)
            }
            startActivity(intent)
        }
        rvTrendingJob.adapter = adapter
    }

    private fun loadTopSalaryLocations(jsonObject: JSONObject) {
        val lokasiMean = jsonObject.getJSONObject("lokasi_mean")
        val locationList = mutableListOf<TopSalaryLocation>()

        // Iterate dan ambil semua locations
        val keys = lokasiMean.keys()
        while (keys.hasNext()) {
            val location = keys.next()
            val salary = lokasiMean.getDouble(location)
            locationList.add(
                TopSalaryLocation(
                    location = location,
                    salary = salary,
                    salaryFormatted = formatSalaryShort(salary),
                    topJobTitle = "Various Jobs" // Bisa di-customize nanti
                )
            )
        }

        // Sort descending by salary dan ambil top 10
        val top10Locations = locationList
            .sortedByDescending { it.salary }
            .take(10)

        // Setup adapter
        val adapter = TopSalaryAdapter(top10Locations) { location ->
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