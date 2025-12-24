package com.example.salaryprediction.api

import com.example.salaryprediction.models.NewsResponse
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

/**
 * News API Service untuk Salary Prediction App
 * Fokus: Business, Jobs, Salary, Career news
 */
interface NewsApiService {

    /**
     * Get top headlines by category
     * Categories: business, technology, science, health, entertainment, sports
     */
    @GET("top-headlines")
    suspend fun getTopHeadlines(
        @Query("country") country: String = "us",
        @Query("category") category: String = "business", // business untuk salary/jobs
        @Query("apiKey") apiKey: String
    ): NewsResponse

    /**
     * Search news dengan keyword spesifik
     */
    @GET("everything")
    suspend fun searchNews(
        @Query("q") query: String,
        @Query("language") language: String = "en",
        @Query("sortBy") sortBy: String = "publishedAt",
        @Query("pageSize") pageSize: Int = 20,
        @Query("apiKey") apiKey: String
    ): NewsResponse

    /**
     * Get salary-related news
     */
    @GET("everything")
    suspend fun getSalaryNews(
        @Query("q") query: String = "salary OR wages OR compensation OR employment",
        @Query("language") language: String = "en",
        @Query("sortBy") sortBy: String = "publishedAt",
        @Query("pageSize") pageSize: Int = 20,
        @Query("apiKey") apiKey: String
    ): NewsResponse

    /**
     * Get job market news
     */
    @GET("everything")
    suspend fun getJobMarketNews(
        @Query("q") query: String = "job market OR hiring trends OR employment outlook",
        @Query("language") language: String = "en",
        @Query("sortBy") sortBy: String = "publishedAt",
        @Query("pageSize") pageSize: Int = 20,
        @Query("apiKey") apiKey: String
    ): NewsResponse

    /**
     * Get tech salary news
     */
    @GET("everything")
    suspend fun getTechSalaryNews(
        @Query("q") query: String = "tech salary OR software engineer salary OR developer compensation",
        @Query("language") language: String = "en",
        @Query("sortBy") sortBy: String = "publishedAt",
        @Query("pageSize") pageSize: Int = 20,
        @Query("apiKey") apiKey: String
    ): NewsResponse

    /**
     * Get Indonesia salary news
     */
    @GET("everything")
    suspend fun getIndonesiaSalaryNews(
        @Query("q") query: String = "Indonesia salary OR gaji Indonesia OR pekerjaan Indonesia",
        @Query("language") language: String = "en",
        @Query("sortBy") sortBy: String = "publishedAt",
        @Query("pageSize") pageSize: Int = 20,
        @Query("apiKey") apiKey: String
    ): NewsResponse

    companion object {
        private const val BASE_URL = "https://newsapi.org/v2/"
        const val API_KEY = "19efdc3c9d3344219c0797eea3e03564"

        fun create(): NewsApiService {
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            return retrofit.create(NewsApiService::class.java)
        }
    }
}