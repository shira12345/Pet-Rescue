package com.example.petrescue.data.models

import com.example.petrescue.base.PostCompletion
import com.example.petrescue.base.PostsCompletion
import com.example.petrescue.base.StringCompletion
import com.example.petrescue.model.Post
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.firestore

class FirebaseModel {
  private val db = Firebase.firestore

  private companion object COLLECTIONS {
    const val POSTS = "posts"
  }

  fun getAllPosts(since: Long, completion: PostsCompletion) {
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

  fun addPost(post: Post, completion: StringCompletion) {
    db.collection(POSTS)
      .document(post.id)
      .set(post.toJson)
      .addOnSuccessListener { completion("Post created successfully! 🐾") }
      .addOnFailureListener { exception -> completion(exception.message) }
  }

  fun deletePost(post: Post) {
  }

  fun getPostById(id: String, completion: PostCompletion) {
  }
}
