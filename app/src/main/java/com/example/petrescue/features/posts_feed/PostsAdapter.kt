package com.example.petrescue.features.posts_feed

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.petrescue.databinding.ItemPostBinding
import com.example.petrescue.model.Post
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso

/**
 * RecyclerView adapter for displaying a list of rescue posts.
 * Utilizes [ListAdapter] for efficient list updates and [DiffUtil] for calculating differences.
 *
 * @param currentUserEmail The email of the currently logged-in user, used to show/hide edit actions.
 * @param onPostClick Lambda invoked when a post item is clicked.
 * @param onEditClick Lambda invoked when the edit button on a post is clicked.
 */
class PostsAdapter(
  private val currentUserEmail: String?,
  private val onPostClick: (Post) -> Unit,
  private val onEditClick: (Post) -> Unit
) : ListAdapter<Post, PostsAdapter.PostViewHolder>(PostDiffCallback()) {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
    val binding = ItemPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    return PostViewHolder(binding, onPostClick)
  }

  override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
    val post = getItem(position)
    holder.bind(post, currentUserEmail, onEditClick)
  }

  /**
   * ViewHolder for individual post items.
   *
   * @param binding View binding for the post item layout.
   * @param onPostClick Callback for root view clicks.
   */
  class PostViewHolder(
    private val binding: ItemPostBinding,
    private val onPostClick: (Post) -> Unit
  ) : RecyclerView.ViewHolder(binding.root) {

    /**
     * Binds post data to the UI elements.
     *
     * @param post The post object to display.
     * @param currentUserEmail Email used to determine ownership for edit permissions.
     * @param onEditClick Callback for the edit button.
     */
    fun bind(post: Post, currentUserEmail: String?, onEditClick: (Post) -> Unit) {
      binding.tvPetName.text = post.petName
      binding.tvStatusTag.text = post.status
      binding.tvDetails.text = "${post.petType} • ${post.breed ?: "Unknown Breed"}"
      binding.tvDescription.text = post.description

      if (!post.imageUri.isNullOrEmpty()) {
        binding.imageProgressBar.visibility = View.VISIBLE
        Picasso.get()
          .load(post.imageUri)
          .into(binding.ivPostImage, object : Callback {
            override fun onSuccess() {
              binding.imageProgressBar.visibility = View.GONE
            }

            override fun onError(e: Exception?) {
              binding.imageProgressBar.visibility = View.GONE
              binding.ivPostImage.setImageResource(com.example.petrescue.R.drawable.logo)
            }
          })
      } else {
        binding.imageProgressBar.visibility = View.GONE
        binding.ivPostImage.setImageResource(com.example.petrescue.R.drawable.logo)
      }

      binding.btnEdit.visibility =
        if (post.creatorEmail == currentUserEmail) View.VISIBLE else View.GONE
      binding.btnEdit.setOnClickListener { onEditClick(post) }

      binding.root.setOnClickListener {
        onPostClick(post)
      }
    }
  }

  /**
   * Callback for calculating the diff between two non-null items in a list.
   */
  class PostDiffCallback : DiffUtil.ItemCallback<Post>() {
    override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
      return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
      return oldItem == newItem
    }
  }
}
