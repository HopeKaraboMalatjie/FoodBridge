package com.foodbridge.app.data.model

/** FR-4: reserve/claim a listing. Created atomically by the REST API's reserve endpoint. */
data class Reservation(
    val reservationId: String = "",
    val listingId: String = "",
    val donorId: String = "",
    val recipientId: String = "",
    val status: ReservationStatus = ReservationStatus.ACTIVE,
    val createdAt: Long = System.currentTimeMillis()
)

enum class ReservationStatus { ACTIVE, COMPLETED, CANCELLED }

/** FR-5: lightweight in-app messaging, scoped to a single reservation. */
data class ChatMessage(
    val messageId: String = "",
    val reservationId: String = "",
    val senderId: String = "",
    val text: String = "",
    val sentAt: Long = System.currentTimeMillis()
)

/** FR-6: gamified impact tracking shown on the profile screen. */
data class ImpactStats(
    val userId: String = "",
    val mealsSaved: Int = 0,
    val co2Avoided: Double = 0.0,
    val badgeLevel: String = "Newcomer"
)
