package com.example.salaryprediction

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.cardview.widget.CardView
import com.example.salaryprediction.api.*
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var etJobTitle: TextInputEditText
    private lateinit var etLocation: TextInputEditText
    private lateinit var actvCompany: AutoCompleteTextView
    private lateinit var btnPredict: MaterialButton
    private lateinit var tvResult: TextView
    private lateinit var cardResult: CardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Hubungkan view dengan id di XML
        etJobTitle = findViewById(R.id.etJobTitle)
        etLocation = findViewById(R.id.etLocation)
        actvCompany = findViewById(R.id.actvCompany)
        btnPredict = findViewById(R.id.btnPredict)
        tvResult = findViewById(R.id.tvResult)
        cardResult = findViewById(R.id.cardResult)

        // Setup dropdown untuk perusahaan
        setupCompanyDropdown()

        btnPredict.setOnClickListener {
            val jobTitle = etJobTitle.text.toString().trim()
            val location = etLocation.text.toString().trim()
            val companyText = actvCompany.text.toString().trim()

            // Jika dropdown masih default atau kosong, set company = null
            val company = if (companyText.isEmpty() || companyText == "-- Pilih Perusahaan --") {
                null
            } else {
                companyText
            }

            // Validasi input
            if (jobTitle.isEmpty()) {
                etJobTitle.error = "Job Title tidak boleh kosong"
                etJobTitle.requestFocus()
                Toast.makeText(this, "Job title wajib diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (location.isEmpty()) {
                etLocation.error = "Lokasi tidak boleh kosong"
                etLocation.requestFocus()
                Toast.makeText(this, "Lokasi wajib diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            callPredictApi(jobTitle, location, company)
        }
    }

    private fun setupCompanyDropdown() {
        // Daftar perusahaan berdasarkan dataset ASLI (Top 45 + Lainnya)
        // Total: 4,969 perusahaan unik dalam dataset
        // Data source: job_salary_mean__1_.csv
        val companies = arrayOf(
            "-- Pilih Perusahaan --",
            "PT. BFI FINANCE INDONESIA, Tbk",
            "PT. Gree Electric Appliances Indonesia",
            "Pengiklan Anonim",
            "PT Amartha Mikro Fintek (Jakarta)",
            "PT Indodana Multi Finance",
            "PT BCA Finance",
            "EUROMEDICA GROUP",
            "PT Aplikasi Karya Anak Bangsa (GO-JEK Indonesia)",
            "PT XL Axiata Tbk",
            "PT. Yakult Indonesia Persada",
            "SKINTIFIC",
            "Dexa Group",
            "PT Garuda Mitra Sejati",
            "PT. Master Kidz Indonesia",
            "PT Allo Fresh Indonesia",
            "PT Subaindo Cahaya Polintraco",
            "pt.anugerah megah lestari textile",
            "PT.X-motors Internasional Group",
            "RS Tria Dipa",
            "PT Telmark Integrasi Indonesia",
            "Hanwha Life Insurance Indonesia",
            "PT. JIMSHONEY INDONESIA PRATAMA",
            "PT RUTAN",
            "PT Planet Chemicals (Subsidiary of Planet Asia Singapore)",
            "CV. ARTHA LIMAS SENTOSA",
            "Nordson Advanced Technology Singapore",
            "PT SUMMIT TRAVEL GOODS",
            "PT Swapro International",
            "PT Berkat Indo-Opple Gemilang",
            "Ahava Prime Skincare",
            "PT Akarsa Garment Indonesia",
            "RGF HR Agent Indonesia",
            "Kawan Lama Group",
            "PT AXA Mandiri Financial Services",
            "PT Home Credit Indonesia",
            "PT BNI Life Insurance",
            "PT Finfleet Teknologi Indonesia",
            "PT. PERSOLKELLY Recruitment Indonesia",
            "PT SMART,Tbk",
            "PT Carlcare Service Ila",
            "CIPUTRA GROUP",
            "Siloam Hospitals Group (Tbk)",
            "PwC Indonesia",
            "PT Bank Danamon Indonesia, Tbk",
            "ASRI (a subsidiary of Agung Sedayu Group)",
            "Lainnya"
        )

        val adapter = ArrayAdapter(this, R.layout.dropdown_item, companies)
        actvCompany.setAdapter(adapter)

        // Set default value
        actvCompany.setText(companies[0], false)
    }

    private fun callPredictApi(jobTitle: String, location: String, company: String?) {
        // Tampilkan loading state
        btnPredict.isEnabled = false
        btnPredict.text = "Memproses..."
        tvResult.text = "⏳ Mengirim permintaan ke server..."
        cardResult.visibility = View.VISIBLE

        val request = SalaryRequest(
            job_title = jobTitle,
            location = location,
            company = company
        )

        val call = ApiClient.apiService.predictSalary(request)

        call.enqueue(object : Callback<SalaryResponse> {
            override fun onResponse(
                call: Call<SalaryResponse>,
                response: Response<SalaryResponse>
            ) {
                // Reset button state
                btnPredict.isEnabled = true
                btnPredict.text = "Prediksi Gaji Sekarang"

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.success && body.prediction != null) {
                        val p = body.prediction

                        // Format hasil dengan tampilan yang lebih menarik
                        val resultText = buildString {
                            append("📋 Informasi Posisi\n")
                            append("━━━━━━━━━━━━━━━━━━━━\n")
                            append("Posisi: $jobTitle\n")
                            append("Lokasi: $location\n")
                            if (company != null && company != "-- Pilih Perusahaan --") {
                                append("Perusahaan: $company\n")
                            }
                            append("\n")

                            append("💰 Estimasi Gaji\n")
                            append("━━━━━━━━━━━━━━━━━━━━\n")
                            append("${p.salary_formatted}\n")
                            append("\n")

                            append("📊 Rentang Gaji\n")
                            append("━━━━━━━━━━━━━━━━━━━━\n")
                            append("${p.range.formatted}\n")
                            append("\n")

                            append("ℹ️ Detail Analisis\n")
                            append("━━━━━━━━━━━━━━━━━━━━\n")
                            append("• Kategori: ${p.details.category}\n")
                            append("• Level: ${p.details.level}\n")
                            append("• Tier Lokasi: ${p.details.tier}")
                        }

                        tvResult.text = resultText
                        cardResult.visibility = View.VISIBLE

                        Toast.makeText(
                            this@MainActivity,
                            "✅ Prediksi berhasil!",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        tvResult.text = "❌ Gagal: Response kosong atau success=false\n\n" +
                                "Silakan coba lagi atau periksa koneksi server."
                        cardResult.visibility = View.VISIBLE

                        Toast.makeText(
                            this@MainActivity,
                            "Gagal memproses prediksi",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    tvResult.text = "❌ Error Server\n\n" +
                            "Kode: ${response.code()}\n" +
                            "Pesan: ${response.message()}\n\n" +
                            "Silakan periksa koneksi atau coba lagi nanti."
                    cardResult.visibility = View.VISIBLE

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
                btnPredict.text = "Prediksi Gaji Sekarang"

                tvResult.text = "❌ Koneksi Gagal\n\n" +
                        "Tidak dapat terhubung ke server.\n\n" +
                        "Error: ${t.localizedMessage}\n\n" +
                        "Tips:\n" +
                        "• Periksa koneksi internet Anda\n" +
                        "• Pastikan server sedang berjalan\n" +
                        "• Periksa URL API di konfigurasi"
                cardResult.visibility = View.VISIBLE

                Toast.makeText(
                    this@MainActivity,
                    "Tidak bisa konek ke server",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }
}