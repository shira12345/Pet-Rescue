package com.example.petrescue.features.posts_feed

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.petrescue.databinding.ItemPostBinding
import com.example.petrescue.model.Post
import com.squareup.picasso.Picasso

class PostsAdapter(private val onPostClick: (Post) -> Unit) :
    ListAdapter<Post, PostsAdapter.PostViewHolder>(PostDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = ItemPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding, onPostClick)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class PostViewHolder(
        private val binding: ItemPostBinding,
        private val onPostClick: (Post) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(post: Post) {
            binding.tvPetName.text = post.petName
            binding.tvStatusTag.text = post.status
            binding.tvDetails.text = "${post.petType} • ${post.breed ?: "Unknown Breed"}"
            binding.tvDescription.text = post.description

            if (!post.imageUri.isNullOrEmpty()) {
                Picasso.get().load(post.imageUri).into(binding.ivPostImage)
            } else {
                binding.ivPostImage.setImageResource(com.example.petrescue.R.drawable.logo)
            }

            binding.root.setOnClickListener {
                onPostClick(post)
            }
        }
    }

    class PostDiffCallback : DiffUtil.ItemCallback<Post>() {
        override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
            return oldItem == newItem
        }
    }
}