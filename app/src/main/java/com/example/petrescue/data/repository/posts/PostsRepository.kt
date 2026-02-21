package com.example.petrescue.data.repository.posts

import android.util.Log
import androidx.lifecycle.LiveData
import com.example.petrescue.base.MyApplication
import com.example.petrescue.dao.AppDatabase
import com.example.petrescue.model.Post
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID
import kotlin.math.max

/**
 * Repository responsible for managing rescue posts.
 * It coordinates data between the remote Firestore database and the local Room database.
 */
class PostsRepository {
  companion object {
    /** Singleton instance for shared access across the app. */
    val shared = PostsRepository()
    /** Firestore collection name for posts. */
    const val POSTS = "posts"
  }

  private val db = try {
    FirebaseFirestore.getInstance()
  } catch (exception: Exception) {
    Log.e("PostsRepository", "Firestore not initialized. Using local DB only.", exception)
    null
  }

  private val postDao =
    AppDatabase.getDatabase(
      MyApplication.appContext ?: throw Exception("App context is null")
    ).postDao()

  /**
   * Retrieves all posts from the local database as observable LiveData.
   *
   * @return LiveData containing the current list of posts stored locally.
   */
  fun getAllPosts(): LiveData<MutableList<Post>> {
    return postDao.getAllPosts()
  }

  /**
   * Fetches posts from Firestore that have been updated since a specific time.
   *
   * @param since The timestamp (in milliseconds) after which to fetch updated posts.
   * @return A list of newly fetched Post objects from Firestore.
   */
  suspend fun getPostsFromDB(since: Long): List<Post> {
    if (db == null) return emptyList()

    return try {
      val snapshot = db.collection(POSTS)
        .whereGreaterThanOrEqualTo(Post.UPDATED_AT_KEY, Timestamp(since / 1000, 0))
        .get()
        .await()

      snapshot.documents.mapNotNull { it.data?.let { data -> Post.fromJson(data) } }
    } catch (exception: Exception) {
      Log.e("PostsRepository", "Error fetching posts from Firestore", exception)
      emptyList()
    }
  }

  /**
   * Syncs local data with the remote Firestore database.
   * Fetches only posts that were updated after the last known local sync time.
   */
  suspend fun refreshPosts() {
    val lastUpdated = Post.lastUpdated
    val newPosts = getPostsFromDB(lastUpdated)

    if (newPosts.isNotEmpty()) {
      withContext(Dispatchers.IO) {
        postDao.insertPosts(newPosts)

        val newestTime = newPosts.maxOf { it.updatedAt }
        Post.lastUpdated = max(lastUpdated, newestTime)
      }
    }
  }

  /**
   * Internal helper to persist a post object into the local Room database.
   *
   * @param post The post object to be saved.
   */
  private suspend fun insertPostLocally(post: Post) = withContext(Dispatchers.IO) {
    postDao.insertPost(post)
  }

  /**
   * Updates an existing post in Firestore and syncs the change locally.
   *
   * @param postId The unique identifier of the post to update.
   * @param updates A map containing the fields and their new values.
   * @return The updated Post object if successful, or null otherwise.
   */
  suspend fun updatePost(postId: String, updates: Map<String, Any>): Post? {
    if (db == null) return null

    return try {
      val postRef = db.collection(POSTS).document(postId)
      postRef.update(updates + mapOf(Post.UPDATED_AT_KEY to FieldValue.serverTimestamp())).await()

      val snapshot = postRef.get().await()
      val updatedPost = snapshot.data?.let { Post.fromJson(it) }
      updatedPost?.let { insertPostLocally(it) }

      updatedPost
    } catch (e: Exception) {
      Log.e("PostsRepository", "Error updating post", e)
      null
    }
  }

  /**
   * Creates a new rescue post in Firestore and saves it locally.
   *
   * @param post The post details to be created (ID will be generated).
   * @return The newly created Post object including its generated ID.
   */
  suspend fun createPost(post: Post): Post {
    val postId = UUID.randomUUID().toString()
    val postWithId = post.copy(id = postId)

    try {
      db
        ?.collection(POSTS)
        ?.document(postId)
        ?.set(postWithId.toJson)?.await()
    } catch (e: Exception) {
      Log.e("PostsRepository", "Error creating post in Firestore", e)
    }

    insertPostLocally(postWithId)
    
    return postWithId
  }

  /**
   * Deletes a post from both Firestore and the local database.
   *
   * @param post The post object to delete.
   */
  suspend fun deletePost(post: Post) {
    if (db != null && post.id.isNotEmpty()) {
      try {
        db.collection(POSTS).document(post.id).delete().await()
      } catch (exception: Exception) {
        Log.e("PostsRepository", "Error deleting post from Firestore", exception)
      }
    }

    withContext(Dispatchers.IO) {
      postDao.deletePost(post)
    }
  }
}
