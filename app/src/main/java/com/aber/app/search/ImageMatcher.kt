package com.aber.app.search

import android.content.ContentResolver
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import com.aber.app.data.Post
import kotlin.math.max
import kotlin.math.min

/** One ranked search result. [score] is 0.0 - 1.0. */
data class Match(val post: Post, val score: Float)

/**
 * Image search = two simple signals combined:
 *  1. Visual similarity: compares a color histogram of the picked photo with each post's photo.
 *  2. Label similarity: Google ML Kit names what is in the photo ("Wallet", "Bag", ...);
 *     we check those names against each post's keywords.
 */
object ImageMatcher {

    private const val MAX_SIDE = 640
    private const val BINS_PER_CHANNEL = 4 // 4 x 4 x 4 = 64 colour buckets

    // ---------- Loading pictures ----------

    /** Loads a picture the user picked (a content:// Uri), scaled down so it uses little memory. */
    fun decodeUri(resolver: ContentResolver, uri: Uri): Bitmap? = try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(resolver, uri)) { decoder, info, _ ->
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE // needed so we can read the pixels
                val sample = max(info.size.width, info.size.height) / MAX_SIDE
                if (sample > 1) decoder.setTargetSampleSize(sample)
            }
        } else {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
            val options = BitmapFactory.Options().apply {
                inSampleSize = max(1, max(bounds.outWidth, bounds.outHeight) / MAX_SIDE)
            }
            resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }
        }
    } catch (e: Exception) {
        null
    }

    /** Loads a picture bundled in the app (drawable), scaled down. */
    fun decodeResource(resources: Resources, resId: Int): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeResource(resources, resId, bounds)
        val options = BitmapFactory.Options().apply {
            inSampleSize = max(1, max(bounds.outWidth, bounds.outHeight) / MAX_SIDE)
        }
        return BitmapFactory.decodeResource(resources, resId, options)
    }

    // ---------- Signal 1: colours ----------

    /** Counts how many pixels fall in each of 64 colour buckets, then divides by the total (so the sum is 1). */
    fun histogram(bitmap: Bitmap): FloatArray {
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        val counts = FloatArray(BINS_PER_CHANNEL * BINS_PER_CHANNEL * BINS_PER_CHANNEL)
        var counted = 0
        for (p in pixels) {
            if ((p ushr 24) < 128) continue // skip transparent pixels
            val r = ((p shr 16) and 0xFF) * BINS_PER_CHANNEL / 256
            val g = ((p shr 8) and 0xFF) * BINS_PER_CHANNEL / 256
            val b = (p and 0xFF) * BINS_PER_CHANNEL / 256
            counts[(r * BINS_PER_CHANNEL + g) * BINS_PER_CHANNEL + b] += 1f
            counted++
        }
        if (counted > 0) {
            for (i in counts.indices) counts[i] /= counted
        }
        return counts
    }

    /** Histogram intersection: 1.0 = identical colour distribution, 0.0 = nothing in common. */
    fun colorSimilarity(a: FloatArray, b: FloatArray): Float {
        var overlap = 0f
        for (i in a.indices) overlap += min(a[i], b[i])
        return overlap
    }

    // ---------- Signal 2: what is in the picture ----------

    /**
     * [labels] are (name, confidence) pairs from ML Kit. The score is the best matching label's
     * confidence, plus a small bonus for every extra label that also matches.
     */
    fun labelScore(labels: List<Pair<String, Float>>, post: Post): Float {
        var best = 0f
        var hits = 0
        for ((name, confidence) in labels) {
            val label = name.lowercase()
            val matches = post.keywords.any { k -> label == k || label.contains(k) || k.contains(label) }
            if (matches) {
                hits++
                best = max(best, confidence)
            }
        }
        if (hits == 0) return 0f
        return min(1f, best + 0.1f * (hits - 1))
    }

    // ---------- Putting it together ----------

    fun rank(posts: List<Post>, colorScores: Map<Int, Float>, labels: List<Pair<String, Float>>): List<Match> {
        val useLabels = labels.isNotEmpty()
        return posts
            .map { post ->
                val color = colorScores[post.id] ?: 0f
                val score = if (useLabels) 0.5f * color + 0.5f * labelScore(labels, post) else color
                Match(post, score)
            }
            .sortedByDescending { it.score }
    }
}
