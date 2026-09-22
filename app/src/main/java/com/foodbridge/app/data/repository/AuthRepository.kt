package com.foodbridge.app.data.repository

import com.foodbridge.app.data.model.User
import com.foodbridge.app.data.model.UserRole
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/** FR-1: registration & login via Firebase Authentication, profile stored in Firestore. */
interface AuthRepository {
    suspend fun register(name: String, email: String, password: String, role: UserRole): Result<User>
    suspend fun login(email: String, password: String): Result<User>
    fun currentUserId(): String?
    suspend fun fetchProfile(userId: String): Result<User>
    fun logout()
}

class FirebaseAuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : AuthRepository {

    override suspend fun register(
        name: String,
        email: String,
        password: String,
        role: UserRole
    ): Result<User> = runCatching {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        val uid = result.user?.uid ?: error("Registration failed: no user id returned")
        val user = User(userId = uid, name = name, email = email, role = role)
        firestore.collection("users").document(uid).set(user).await()
        user
    }

    override suspend fun login(email: String, password: String): Result<User> = runCatching {
        val result = auth.signInWithEmailAndPassword(email, password).await()
        val uid = result.user?.uid ?: error("Login failed: no user id returned")
        fetchProfile(uid).getOrThrow()
    }

    override fun currentUserId(): String? = auth.currentUser?.uid

    override suspend fun fetchProfile(userId: String): Result<User> = runCatching {
        val snapshot = firestore.collection("users").document(userId).get().await()
        snapshot.toObject(User::class.java) ?: error("Profile not found for $userId")
    }

    override fun logout() = auth.signOut()
}
