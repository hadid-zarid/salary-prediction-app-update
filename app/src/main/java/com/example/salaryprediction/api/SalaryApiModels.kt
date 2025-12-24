package com.example.salaryprediction.api

// Body request ke Flask
data class SalaryRequest(
    val job_title: String,
    val location: String,
    val company: String? // boleh null
)

// Response dari Flask
data class SalaryResponse(
    val success: Boolean,
    val prediction: Prediction?
)

data class Prediction(
    val salary: Double,
    val salary_formatted: String,
    val range: Range,
    val details: Details
)

data class Range(
    val lower: Double,
    val upper: Double,
    val formatted: String
)

data class Details(
    val category: String,
    val level: Int,
    val tier: String
)
