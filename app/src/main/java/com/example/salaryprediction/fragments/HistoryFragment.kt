package com.example.salaryprediction.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.salaryprediction.MainActivity
import com.example.salaryprediction.R
import com.example.salaryprediction.adapters.HistoryAdapter
import com.example.salaryprediction.dialogs.HistoryDetailDialog
import com.example.salaryprediction.models.PredictionHistory
import com.example.salaryprediction.repository.HistoryRepository
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HistoryFragment : Fragment() {

    private lateinit var historyRepository: HistoryRepository
    private lateinit var historyAdapter: HistoryAdapter

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressIndicator: CircularProgressIndicator
    private lateinit var emptyStateView: LinearLayout
    private lateinit var tvHistoryCount: TextView
    private lateinit var fabClearAll: ExtendedFloatingActionButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_history, container, false)

        historyRepository = HistoryRepository.getInstance()

        initViews(view)
        setupRecyclerView()
        setupClickListeners()
        observeHistory()

        return view
    }

    private fun initViews(view: View) {
        recyclerView = view.findViewById(R.id.rvHistory)
        progressIndicator = view.findViewById(R.id.progressIndicator)
        emptyStateView = view.findViewById(R.id.emptyStateView)
        tvHistoryCount = view.findViewById(R.id.tvHistoryCount)
        fabClearAll = view.findViewById(R.id.fabClearAll)
    }

    private fun setupRecyclerView() {
        historyAdapter = HistoryAdapter(
            onItemClick = { history ->
                // Show detail dialog
                showDetailDialog(history)
            },
            onDeleteClick = { history ->
                showDeleteConfirmation(history)
            }
        )

        recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = historyAdapter
        }
    }

    private fun showDetailDialog(history: PredictionHistory) {
        val dialog = HistoryDetailDialog(requireContext(), history)
        dialog.show()
    }

    private fun setupClickListeners() {
        fabClearAll.setOnClickListener {
            showClearAllConfirmation()
        }
    }

    private fun observeHistory() {
        setLoading(true)

        lifecycleScope.launch {
            historyRepository.getHistoryFlow().collectLatest { historyList ->
                setLoading(false)
                updateUI(historyList)
            }
        }
    }

    private fun updateUI(historyList: List<PredictionHistory>) {
        if (historyList.isEmpty()) {
            showEmptyState()
        } else {
            hideEmptyState()
            historyAdapter.submitList(historyList)
            tvHistoryCount.text = "${historyList.size} prediksi tersimpan"
            fabClearAll.visibility = View.VISIBLE
        }
    }

    private fun showEmptyState() {
        emptyStateView.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        fabClearAll.visibility = View.GONE
        tvHistoryCount.text = "Belum ada history"
    }

    private fun hideEmptyState() {
        emptyStateView.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE
    }

    private fun setLoading(isLoading: Boolean) {
        progressIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showDeleteConfirmation(history: PredictionHistory) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Hapus History")
            .setMessage("Hapus prediksi untuk \"${history.jobTitle}\"?")
            .setPositiveButton("Hapus") { _, _ ->
                deleteHistory(history)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun deleteHistory(history: PredictionHistory) {
        lifecycleScope.launch {
            val result = historyRepository.deleteHistory(history.id)

            result.onSuccess {
                Toast.makeText(requireContext(), "History dihapus", Toast.LENGTH_SHORT).show()
            }.onFailure {
                Toast.makeText(requireContext(), "Gagal menghapus", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showClearAllConfirmation() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Hapus Semua History")
            .setMessage("Apakah Anda yakin ingin menghapus semua history prediksi?")
            .setPositiveButton("Hapus Semua") { _, _ ->
                clearAllHistory()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun clearAllHistory() {
        lifecycleScope.launch {
            setLoading(true)

            val result = historyRepository.deleteAllHistory()

            setLoading(false)

            result.onSuccess {
                Toast.makeText(requireContext(), "Semua history dihapus", Toast.LENGTH_SHORT).show()
            }.onFailure {
                Toast.makeText(requireContext(), "Gagal menghapus", Toast.LENGTH_SHORT).show()
            }
        }
    }
}