package com.aber.app.data

import androidx.annotation.DrawableRes

enum class PostType { LOST, FOUND }

/**
 * One Lost or Found report, as shown in the feed.
 * [keywords] are extra words that describe the item; image search compares them with what the
 * on-device image labeler recognizes in the photo (for example "bag" or "leather" for a wallet).
 */
data class Post(
    val id: Int,
    val type: PostType,
    val poster: String,
    val postedAgo: String,
    val itemType: String,
    val place: String,
    val date: String,
    val details: String,
    val claimAt: String?, // only for FOUND posts ("For Claiming")
    @DrawableRes val imageRes: Int,
    val keywords: Set<String>
) {
    /** True when [query] appears in the type, place or details (ignores upper/lower case). */
    fun matches(query: String): Boolean {
        if (query.isBlank()) return true
        val q = query.trim().lowercase()
        return itemType.lowercase().contains(q) ||
            place.lowercase().contains(q) ||
            details.lowercase().contains(q)
    }
}
