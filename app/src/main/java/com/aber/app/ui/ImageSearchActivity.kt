package com.aber.app.ui

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.aber.app.R
import com.aber.app.data.PostRepository
import com.aber.app.databinding.ActivityImageSearchBinding
import com.aber.app.search.ImageMatcher
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import java.util.concurrent.Executors
import kotlin.math.roundToInt

/**
 * Feature 2 - Image search.
 * The user picks a photo; the app compares it with every Lost/Found post and ranks the results.
 * Everything runs on the phone (no internet needed).
 */
class ImageSearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivityImageSearchBinding
    private val adapter = MatchAdapter()

    // Heavy work (decoding pictures, counting pixels) runs on this background thread.
    private val worker = Executors.newSingleThreadExecutor()

    // Colour histograms of the posts' own photos. Only touched from the worker thread.
    private val postHistograms = HashMap<Int, FloatArray>()

    private val labeler by lazy {
        ImageLabeling.getClient(
            ImageLabelerOptions.Builder().setConfidenceThreshold(0.6f).build()
        )
    }

    // The system Photo Picker. It needs no storage permission.
    private val pickPhoto = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) analyze(uri)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImageSearchBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.rvMatches.layoutManager = LinearLayoutManager(this)
        binding.rvMatches.adapter = adapter

        binding.btnPick.setOnClickListener {
            pickPhoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        worker.shutdown()
        labeler.close()
    }

    /** Step 1 (background thread): load the photo and compare colours with every post. */
    private fun analyze(uri: Uri) {
        setLoading(true)
        worker.execute {
            val picked = ImageMatcher.decodeUri(contentResolver, uri)
            if (picked == null) {
                runOnUiThread {
                    setLoading(false)
                    Toast.makeText(this, R.string.load_error, Toast.LENGTH_SHORT).show()
                }
                return@execute
            }

            val pickedHistogram = ImageMatcher.histogram(picked)
            val colorScores = HashMap<Int, Float>()
            for (post in PostRepository.posts) {
                val histogram = postHistograms.getOrPut(post.id) {
                    val bitmap = ImageMatcher.decodeResource(resources, post.imageRes)
                    if (bitmap != null) ImageMatcher.histogram(bitmap) else FloatArray(64)
                }
                colorScores[post.id] = ImageMatcher.colorSimilarity(pickedHistogram, histogram)
            }

            runOnUiThread {
                if (isDestroyed) return@runOnUiThread
                binding.ivPreview.setImageBitmap(picked)
                binding.ivPreview.visibility = View.VISIBLE
                labelAndShow(picked, colorScores)
            }
        }
    }

    /** Step 2 (ML Kit): ask what is in the photo, then combine with the colour scores and show the results. */
    private fun labelAndShow(picked: Bitmap, colorScores: Map<Int, Float>) {
        labeler.process(InputImage.fromBitmap(picked, 0))
            .addOnSuccessListener { result ->
                showResults(colorScores, result.map { it.text to it.confidence })
            }
            .addOnFailureListener {
                // Labeling failed: still show results using colours only.
                showResults(colorScores, emptyList())
            }
    }

    private fun showResults(colorScores: Map<Int, Float>, labels: List<Pair<String, Float>>) {
        if (isDestroyed) return
        setLoading(false)

        val ranked = ImageMatcher.rank(PostRepository.posts, colorScores, labels)
        adapter.submit(ranked)
        binding.tvResultsHeader.visibility = View.VISIBLE

        binding.tvLabels.visibility = View.VISIBLE
        binding.tvLabels.text = if (labels.isEmpty()) {
            getString(R.string.nothing_detected)
        } else {
            val names = labels.joinToString(", ") { (name, confidence) -> "$name ${(confidence * 100).roundToInt()}%" }
            getString(R.string.detected, names)
        }
    }

    private fun setLoading(loading: Boolean) {
        binding.progress.visibility = if (loading) View.VISIBLE else View.GONE
        binding.btnPick.isEnabled = !loading
    }
}
