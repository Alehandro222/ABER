package com.aber.app.ui

import com.aber.app.PostDetailsActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.recyclerview.widget.RecyclerView
import com.aber.app.R
import com.aber.app.data.Post
import com.aber.app.data.PostType
import com.aber.app.databinding.ItemPostBinding
import android.content.Intent

/** Shows a list of [Post]s as cards. A RecyclerView adapter creates one card per row and reuses them while scrolling. */
class PostAdapter : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    private var items: List<Post> = emptyList()

    fun submit(newItems: List<Post>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = ItemPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) = holder.bind(items[position])

    override fun getItemCount(): Int = items.size

    class PostViewHolder(private val binding: ItemPostBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(post: Post) {
            val isLost = post.type == PostType.LOST

            binding.root.setOnClickListener {
                val context = binding.root.context
                val intent = Intent(context, PostDetailsActivity::class.java).apply {
                    putExtra("poster", post.poster)
                    putExtra("type", post.type.name)
                    putExtra("place", post.place)
                    putExtra("date", post.date)
                    putExtra("details", post.details)
                    putExtra("claim", "Claim at the guard's office")
                    putExtra("contact", "09123456789")
                    putExtra("imageRes", post.imageRes)
                }
                context.startActivity(intent)
        }

            binding.tvPoster.text = post.poster
            binding.tvTime.text = post.postedAgo
            binding.ivPhoto.setImageResource(post.imageRes)

            binding.tvType.text = line(R.string.label_type, post.itemType)
            binding.tvPlace.text = line(
                if (isLost) R.string.label_place_last_seen else R.string.label_place_found, post.place
            )
            binding.tvDate.text = line(
                if (isLost) R.string.label_date_last_seen else R.string.label_date_found, post.date
            )
            binding.tvDetails.text = line(R.string.label_details, post.details)

            if (post.claimAt != null) {
                binding.tvClaim.visibility = View.VISIBLE
                binding.tvClaim.text = line(R.string.label_for_claiming, post.claimAt)
            } else {
                binding.tvClaim.visibility = View.GONE
            }
        }

        /** "Label: value" with the label in bold. */
        private fun line(@StringRes label: Int, value: String): CharSequence {
            val labelText = binding.root.context.getString(label)
            return buildSpannedString {
                bold { append(labelText) }
                append(" ")
                append(value)
            }
        }
    }
}
