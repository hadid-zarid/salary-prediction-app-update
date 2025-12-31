package com.example.salaryprediction.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Window
import android.widget.TextView
import com.example.salaryprediction.R
import com.example.salaryprediction.models.PredictionHistory
import com.google.android.material.button.MaterialButton

class HistoryDetailDialog(
    context: Context,
    private val history: PredictionHistory
) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Remove default title
        requestWindowFeature(Window.FEATURE_NO_TITLE)

        setContentView(R.layout.dialog_history_detail)

        // Make dialog background transparent
        window?.setBackgroundDrawableResource(android.R.color.transparent)

        // Set width to match parent with margin
        window?.setLayout(
            (context.resources.displayMetrics.widthPixels * 0.9).toInt(),
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        )

        // Initialize views
        setupViews()
    }

    private fun setupViews() {
        // Job Title
        findViewById<TextView>(R.id.tvJobTitle).text = history.jobTitle

        // Location
        findViewById<TextView>(R.id.tvLocation).text = history.location

        // Salary
        findViewById<TextView>(R.id.tvSalary).text = history.salaryFormatted

        // Range
        findViewById<TextView>(R.id.tvRange).text = history.rangeFormatted.ifEmpty {
            formatRange(history.salaryMin, history.salaryMax)
        }

        // Category
        findViewById<TextView>(R.id.tvCategory).text = history.category.ifEmpty { "-" }

        // Level
        findViewById<TextView>(R.id.tvLevel).text = formatLevel(history.level)

        // Tier
        findViewById<TextView>(R.id.tvTier).text = formatTier(history.tier)

        // Date
        findViewById<TextView>(R.id.tvDate).text = history.getFormattedDate()

        // Timestamp in header
        findViewById<TextView>(R.id.tvTimestamp).text = history.getRelativeTime()

        // Close button
        findViewById<MaterialButton>(R.id.btnClose).setOnClickListener {
            dismiss()
        }
    }

    private fun formatRange(min: Double, max: Double): String {
        return if (min > 0 && max > 0) {
            "Rp ${formatNumber(min)} - Rp ${formatNumber(max)}"
        } else {
            "-"
        }
    }

    private fun formatNumber(value: Double): String {
        return when {
            value >= 1_000_000 -> String.format("%,.0f", value)
            value >= 1_000 -> String.format("%,.0f", value)
            else -> value.toString()
        }
    }

    private fun formatLevel(level: String): String {
        return when (level) {
            "1" -> "Entry Level"
            "2" -> "Junior"
            "3" -> "Mid-Level"
            "4" -> "Senior"
            "5" -> "Lead/Manager"
            "6" -> "Executive"
            else -> if (level.isEmpty()) "-" else "Level $level"
        }
    }

    private fun formatTier(tier: String): String {
        return when {
            tier.contains("1A") -> "Tier 1A"
            tier.contains("1B") -> "Tier 1B"
            tier.contains("2A") -> "Tier 2A"
            tier.contains("2B") -> "Tier 2B"
            tier.contains("3") -> "Tier 3"
            tier.isEmpty() -> "-"
            else -> tier.replace("_", " ")
        }
    }
}