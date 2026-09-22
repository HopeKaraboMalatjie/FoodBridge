package com.foodbridge.app.ui.navigation

/** Mirrors the Figure 7 navigation map from the Part 1 design doc. */
object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME_FEED = "home_feed"
    const val MAP_VIEW = "map_view"
    const val POST_LISTING = "post_listing"
    const val IMPACT_PROFILE = "impact_profile"
    const val LISTING_DETAIL = "listing_detail/{listingId}"
    const val CHAT = "chat/{reservationId}"

    fun listingDetail(listingId: String) = "listing_detail/$listingId"
    fun chat(reservationId: String) = "chat/$reservationId"

    val bottomTabs = listOf(HOME_FEED, MAP_VIEW, POST_LISTING, IMPACT_PROFILE)
}
