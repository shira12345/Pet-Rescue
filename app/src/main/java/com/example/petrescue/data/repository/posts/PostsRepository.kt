package com.example.petrescue.data.repository.posts

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import com.example.petrescue.base.MyApplication
import com.example.petrescue.base.PostCompletion
import com.example.petrescue.base.StringCompletion
import com.example.petrescue.dao.AppDatabase
import com.example.petrescue.data.models.CloudinaryStorageModel
import com.example.petrescue.data.models.FirebaseModel
import com.example.petrescue.model.Post
import java.util.concurrent.Executors
import kotlin.math.min

class PostsRepository private constructor() {

  private val storageModel = CloudinaryStorageModel()
  private val firebaseModel = FirebaseModel()

  private val executor = Executors.newSingleThreadExecutor()
  private val database = AppDatabase.getDatabase(
    MyApplication.appContext ?: throw IllegalStateException("Context is null")
  )

  private val posts: LiveData<MutableList<Post>>? = null

  companion object Companion {
    val shared = PostsRepository()
  }

  fun getAllPosts(): LiveData<MutableList<Post>> {
    return posts ?: database.postDao().getAllPosts()
  }

  fun refreshPosts() {
    val lastUpdated = Post.lastUpdated

    firebaseModel.getAllPosts(lastUpdated) {
      executor.execute {
        var time = lastUpdated

        for (post in it) {
          database.postDao().insertPosts(post)

          post.updatedAt.let { postLastUpdated ->
            time = min(time, postLastUpdated)
          }

          Post.lastUpdated = time
        }
      }

    }
  }

  fun createPost(image: Bitmap, post: Post, completion: StringCompletion) {
    firebaseModel.addPost(post) {
      storageModel.uploadPostImage(image, post) { imageUri ->
        if (imageUri.isNullOrEmpty())
          return@uploadPostImage completion("An error occurred during image upload, please try again")

        val postCopy = post.copy(imageUri = imageUri)
        firebaseModel.addPost(postCopy, completion)
      }
    }
  }

  fun deletePost(post: Post) {
    firebaseModel.deletePost(post)
  }

  fun getPostById(id: String, completion: PostCompletion) {
    firebaseModel.getPostById(id, completion)
  }
}