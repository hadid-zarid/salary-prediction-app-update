package com.example.salaryprediction.models

/**
 * Data class untuk Menu Card di Home Screen
 */
data class MenuCard(
    val id: Int,
    val title: String,
    val description: String,
    val icon: Int, // Resource ID untuk icon
    val backgroundColor: Int // Resource ID untuk color
)
