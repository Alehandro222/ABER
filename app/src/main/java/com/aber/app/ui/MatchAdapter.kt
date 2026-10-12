package com.aber.app.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.aber.app.R
import com.aber.app.data.PostType
import com.aber.app.databinding.ItemMatchBinding
import com.aber.app.search.Match
import kotlin.math.roundToInt

/** Lists image-search results, best match first. */
class MatchAdapter : RecyclerView.Adapter<MatchAdapter.MatchViewHolder>() {

    private var items: List<Match> = emptyList()

    fun submit(newItems: List<Match>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MatchViewHolder {
        val binding = ItemMatchBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MatchViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MatchViewHolder, position: Int) {
        holder.bind(items[position], isBest = position == 0 && items[position].score >= BEST_MATCH_THRESHOLD)
    }

    override fun getItemCount(): Int = items.size

    class MatchViewHolder(private val binding: ItemMatchBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(match: Match, isBest: Boolean) {
            val context = binding.root.context
            val post = match.post
            val percent = (match.score * 100).roundToInt()
            val tag = context.getString(if (post.type == PostType.LOST) R.string.tag_lost else R.string.tag_found)

            binding.ivThumb.setImageResource(post.imageRes)
            binding.tvBadge.visibility = if (isBest) View.VISIBLE else View.GONE
            binding.tvTitle.text = "${post.itemType} ($tag)"
            binding.tvSubtitle.text = post.place
            binding.tvPercent.text = context.getString(R.string.match_percent, percent)
            binding.pbScore.progress = percent
        }
    }

    private companion object {
        const val BEST_MATCH_THRESHOLD = 0.35f
    }
}
