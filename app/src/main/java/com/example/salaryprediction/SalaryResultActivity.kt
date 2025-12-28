package com.example.salaryprediction

import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.salaryprediction.adapters.RecommendedJobAdapter
import com.example.salaryprediction.models.RecommendedJob
import com.google.android.material.button.MaterialButton
import org.json.JSONObject
import java.text.NumberFormat
import java.util.Locale

class SalaryResultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_salary_result)

        // Get data dari intent
        val predictedSalary = intent.getDoubleExtra("PREDICTED_SALARY", 0.0)
        val jobTitle = intent.getStringExtra("JOB_TITLE") ?: ""
        val location = intent.getStringExtra("LOCATION") ?: ""
        val salaryFormatted = intent.getStringExtra("SALARY_FORMATTED") ?: formatCurrency(predictedSalary)
        val rangeFormatted = intent.getStringExtra("RANGE_FORMATTED") ?: ""
        val category = intent.getStringExtra("CATEGORY") ?: ""
        val level = intent.getStringExtra("LEVEL") ?: ""
        val tier = intent.getStringExtra("TIER") ?: ""

        // Set views
        findViewById<TextView>(R.id.tvSubtitle).text = "Detail Gaji untuk $jobTitle"
        findViewById<TextView>(R.id.tvPosisi).text = jobTitle
        findViewById<TextView>(R.id.tvLokasi).text = location
        findViewById<TextView>(R.id.tvEstimasiGaji).text = salaryFormatted
        findViewById<TextView>(R.id.tvRentangGaji).text = rangeFormatted
        findViewById<TextView>(R.id.tvKategori).text = category
        findViewById<TextView>(R.id.tvLevel).text = level
        findViewById<TextView>(R.id.tvTierLokasi).text = tier

        // Back button
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        // Finish button
        findViewById<MaterialButton>(R.id.btnFinish).setOnClickListener {
            finish()
        }

        // Load recommended jobs
        loadRecommendedJobs()
    }

    private fun loadRecommendedJobs() {
        try {
            val jsonString = assets
                .open("home_data.json")
                .bufferedReader()
                .use { it.readText() }

            val jsonObject = JSONObject(jsonString)
            val locationsArray = jsonObject.getJSONArray("top_salary_locations")

            val recommendedJobs = mutableListOf<RecommendedJob>()

            for (i in 0 until minOf(5, locationsArray.length())) {
                val locObj = locationsArray.getJSONObject(i)
                recommendedJobs.add(
                    RecommendedJob(
                        title = locObj.getString("topJobTitle"),
                        company = locObj.getString("company"),
                        location = locObj.getString("location"),
                        salary = locObj.getDouble("topJobSalary"),
                        salaryFormatted = formatSalaryShort(locObj.getDouble("topJobSalary"))
                    )
                )
            }

            val rvRecommendedJobs = findViewById<RecyclerView>(R.id.rvRecommendedJobs)
            rvRecommendedJobs.layoutManager = LinearLayoutManager(this)
            rvRecommendedJobs.adapter = RecommendedJobAdapter(recommendedJobs) { job ->
                Toast.makeText(this, "Selected: ${job.title}", Toast.LENGTH_SHORT).show()
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun formatCurrency(amount: Double): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        return formatter.format(amount).replace("Rp", "Rp ")
    }

    private fun formatSalaryShort(salary: Double): String {
        val millions = salary / 1_000_000
        return if (millions >= 1) {
            String.format("%.0fM", millions)
        } else {
            val thousands = salary / 1_000
            String.format("%.0fK", thousands)
        }
    }
}