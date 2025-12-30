package com.example.salaryprediction.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.salaryprediction.R
import com.example.salaryprediction.models.PredictionHistory
import com.google.android.material.card.MaterialCardView

class HistoryAdapter(
    private val onItemClick: (PredictionHistory) -> Unit,
    private val onDeleteClick: (PredictionHistory) -> Unit
) : ListAdapter<PredictionHistory, HistoryAdapter.HistoryViewHolder>(HistoryDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardView: MaterialCardView = itemView.findViewById(R.id.cardHistory)
        private val tvJobTitle: TextView = itemView.findViewById(R.id.tvJobTitle)
        private val tvLocation: TextView = itemView.findViewById(R.id.tvLocation)
        private val tvSalary: TextView = itemView.findViewById(R.id.tvSalary)
        private val tvRange: TextView = itemView.findViewById(R.id.tvRange)
        private val tvTimestamp: TextView = itemView.findViewById(R.id.tvTimestamp)
        private val tvCategory: TextView = itemView.findViewById(R.id.tvCategory)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)

        fun bind(history: PredictionHistory) {
            tvJobTitle.text = history.jobTitle
            tvLocation.text = history.location
            tvSalary.text = history.salaryFormatted
            tvRange.text = history.rangeFormatted
            tvTimestamp.text = history.getRelativeTime()
            tvCategory.text = history.category

            // Click listeners
            cardView.setOnClickListener {
                onItemClick(history)
            }

            btnDelete.setOnClickListener {
                onDeleteClick(history)
            }
        }
    }

    class HistoryDiffCallback : DiffUtil.ItemCallback<PredictionHistory>() {
        override fun areItemsTheSame(oldItem: PredictionHistory, newItem: PredictionHistory): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: PredictionHistory, newItem: PredictionHistory): Boolean {
            return oldItem == newItem
        }
    }
}