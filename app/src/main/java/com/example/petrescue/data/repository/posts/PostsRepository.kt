package com.example.petrescue.data.repository.posts

import com.example.petrescue.model.Post
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await
import java.util.UUID

class PostsRepository {
  private val db = Firebase.firestore

  private companion object {
    const val POSTS = "posts"
  }

  suspend fun getAllPosts(since: Long): List<Post> {
    val snapshot = db.collection(POSTS)
      .whereGreaterThanOrEqualTo(Post.UPDATED_AT_KEY, Timestamp(since / 1000, 0))
      .get()
      .await()

    return snapshot.map { Post.fromJson(it.data) }
  }

  suspend fun updatePost(postId: String, updates: Map<String, Any>): Post? {
    val postRef = db.collection(POSTS).document(postId)

    postRef.update(updates + mapOf(Post.UPDATED_AT_KEY to FieldValue.serverTimestamp())).await()

    val snapshot = postRef.get().await()
    
    return snapshot.data?.let { Post.fromJson(it) }
  }

  suspend fun createPost(post: Post): Post {
    val postId = UUID.randomUUID().toString()
    val postWithId = post.copy(id = postId)

    db.collection(POSTS)
      .document(postId)
      .set(postWithId.toJson)
      .await()

    return postWithId
  }
}
