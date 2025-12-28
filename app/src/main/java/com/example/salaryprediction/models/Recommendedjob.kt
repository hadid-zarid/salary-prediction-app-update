package com.example.salaryprediction.models

data class RecommendedJob(
    val title: String,
    val company: String,
    val location: String,
    val salary: Double,
    val salaryFormatted: String
)