package com.example.salaryprediction.models

data class TopSalaryLocation(
    val location: String,
    val salary: Double,
    val salaryFormatted: String,
    val topJobTitle: String = "Various Jobs"
)