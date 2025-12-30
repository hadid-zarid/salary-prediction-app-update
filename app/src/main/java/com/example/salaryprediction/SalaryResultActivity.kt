package com.example.salaryprediction

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.salaryprediction.adapters.RecommendedJobAdapter
import com.example.salaryprediction.models.RecommendedJob
import com.google.android.material.button.MaterialButton
import org.json.JSONObject
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs

class SalaryResultActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "SalaryResultActivity"
    }

    // Data dari intent
    private var predictedSalary: Double = 0.0
    private var inputJobTitle: String = ""
    private var inputLocation: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_salary_result)

        // Get data dari intent
        predictedSalary = intent.getDoubleExtra("PREDICTED_SALARY", 0.0)
        inputJobTitle = intent.getStringExtra("JOB_TITLE") ?: ""
        inputLocation = intent.getStringExtra("LOCATION") ?: ""

        val salaryFormatted = intent.getStringExtra("SALARY_FORMATTED") ?: formatCurrency(predictedSalary)
        val rangeFormatted = intent.getStringExtra("RANGE_FORMATTED") ?: ""
        val category = intent.getStringExtra("CATEGORY") ?: ""
        val level = intent.getStringExtra("LEVEL") ?: ""
        val tier = intent.getStringExtra("TIER") ?: ""

        // Set views
        findViewById<TextView>(R.id.tvSubtitle).text = "Detail Gaji untuk $inputJobTitle"
        findViewById<TextView>(R.id.tvPosisi).text = inputJobTitle
        findViewById<TextView>(R.id.tvLokasi).text = inputLocation
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

        // Load recommended jobs dengan algoritma baru
        loadRecommendedJobs()
    }

    /**
     * Load recommended jobs berdasarkan:
     * 1. Lokasi yang SAMA (prioritas utama)
     * 2. Kesamaan job title (keyword matching)
     * 3. Range gaji yang mirip
     */
    private fun loadRecommendedJobs() {
        try {
            Log.d(TAG, "Loading jobs database...")

            val jsonString = assets
                .open("jobs_database.json")
                .bufferedReader()
                .use { it.readText() }

            val jsonObject = JSONObject(jsonString)
            val jobsArray = jsonObject.getJSONArray("jobs")

            Log.d(TAG, "Total jobs in database: ${jobsArray.length()}")

            val allJobs = mutableListOf<JobData>()

            // Parse semua jobs dari JSON
            for (i in 0 until jobsArray.length()) {
                val jobObj = jobsArray.getJSONObject(i)
                allJobs.add(
                    JobData(
                        title = jobObj.getString("title"),
                        company = jobObj.getString("company"),
                        location = jobObj.getString("location"),
                        salary = jobObj.getDouble("salary")
                    )
                )
            }

            // Filter dan hitung relevance score
            val scoredJobs = allJobs
                .asSequence()
                .filter { !isSameJob(it) } // Exclude exact same job
                .map { job ->
                    val score = calculateRelevanceScore(job)
                    Pair(job, score)
                }
                .filter { it.second > 0 } // Hanya ambil yang punya score > 0
                .sortedByDescending { it.second } // Sort by score (highest first)
                .distinctBy { "${it.first.title}|${it.first.company}" } // Remove duplicates
                .take(5) // Ambil top 5
                .toList()

            Log.d(TAG, "Found ${scoredJobs.size} recommended jobs")

            // Convert ke RecommendedJob model
            val recommendedJobs = scoredJobs.map { (job, score) ->
                Log.d(TAG, "Recommended: ${job.title} @ ${job.company} (${job.location}) - Score: $score")
                RecommendedJob(
                    title = job.title,
                    company = job.company,
                    location = job.location,
                    salary = job.salary,
                    salaryFormatted = formatSalaryShort(job.salary)
                )
            }

            // Jika tidak ada yang cocok, fallback
            val finalRecommendations = if (recommendedJobs.isEmpty()) {
                Log.d(TAG, "No matches found, using fallback")
                loadFallbackRecommendations(allJobs)
            } else {
                recommendedJobs
            }

            // Setup RecyclerView
            val rvRecommendedJobs = findViewById<RecyclerView>(R.id.rvRecommendedJobs)
            rvRecommendedJobs.layoutManager = LinearLayoutManager(this)
            rvRecommendedJobs.adapter = RecommendedJobAdapter(finalRecommendations) { job ->
                // Klik item -> buka MainActivity dengan pre-fill data
                val intent = Intent(this, MainActivity::class.java).apply {
                    putExtra("JOB_TITLE", job.title)
                    putExtra("LOCATION", job.location)
                }
                startActivity(intent)
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error loading jobs", e)
            e.printStackTrace()
            loadFallbackFromHomeData()
        }
    }

    /**
     * Check if this is the exact same job user searched for
     */
    private fun isSameJob(job: JobData): Boolean {
        return job.title.equals(inputJobTitle, ignoreCase = true) &&
                job.location.equals(inputLocation, ignoreCase = true)
    }

    /**
     * Hitung relevance score berdasarkan:
     * - Location match (bobot 40%) - PRIORITAS!
     * - Title similarity (bobot 35%)
     * - Salary similarity (bobot 25%)
     */
    private fun calculateRelevanceScore(job: JobData): Double {
        var score = 0.0

        // 1. Location Match (max 40 points) - PRIORITAS UTAMA
        val locationScore = calculateLocationScore(job.location)
        score += locationScore * 40

        // 2. Title Similarity (max 35 points)
        val titleScore = calculateTitleSimilarity(inputJobTitle, job.title)
        score += titleScore * 35

        // 3. Salary Similarity (max 25 points)
        val salaryScore = calculateSalarySimilarity(predictedSalary, job.salary)
        score += salaryScore * 25

        return score
    }

    /**
     * Hitung location score
     */
    private fun calculateLocationScore(jobLocation: String): Double {
        val inputLower = inputLocation.lowercase().trim()
        val jobLower = jobLocation.lowercase().trim()

        // Exact match
        if (inputLower == jobLower) return 1.0

        // Contains check (partial match)
        if (jobLower.contains(inputLower) || inputLower.contains(jobLower)) {
            return 0.85
        }

        // City/region matching
        val inputCity = extractCity(inputLower)
        val jobCity = extractCity(jobLower)
        if (inputCity.isNotEmpty() && jobCity.isNotEmpty() && inputCity == jobCity) {
            return 0.7
        }

        // Same province/region
        if (isSameRegion(inputLower, jobLower)) {
            return 0.5
        }

        // Different location, but still consider for title/salary match
        return 0.1
    }

    /**
     * Extract city name dari location string
     */
    private fun extractCity(location: String): String {
        val cityKeywords = listOf(
            "jakarta", "surabaya", "bandung", "medan", "semarang",
            "makassar", "palembang", "tangerang", "depok", "bekasi",
            "bogor", "malang", "yogyakarta", "solo", "bali", "denpasar",
            "balikpapan", "samarinda", "pontianak", "banjarmasin",
            "cikarang", "karawang", "pekanbaru", "batam", "manado"
        )

        for (city in cityKeywords) {
            if (location.contains(city)) return city
        }
        return ""
    }

    /**
     * Check if two locations are in the same region
     */
    private fun isSameRegion(loc1: String, loc2: String): Boolean {
        val regions = mapOf(
            "jakarta" to listOf("jakarta", "dki", "jabodetabek"),
            "jawa barat" to listOf("bandung", "bekasi", "bogor", "depok", "cikarang", "karawang", "cikupa"),
            "jawa timur" to listOf("surabaya", "malang", "sidoarjo", "gresik"),
            "jawa tengah" to listOf("semarang", "solo", "surakarta"),
            "banten" to listOf("tangerang", "serang", "cilegon"),
            "bali" to listOf("bali", "denpasar", "badung", "gianyar", "ubud", "kuta")
        )

        for ((_, cities) in regions) {
            val loc1InRegion = cities.any { loc1.contains(it) }
            val loc2InRegion = cities.any { loc2.contains(it) }
            if (loc1InRegion && loc2InRegion) return true
        }
        return false
    }

    /**
     * Hitung kesamaan title berdasarkan keyword matching
     */
    private fun calculateTitleSimilarity(input: String, jobTitle: String): Double {
        val inputLower = input.lowercase().trim()
        val jobLower = jobTitle.lowercase().trim()

        // Exact match
        if (inputLower == jobLower) return 0.9

        // Contains check (one contains the other)
        if (jobLower.contains(inputLower) || inputLower.contains(jobLower)) {
            return 0.95
        }

        // Keyword matching
        val inputKeywords = extractKeywords(inputLower)
        val jobKeywords = extractKeywords(jobLower)

        if (inputKeywords.isEmpty() || jobKeywords.isEmpty()) return 0.0

        val commonKeywords = inputKeywords.intersect(jobKeywords.toSet())

        if (commonKeywords.isEmpty()) return 0.0

        // Jaccard similarity with bonus for more matches
        val unionSize = (inputKeywords + jobKeywords).toSet().size
        val similarity = commonKeywords.size.toDouble() / unionSize

        // Bonus if multiple keywords match
        val bonus = if (commonKeywords.size >= 2) 0.1 else 0.0

        return minOf(similarity + bonus, 1.0)
    }

    /**
     * Extract keywords dari job title
     */
    private fun extractKeywords(text: String): List<String> {
        val stopWords = setOf(
            "staff", "officer", "specialist", "associate", "assistant",
            "junior", "senior", "lead", "head", "chief", "executive",
            "intern", "trainee", "coordinator", "administrator",
            "dan", "and", "or", "the", "a", "an", "di", "untuk", "with",
            "pt", "cv", "tbk", "indonesia", "-", "&", "/", "(", ")",
            "penempatan", "area", "cabang", "wilayah"
        )

        return text.split(" ", "-", "_", "/", "&", "(", ")", ",")
            .map { it.trim().lowercase() }
            .filter { it.length > 2 && it !in stopWords }
    }

    /**
     * Hitung kesamaan gaji
     */
    private fun calculateSalarySimilarity(inputSalary: Double, jobSalary: Double): Double {
        if (inputSalary <= 0 || jobSalary <= 0) return 0.3

        val difference = abs(inputSalary - jobSalary)
        val avgSalary = (inputSalary + jobSalary) / 2
        val percentDiff = difference / avgSalary

        return when {
            percentDiff <= 0.1 -> 1.0    // Within 10%
            percentDiff <= 0.2 -> 0.85   // Within 20%
            percentDiff <= 0.3 -> 0.7    // Within 30%
            percentDiff <= 0.5 -> 0.5    // Within 50%
            percentDiff <= 0.7 -> 0.3    // Within 70%
            else -> 0.15
        }
    }

    /**
     * Fallback recommendations - prioritize same location, then similar salary
     */
    private fun loadFallbackRecommendations(allJobs: List<JobData>): List<RecommendedJob> {
        // First try: same location with similar salary
        val sameLocationJobs = allJobs
            .filter { it.location.equals(inputLocation, ignoreCase = true) }
            .filter { !isSameJob(it) }
            .sortedBy { abs(it.salary - predictedSalary) }
            .take(5)

        if (sameLocationJobs.isNotEmpty()) {
            return sameLocationJobs.map { job ->
                RecommendedJob(
                    title = job.title,
                    company = job.company,
                    location = job.location,
                    salary = job.salary,
                    salaryFormatted = formatSalaryShort(job.salary)
                )
            }
        }

        // Second try: similar salary anywhere
        val similarSalaryJobs = allJobs
            .filter { !isSameJob(it) }
            .sortedBy { abs(it.salary - predictedSalary) }
            .distinctBy { "${it.title}|${it.company}" }
            .take(5)

        return similarSalaryJobs.map { job ->
            RecommendedJob(
                title = job.title,
                company = job.company,
                location = job.location,
                salary = job.salary,
                salaryFormatted = formatSalaryShort(job.salary)
            )
        }
    }

    /**
     * Fallback ke home_data.json (implementasi lama)
     */
    private fun loadFallbackFromHomeData() {
        try {
            val jsonString = assets
                .open("home_data.json")
                .bufferedReader()
                .use { it.readText() }

            val jsonObject = JSONObject(jsonString)
            val locationsArray = jsonObject.getJSONArray("top_salary_locations")

            val recommendations = mutableListOf<RecommendedJob>()

            for (i in 0 until minOf(5, locationsArray.length())) {
                val locObj = locationsArray.getJSONObject(i)
                recommendations.add(
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
            rvRecommendedJobs.adapter = RecommendedJobAdapter(recommendations) { job ->
                val intent = Intent(this, MainActivity::class.java).apply {
                    putExtra("JOB_TITLE", job.title)
                    putExtra("LOCATION", job.location)
                }
                startActivity(intent)
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error loading fallback data", e)
        }
    }

    private fun formatCurrency(amount: Double): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        return formatter.format(amount).replace("Rp", "Rp ")
    }

    private fun formatSalaryShort(salary: Double): String {
        val millions = salary / 1_000_000
        return if (millions >= 1) {
            String.format("Rp %.1f jt", millions)
        } else {
            val thousands = salary / 1_000
            String.format("Rp %.0f rb", thousands)
        }
    }

    /**
     * Data class untuk job dari JSON
     */
    private data class JobData(
        val title: String,
        val company: String,
        val location: String,
        val salary: Double
    )
}