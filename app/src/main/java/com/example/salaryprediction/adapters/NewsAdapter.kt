package com.example.salaryprediction.adapters

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.salaryprediction.R
import com.example.salaryprediction.models.NewsArticle
import java.text.SimpleDateFormat
import java.util.*

class NewsAdapter(
    private var articles: List<NewsArticle> = emptyList()
) : RecyclerView.Adapter<NewsAdapter.NewsViewHolder>() {

    fun updateData(newArticles: List<NewsArticle>) {
        articles = newArticles
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_news, parent, false)
        return NewsViewHolder(view)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        holder.bind(articles[position])
    }

    override fun getItemCount(): Int = articles.size

    inner class NewsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.newsImage)
        private val titleText: TextView = itemView.findViewById(R.id.newsTitle)
        private val descriptionText: TextView = itemView.findViewById(R.id.newsDescription)
        private val sourceText: TextView = itemView.findViewById(R.id.newsSource)
        private val dateText: TextView = itemView.findViewById(R.id.newsDate)

        fun bind(article: NewsArticle) {
            titleText.text = article.title
            descriptionText.text = article.description ?: "Tidak ada deskripsi"
            sourceText.text = article.source.name
            
            // Format tanggal
            try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
                val outputFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
                val date = inputFormat.parse(article.publishedAt)
                dateText.text = date?.let { outputFormat.format(it) } ?: article.publishedAt
            } catch (e: Exception) {
                dateText.text = article.publishedAt
            }

            // Load image dengan Glide
            if (!article.urlToImage.isNullOrEmpty()) {
                Glide.with(itemView.context)
                    .load(article.urlToImage)
                    .placeholder(R.drawable.ic_news_placeholder)
                    .error(R.drawable.ic_news_placeholder)
                    .centerCrop()
                    .into(imageView)
            } else {
                imageView.setImageResource(R.drawable.ic_news_placeholder)
            }

            // Click listener - buka browser
            itemView.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(article.url))
                itemView.context.startActivity(intent)
            }
        }
    }
}
