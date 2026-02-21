package com.example.petrescue.data.repository.posts

import android.util.Log
import androidx.lifecycle.LiveData
import com.example.petrescue.base.MyApplication
import com.example.petrescue.base.PostsCompletion
import com.example.petrescue.dao.AppDatabase
import com.example.petrescue.model.Post
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.UUID
import java.util.concurrent.Executors
import kotlin.math.max

class PostsRepository {
  companion object {
    val shared = PostsRepository()
    const val POSTS = "posts"
  }

  private val db = try {
    FirebaseFirestore.getInstance()
  } catch (e: Exception) {
    Log.e("PostsRepository", "Firestore not initialized. Using local DB only.", e)
    null
  }

  private val postDao =
    AppDatabase.getDatabase(
      MyApplication.appContext ?: throw Exception("App context is null")
    )
      .postDao()

  private val executor = Executors.newSingleThreadExecutor()

  fun getAllPosts(): LiveData<MutableList<Post>> {
    return postDao.getAllPosts()
  }

  fun getPostsFromDB(since: Long, completion: PostsCompletion) {
    if (db == null) {
      completion(emptyList())
      return
    }

    db.collection(POSTS)
      .whereGreaterThanOrEqualTo(Post.UPDATED_AT_KEY, Timestamp(since / 1000, 0))
      .get()
      .addOnCompleteListener { result ->
        when (result.isSuccessful) {
          true -> completion(result.result.map { Post.fromJson(it.data) })
          false -> completion(emptyList())
        }
      }
  }

  fun refreshPosts() {
    val lastUpdated = Post.lastUpdated

    getPostsFromDB(lastUpdated) {
      executor.execute {
        if (it.isEmpty()) return@execute

        postDao.insertPosts(it)

        val newestTime = it.maxOf { post -> post.updatedAt }

        Post.lastUpdated = max(lastUpdated, newestTime)
      }
    }
  }

  private fun insertPostLocally(post: Post) {
    executor.execute { postDao.insertPost(post) }
  }

  suspend fun updatePost(postId: String, updates: Map<String, Any>): Post? {
    if (db == null) return null

    val postRef = db.collection(POSTS).document(postId)
    postRef.update(updates + mapOf(Post.UPDATED_AT_KEY to FieldValue.serverTimestamp())).await()
    val snapshot = postRef.get().await()
    val updatedPost = snapshot.data?.let { Post.fromJson(it) }
    updatedPost?.let { insertPostLocally(it) }
    return updatedPost
  }

  suspend fun createPost(post: Post): Post {
    val postId = UUID.randomUUID().toString()
    val postWithId = post.copy(id = postId)

    if (db != null) {
      db.collection(POSTS)
        .document(postId)
        .set(postWithId.toJson)
        .await()
    }

    insertPostLocally(postWithId)
    return postWithId
  }
}
