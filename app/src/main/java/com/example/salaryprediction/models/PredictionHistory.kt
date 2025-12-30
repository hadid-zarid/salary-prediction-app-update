package com.example.salaryprediction.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

data class PredictionHistory(
    @DocumentId
    val id: String = "",
    val jobTitle: String = "",
    val location: String = "",
    val predictedSalary: Double = 0.0,
    val salaryFormatted: String = "",
    val salaryMin: Double = 0.0,
    val salaryMax: Double = 0.0,
    val rangeFormatted: String = "",
    val category: String = "",
    val level: String = "",
    val tier: String = "",
    @ServerTimestamp
    val timestamp: Timestamp? = null
) {
    // Empty constructor untuk Firestore
    constructor() : this("", "", "", 0.0, "", 0.0, 0.0, "", "", "", "")

    /**
     * Format timestamp ke string yang readable
     */
    fun getFormattedDate(): String {
        return timestamp?.toDate()?.let { date ->
            val formatter = java.text.SimpleDateFormat("dd MMM yyyy, HH:mm", java.util.Locale("id", "ID"))
            formatter.format(date)
        } ?: "-"
    }

    /**
     * Format timestamp ke relative time (misal: "2 jam lalu")
     */
    fun getRelativeTime(): String {
        val now = System.currentTimeMillis()
        val time = timestamp?.toDate()?.time ?: return "-"
        val diff = now - time

        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24

        return when {
            days > 0 -> "$days hari lalu"
            hours > 0 -> "$hours jam lalu"
            minutes > 0 -> "$minutes menit lalu"
            else -> "Baru saja"
        }
    }
}