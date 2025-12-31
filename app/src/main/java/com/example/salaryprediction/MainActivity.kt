package com.example.salaryprediction

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import com.example.salaryprediction.api.*
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageButton
    private lateinit var etLocation: TextInputEditText
    private lateinit var etJobTitle: TextInputEditText
    private lateinit var btnPredict: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        setupClickListeners()
        setupBackNavigation()

        // Check for pre-filled data from intent
        handleIntentData()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        etLocation = findViewById(R.id.etLocation)
        etJobTitle = findViewById(R.id.etJobTitle)
        btnPredict = findViewById(R.id.btnPredict)
    }

    private fun handleIntentData() {
        // Pre-fill data jika ada dari HomeFragment
        intent.getStringExtra("JOB_TITLE")?.let {
            etJobTitle.setText(it)
        }
        intent.getStringExtra("LOCATION")?.let {
            etLocation.setText(it)
        }
    }

    /**
     * Setup back navigation untuk system back button
     */
    private fun setupBackNavigation() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                navigateBack()
            }
        })
    }

    /**
     * Navigate back ke halaman sebelumnya dengan animasi
     */
    private fun navigateBack() {
        finish()
        @Suppress("DEPRECATION")
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
    }

    private fun setupClickListeners() {
        // Back button click - kembali ke halaman sebelumnya
        btnBack.setOnClickListener {
            navigateBack()
        }

        btnPredict.setOnClickListener {
            val location = etLocation.text.toString().trim()
            val jobTitle = etJobTitle.text.toString().trim()

            // Validasi input
            if (location.isEmpty()) {
                etLocation.error = "Lokasi tidak boleh kosong"
                etLocation.requestFocus()
                Toast.makeText(this, "Lokasi wajib diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (jobTitle.isEmpty()) {
                etJobTitle.error = "Jabatan tidak boleh kosong"
                etJobTitle.requestFocus()
                Toast.makeText(this, "Jabatan wajib diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            callPredictApi(jobTitle, location)
        }
    }

    private fun callPredictApi(jobTitle: String, location: String) {
        // Tampilkan loading state
        btnPredict.isEnabled = false
        btnPredict.text = "Memproses..."

        val request = SalaryRequest(
            job_title = jobTitle,
            location = location,
            company = null
        )

        val call = ApiClient.apiService.predictSalary(request)

        call.enqueue(object : Callback<SalaryResponse> {
            override fun onResponse(
                call: Call<SalaryResponse>,
                response: Response<SalaryResponse>
            ) {
                // Reset button state
                btnPredict.isEnabled = true
                btnPredict.text = "MULAI"

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.success && body.prediction != null) {
                        val p = body.prediction

                        // Navigate to SalaryResultActivity
                        val intent = Intent(this@MainActivity, SalaryResultActivity::class.java).apply {
                            putExtra("PREDICTED_SALARY", p.salary)
                            putExtra("JOB_TITLE", jobTitle)
                            putExtra("LOCATION", location)
                            putExtra("SALARY_MIN", p.range.lower)
                            putExtra("SALARY_MAX", p.range.upper)
                            putExtra("SALARY_FORMATTED", p.salary_formatted)
                            putExtra("RANGE_FORMATTED", p.range.formatted)
                            putExtra("CATEGORY", p.details.category)
                            putExtra("LEVEL", p.details.level.toString())
                            putExtra("TIER", p.details.tier)
                        }
                        startActivity(intent)

                    } else {
                        Toast.makeText(
                            this@MainActivity,
                            "Gagal memproses prediksi",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        "Error: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<SalaryResponse>, t: Throwable) {
                // Reset button state
                btnPredict.isEnabled = true
                btnPredict.text = "MULAI"

                Toast.makeText(
                    this@MainActivity,
                    "Tidak bisa konek ke server: ${t.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }
}