package com.foodbridge.app.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.foodbridge.app.data.model.User
import com.foodbridge.app.data.model.UserRole
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.UUID

interface AuthRepository {
    suspend fun register(name: String, email: String, password: String, role: UserRole): Result<User>
    suspend fun login(email: String, password: String): Result<User>
    suspend fun loginWithGoogle(idToken: String): Result<User>
    suspend fun loginWithGoogleFallback(email: String, displayName: String): Result<User>
    fun currentUserId(): String?
    suspend fun fetchProfile(userId: String): Result<User>
    fun getCurrentUser(): User?
    fun logout()
}

class FirebaseAuthRepository(
    private val context: Context? = null,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : AuthRepository {

    private val prefs: SharedPreferences? by lazy {
        context?.getSharedPreferences("foodbridge_user_prefs", Context.MODE_PRIVATE)
    }

    override suspend fun register(
        name: String,
        email: String,
        password: String,
        role: UserRole
    ): Result<User> = runCatching {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        val uid = result.user?.uid ?: error("Registration failed: no user id returned")
        val user = User(userId = uid, name = name, email = email, role = role)

        saveLocalProfile(user)

        runCatching {
            firestore.collection("users").document(uid).set(user).await()
        }
        user
    }

    override suspend fun login(email: String, password: String): Result<User> = runCatching {
        val result = auth.signInWithEmailAndPassword(email, password).await()
        val uid = result.user?.uid ?: error("Login failed: no user id returned")
        val emailStr = result.user?.email.orEmpty().ifEmpty { email }
        val displayName = result.user?.displayName?.takeIf { it.isNotBlank() }
            ?: emailStr.substringBefore("@").replace(".", " ").ifEmpty { "User" }

        val profile = runCatching { fetchProfile(uid).getOrThrow() }.getOrElse {
            val fallback = User(userId = uid, name = displayName, email = emailStr, role = UserRole.RECIPIENT)
            saveLocalProfile(fallback)
            fallback
        }
        saveLocalProfile(profile)
        profile
    }

    override suspend fun loginWithGoogle(idToken: String): Result<User> = runCatching {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val result = auth.signInWithCredential(credential).await()
        val uid = result.user?.uid ?: error("Google Sign-In failed")
        val email = result.user?.email.orEmpty()
        val displayName = result.user?.displayName?.takeIf { it.isNotBlank() }
            ?: email.substringBefore("@").replace(".", " ").ifEmpty { "Google Member" }

        val googleUser = User(userId = uid, name = displayName, email = email, role = UserRole.RECIPIENT)

        saveLocalProfile(googleUser)
        runCatching { firestore.collection("users").document(uid).set(googleUser).await() }
        googleUser
    }

    override suspend fun loginWithGoogleFallback(email: String, displayName: String): Result<User> = runCatching {
        val uid = auth.currentUser?.uid ?: ("google_" + UUID.randomUUID().toString().take(8))
        val resolvedEmail = email.ifBlank { auth.currentUser?.email.orEmpty() }.ifEmpty { "google_user@foodbridge.app" }
        val resolvedName = displayName.takeIf { it.isNotBlank() && it != "Verified User" && it != "Member" }
            ?: resolvedEmail.substringBefore("@").replace(".", " ")

        val user = User(
            userId = uid,
            name = resolvedName.ifBlank { "Google Member" },
            email = resolvedEmail,
            role = UserRole.RECIPIENT
        )

        saveLocalProfile(user)
        runCatching { firestore.collection("users").document(uid).set(user).await() }
        user
    }

    override fun currentUserId(): String? = auth.currentUser?.uid ?: prefs?.getString("current_user_id", null)

    override fun getCurrentUser(): User? {
        val uid = currentUserId() ?: return null
        val name = prefs?.getString("current_user_name", null)
            ?: prefs?.getString("user_name_$uid", null)
            ?: auth.currentUser?.displayName
            ?: auth.currentUser?.email?.substringBefore("@")
            ?: return null

        val email = prefs?.getString("current_user_email", null)
            ?: prefs?.getString("user_email_$uid", null)
            ?: auth.currentUser?.email.orEmpty()

        val roleStr = prefs?.getString("current_user_role", null)
            ?: prefs?.getString("user_role_$uid", null)
            ?: UserRole.RECIPIENT.name

        val role = runCatching { UserRole.valueOf(roleStr) }.getOrDefault(UserRole.RECIPIENT)
        return User(userId = uid, name = name, email = email, role = role)
    }

    override suspend fun fetchProfile(userId: String): Result<User> = runCatching {
        val activeUser = getCurrentUser()
        if (activeUser != null && activeUser.userId == userId && activeUser.name.isNotBlank()) {
            return@runCatching activeUser
        }

        val localUser = getLocalProfile(userId)
        val snapshot = runCatching {
            firestore.collection("users").document(userId).get().await()
        }.getOrNull()

        val firestoreUser = snapshot?.toObject(User::class.java)
        if (firestoreUser != null && firestoreUser.name.isNotBlank() && firestoreUser.name != "Verified User") {
            saveLocalProfile(firestoreUser)
            firestoreUser
        } else if (localUser != null && localUser.name.isNotBlank() && localUser.name != "Verified User") {
            localUser
        } else {
            val firebaseUser = auth.currentUser
            val email = firebaseUser?.email.orEmpty()
            val name = firebaseUser?.displayName?.takeIf { it.isNotBlank() && it != "Verified User" }
                ?: email.substringBefore("@").replace(".", " ").ifEmpty { "Community Member" }
            val fallback = User(
                userId = userId,
                name = name,
                email = email,
                role = UserRole.RECIPIENT
            )
            saveLocalProfile(fallback)
            fallback
        }
    }

    override fun logout() {
        auth.signOut()
        prefs?.edit()?.clear()?.apply()
    }

    private fun saveLocalProfile(user: User) {
        prefs?.edit()
            ?.putString("current_user_id", user.userId)
            ?.putString("current_user_name", user.name)
            ?.putString("current_user_email", user.email)
            ?.putString("current_user_role", user.role.name)
            ?.putString("user_id_${user.userId}", user.userId)
            ?.putString("user_name_${user.userId}", user.name)
            ?.putString("user_email_${user.userId}", user.email)
            ?.putString("user_role_${user.userId}", user.role.name)
            ?.apply()
    }

    private fun getLocalProfile(userId: String): User? {
        val name = prefs?.getString("user_name_$userId", null)?.takeIf { it.isNotBlank() && it != "Verified User" }
            ?: prefs?.getString("current_user_name", null)?.takeIf { it.isNotBlank() }
            ?: return null
        val email = prefs?.getString("user_email_$userId", "") ?: prefs?.getString("current_user_email", "") ?: ""
        val roleStr = prefs?.getString("user_role_$userId", UserRole.RECIPIENT.name) ?: UserRole.RECIPIENT.name
        val role = runCatching { UserRole.valueOf(roleStr) }.getOrDefault(UserRole.RECIPIENT)
        return User(userId = userId, name = name, email = email, role = role)
    }
}
