package com.example.salaryprediction.auth

import com.example.salaryprediction.models.User
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Repository untuk mengelola Authentication dan Firestore operations
 * Menggunakan Singleton pattern
 */
class AuthRepository private constructor() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val usersCollection = firestore.collection("users")

    companion object {
        @Volatile
        private var instance: AuthRepository? = null

        fun getInstance(): AuthRepository {
            return instance ?: synchronized(this) {
                instance ?: AuthRepository().also { instance = it }
            }
        }
    }

    /**
     * Get current logged in user
     */
    fun getCurrentUser(): FirebaseUser? = auth.currentUser

    /**
     * Check if user is logged in
     */
    fun isUserLoggedIn(): Boolean = auth.currentUser != null

    /**
     * Register user baru
     */
    suspend fun register(
        email: String,
        password: String,
        displayName: String
    ): Result<User> {
        return try {
            // Create user dengan Firebase Authentication
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user 
                ?: return Result.failure(Exception("Failed to create user"))

            // Create user document di Firestore
            val user = User(
                uid = firebaseUser.uid,
                email = email,
                displayName = displayName,
                role = "user", // Default role
                createdAt = Timestamp.now()
            )

            // Save to Firestore
            usersCollection.document(firebaseUser.uid)
                .set(user.toMap())
                .await()

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Login user
     */
    suspend fun login(email: String, password: String): Result<User> {
        return try {
            // Sign in dengan Firebase Auth
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user 
                ?: return Result.failure(Exception("Login failed"))

            // Get user data dari Firestore
            val userDoc = usersCollection.document(firebaseUser.uid).get().await()
            
            if (userDoc.exists()) {
                val userData = userDoc.data ?: emptyMap()
                val user = User.fromMap(firebaseUser.uid, userData)
                Result.success(user)
            } else {
                Result.failure(Exception("User data not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Logout user
     */
    fun logout() {
        auth.signOut()
    }

    /**
     * Get user data dari Firestore by UID
     */
    suspend fun getUserData(uid: String): Result<User> {
        return try {
            val userDoc = usersCollection.document(uid).get().await()
            
            if (userDoc.exists()) {
                val userData = userDoc.data ?: emptyMap()
                val user = User.fromMap(uid, userData)
                Result.success(user)
            } else {
                Result.failure(Exception("User not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get current user data dari Firestore
     */
    suspend fun getCurrentUserData(): Result<User> {
        val currentUser = auth.currentUser 
            ?: return Result.failure(Exception("No user logged in"))
        
        return getUserData(currentUser.uid)
    }

    /**
     * Get semua users (hanya untuk admin)
     */
    suspend fun getAllUsers(): Result<List<User>> {
        return try {
            val snapshot = usersCollection.get().await()
            val users = snapshot.documents.mapNotNull { doc ->
                val data = doc.data ?: return@mapNotNull null
                User.fromMap(doc.id, data)
            }
            Result.success(users)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Update user profile
     */
    suspend fun updateUserProfile(
        uid: String,
        displayName: String
    ): Result<Unit> {
        return try {
            usersCollection.document(uid)
                .update("displayName", displayName)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Delete user (admin only)
     */
    suspend fun deleteUser(uid: String): Result<Unit> {
        return try {
            // Delete dari Firestore
            usersCollection.document(uid).delete().await()
            // Note: Untuk delete dari Authentication, butuh Firebase Admin SDK
            // atau Cloud Function
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
