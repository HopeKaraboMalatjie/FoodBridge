package com.foodbridge.app.data.model

/**
 * FR-1: User registration & login.
 * Mirrors the "userId / name / email / role" fields from the Part 1 data dictionary.
 */
data class User(
    val userId: String = "",
    val name: String = "",
    val email: String = "",
    val role: UserRole = UserRole.BOTH,
    val locationOptIn: Boolean = false,
    val mealsSaved: Int = 0,
    val co2Avoided: Double = 0.0,
    val badgeLevel: String = "Newcomer"
)

enum class UserRole { DONOR, RECIPIENT, BOTH }
