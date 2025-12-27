package com.example.salaryprediction.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.salaryprediction.R
import com.example.salaryprediction.models.TopSalaryLocation
import com.google.android.material.button.MaterialButton

class TopSalaryAdapter(
    private val items: List<TopSalaryLocation>,
    private val onItemClick: (TopSalaryLocation) -> Unit
) : RecyclerView.Adapter<TopSalaryAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvJobTitle: TextView = itemView.findViewById(R.id.tvJobTitle)
        val tvLocation: TextView = itemView.findViewById(R.id.tvLocation)
        val tvSalary: TextView = itemView.findViewById(R.id.tvSalary)
        val btnViewMore: MaterialButton = itemView.findViewById(R.id.btnViewMore)

        fun bind(item: TopSalaryLocation) {
            tvJobTitle.text = item.topJobTitle
            tvLocation.text = item.location
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
            .inflate(R.layout.item_top_salary, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}