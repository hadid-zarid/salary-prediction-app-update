package com.example.salaryprediction.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {

    // GANTI baseUrl sesuai lingkungan kamu

    // Kalau pakai EMULATOR Android:
    // Flask di laptop: http://localhost:5000
    // Base URL di Android:
    // http://10.0.2.2:5000/
    //
    // Kalau pakai HP FISIK:
    // - Cari IP laptop kamu, misal: 192.168.0.10
    // - Base URL: "http://192.168.0.10:5000/"

    private const val BASE_URL = "https://myreport12-salary-api.hf.space/"

    val apiService: SalaryApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SalaryApiService::class.java)
    }
}
