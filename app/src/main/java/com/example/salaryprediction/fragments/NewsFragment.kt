package com.example.salaryprediction.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.salaryprediction.R
import com.example.salaryprediction.adapters.NewsAdapter
import com.example.salaryprediction.api.NewsApiService
import com.google.android.material.progressindicator.CircularProgressIndicator
import kotlinx.coroutines.launch

class NewsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var newsAdapter: NewsAdapter
    private lateinit var progressIndicator: CircularProgressIndicator
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var emptyStateView: View

    private val newsApiService = NewsApiService.create()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_news, container, false)

        initViews(view)
        setupRecyclerView()
        loadNews()

        return view
    }

    private fun initViews(view: View) {
        recyclerView = view.findViewById(R.id.newsRecyclerView)
        progressIndicator = view.findViewById(R.id.progressIndicator)
        swipeRefresh = view.findViewById(R.id.swipeRefresh)
        emptyStateView = view.findViewById(R.id.emptyStateView)

        swipeRefresh.setOnRefreshListener {
            loadNews()
        }
    }

    private fun setupRecyclerView() {
        newsAdapter = NewsAdapter()
        recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = newsAdapter
        }
    }

    private fun loadNews() {
        setLoading(true)

        lifecycleScope.launch {
            try {
                Log.d("NewsFragment", "Loading salary-related news...")

                // ========================================
                // PILIH SALAH SATU OPSI DI BAWAH:
                // ========================================

                // OPSI 1: Business News (Rekomendasi) ✅
                val response = newsApiService.getTopHeadlines(
                    country = "us",
                    category = "business", // Berita bisnis, ekonomi, gaji
                    apiKey = NewsApiService.API_KEY
                )

                // OPSI 2: Search dengan Keyword Salary/Jobs (Lebih Spesifik)
                // val response = newsApiService.searchNews(
                //     query = "salary OR jobs OR employment OR career",
                //     language = "en",
                //     apiKey = NewsApiService.API_KEY
                // )

                // OPSI 3: Tech Salary Specific
                // val response = newsApiService.searchNews(
                //     query = "tech salary OR software engineer salary OR IT compensation",
                //     language = "en",
                //     apiKey = NewsApiService.API_KEY
                // )

                // OPSI 4: Job Market Trends
                // val response = newsApiService.searchNews(
                //     query = "job market OR hiring trends OR employment outlook",
                //     language = "en",
                //     apiKey = NewsApiService.API_KEY
                // )

                // OPSI 5: Indonesia Salary News (konten terbatas)
                // val response = newsApiService.searchNews(
                //     query = "Indonesia salary OR gaji Indonesia",
                //     language = "en",
                //     apiKey = NewsApiService.API_KEY
                // )

                Log.d("NewsFragment", "Response: ${response.status}, total=${response.totalResults}")

                if (response.status == "ok" && response.articles.isNotEmpty()) {
                    Log.d("NewsFragment", "✅ Loaded ${response.articles.size} articles")
                    newsAdapter.updateData(response.articles)
                    emptyStateView.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE

                    Toast.makeText(
                        requireContext(),
                        "${response.articles.size} berita dimuat",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Log.w("NewsFragment", "⚠️ No articles found")
                    showEmptyState()
                }

            } catch (e: Exception) {
                Log.e("NewsFragment", "❌ Error loading news", e)
                handleError(e)
            } finally {
                setLoading(false)
                swipeRefresh.isRefreshing = false
            }
        }
    }

    private fun showEmptyState() {
        emptyStateView.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        Toast.makeText(
            requireContext(),
            "Tidak ada berita tersedia",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun handleError(exception: Exception) {
        val message = when {
            exception.message?.contains("Unable to resolve host") == true ->
                "Tidak ada koneksi internet"
            exception.message?.contains("apiKey") == true ||
                    exception.message?.contains("401") == true ->
                "API Key tidak valid"
            exception.message?.contains("426") == true ->
                "API Key gratis hanya untuk localhost"
            else ->
                "Gagal memuat berita: ${exception.message}"
        }

        Log.e("NewsFragment", "Error: $message")
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
        showEmptyState()
    }

    private fun setLoading(isLoading: Boolean) {
        progressIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
}