package com.example.salaryprediction.models

import com.google.gson.annotations.SerializedName

/**
 * Response dari NewsAPI
 */
data class NewsResponse(
    @SerializedName("status")
    val status: String,

    @SerializedName("totalResults")
    val totalResults: Int,

    @SerializedName("articles")
    val articles: List<NewsArticle>,

    // Untuk error response dari NewsAPI
    @SerializedName("code")
    val code: String? = null,

    @SerializedName("message")
    val message: String? = null
)

/**
 * Data class untuk News Article dari NewsAPI
 */
data class NewsArticle(
    @SerializedName("source")
    val source: NewsSource,

    @SerializedName("author")
    val author: String?,

    @SerializedName("title")
    val title: String,

    @SerializedName("description")
    val description: String?,

    @SerializedName("url")
    val url: String,

    @SerializedName("urlToImage")
    val urlToImage: String?,

    @SerializedName("publishedAt")
    val publishedAt: String,

    @SerializedName("content")
    val content: String?
)

/**
 * Source/Publisher dari artikel
 */
data class NewsSource(
    @SerializedName("id")
    val id: String?,

    @SerializedName("name")
    val name: String
)