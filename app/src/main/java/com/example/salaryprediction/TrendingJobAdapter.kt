package com.example.salaryprediction.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.salaryprediction.R
import com.example.salaryprediction.models.TrendingJob

class TrendingJobAdapter(
    private val items: List<TrendingJob>,
    private val onItemClick: (TrendingJob) -> Unit
) : RecyclerView.Adapter<TrendingJobAdapter.ViewHolder>() {

    // Warna-warna untuk card (sesuai desain)
    private val cardColors = listOf(
        "#FFD54F", // Kuning
        "#64B5F6", // Biru
        "#81C784", // Hijau
        "#FF8A65", // Orange
        "#BA68C8", // Ungu
        "#4DD0E1", // Cyan
        "#F06292", // Pink
        "#AED581"  // Light Green
    )

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardBackground: LinearLayout = itemView.findViewById(R.id.cardBackground)
        val tvJobTitle: TextView = itemView.findViewById(R.id.tvJobTitle)
        val tvSalary: TextView = itemView.findViewById(R.id.tvSalary)
        val tvRank: TextView = itemView.findViewById(R.id.tvRank)

        fun bind(item: TrendingJob, position: Int) {
            tvJobTitle.text = item.title
            tvSalary.text = item.salaryFormatted
            tvRank.text = "Top ${position + 1}"

            // Set warna background
            val colorIndex = position % cardColors.size
            cardBackground.setBackgroundColor(Color.parseColor(cardColors[colorIndex]))

            itemView.setOnClickListener {
                onItemClick(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_trending_job, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], position)
    }

    override fun getItemCount(): Int = items.size
}