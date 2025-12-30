package com.example.salaryprediction.repository

import com.example.salaryprediction.models.PredictionHistory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class HistoryRepository private constructor() {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    companion object {
        private const val COLLECTION_USERS = "users"
        private const val COLLECTION_HISTORY = "prediction_history"
        private const val MAX_HISTORY_ITEMS = 50 // Batasi jumlah history

        @Volatile
        private var instance: HistoryRepository? = null

        fun getInstance(): HistoryRepository {
            return instance ?: synchronized(this) {
                instance ?: HistoryRepository().also { instance = it }
            }
        }
    }

    /**
     * Get current user ID
     */
    private fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    /**
     * Get reference to user's history collection
     */
    private fun getHistoryCollection() = getCurrentUserId()?.let { userId ->
        firestore.collection(COLLECTION_USERS)
            .document(userId)
            .collection(COLLECTION_HISTORY)
    }

    /**
     * Save prediction to history
     */
    suspend fun savePrediction(history: PredictionHistory): Result<String> {
        return try {
            val collection = getHistoryCollection()
                ?: return Result.failure(Exception("User not logged in"))

            val docRef = collection.add(history).await()

            // Cleanup old history jika melebihi limit
            cleanupOldHistory()

            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get all prediction history (realtime updates)
     */
    fun getHistoryFlow(): Flow<List<PredictionHistory>> = callbackFlow {
        val collection = getHistoryCollection()

        if (collection == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = collection
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(MAX_HISTORY_ITEMS.toLong())
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val historyList = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(PredictionHistory::class.java)
                } ?: emptyList()

                trySend(historyList)
            }

        awaitClose { listener.remove() }
    }

    /**
     * Get all prediction history (one-time fetch)
     */
    suspend fun getHistory(): Result<List<PredictionHistory>> {
        return try {
            val collection = getHistoryCollection()
                ?: return Result.failure(Exception("User not logged in"))

            val snapshot = collection
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(MAX_HISTORY_ITEMS.toLong())
                .get()
                .await()

            val historyList = snapshot.documents.mapNotNull { doc ->
                doc.toObject(PredictionHistory::class.java)
            }

            Result.success(historyList)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Delete a specific history item
     */
    suspend fun deleteHistory(historyId: String): Result<Unit> {
        return try {
            val collection = getHistoryCollection()
                ?: return Result.failure(Exception("User not logged in"))

            collection.document(historyId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Delete all history
     */
    suspend fun deleteAllHistory(): Result<Unit> {
        return try {
            val collection = getHistoryCollection()
                ?: return Result.failure(Exception("User not logged in"))

            val snapshot = collection.get().await()

            // Batch delete
            val batch = firestore.batch()
            snapshot.documents.forEach { doc ->
                batch.delete(doc.reference)
            }
            batch.commit().await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Cleanup old history items if exceeds limit
     */
    private suspend fun cleanupOldHistory() {
        try {
            val collection = getHistoryCollection() ?: return

            val snapshot = collection
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()

            if (snapshot.size() > MAX_HISTORY_ITEMS) {
                val toDelete = snapshot.documents.drop(MAX_HISTORY_ITEMS)
                val batch = firestore.batch()
                toDelete.forEach { doc ->
                    batch.delete(doc.reference)
                }
                batch.commit().await()
            }
        } catch (e: Exception) {
            // Ignore cleanup errors
        }
    }

    /**
     * Check if history exists for a specific job+location combo
     */
    suspend fun historyExists(jobTitle: String, location: String): Boolean {
        return try {
            val collection = getHistoryCollection() ?: return false

            val snapshot = collection
                .whereEqualTo("jobTitle", jobTitle)
                .whereEqualTo("location", location)
                .limit(1)
                .get()
                .await()

            !snapshot.isEmpty
        } catch (e: Exception) {
            false
        }
    }
}