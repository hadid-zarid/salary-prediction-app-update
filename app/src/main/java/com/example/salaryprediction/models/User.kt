package com.example.salaryprediction.models

import com.google.firebase.Timestamp

class User(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val role: String = "user",
    val createdAt: Timestamp? = null
) {

    fun toMap(): Map<String, Any?> {
        return hashMapOf(
            "email" to email,
            "displayName" to displayName,
            "role" to role,
            "createdAt" to (createdAt ?: Timestamp.now())
        )
    }

    fun isAdmin(): Boolean = role == "admin"

    companion object {
        fun fromMap(uid: String, map: Map<String, Any>): User {
            return User(
                uid = uid,
                email = map["email"] as? String ?: "",
                displayName = map["displayName"] as? String ?: "",
                role = map["role"] as? String ?: "user",
                createdAt = map["createdAt"] as? Timestamp
            )
        }
    }
}
