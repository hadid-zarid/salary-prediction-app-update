package com.example.salaryprediction.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.salaryprediction.R
import com.example.salaryprediction.models.RecommendedJob
import com.google.android.material.button.MaterialButton

class RecommendedJobAdapter(
    private val items: List<RecommendedJob>,
    private val onItemClick: (RecommendedJob) -> Unit
) : RecyclerView.Adapter<RecommendedJobAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvJobTitle: TextView = itemView.findViewById(R.id.tvJobTitle)
        val tvCompanyLocation: TextView = itemView.findViewById(R.id.tvCompanyLocation)
        val tvSalary: TextView = itemView.findViewById(R.id.tvSalary)
        val btnViewMore: MaterialButton = itemView.findViewById(R.id.btnViewMore)

        fun bind(item: RecommendedJob) {
            tvJobTitle.text = item.title
            tvCompanyLocation.text = "${item.company} • ${item.location}"
            tvSalary.text = item.salaryFormatted

            btnViewMore.setOnClickListener {
                onItemClick(item)
            }

            itemView.setOnClickListener {
                onItemClick(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recommended_job, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}