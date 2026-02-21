package com.example.petrescue.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.petrescue.model.Post

/**
 * Data Access Object for the posts table.
 * Handles local database operations for rescue posts.
 */
@Dao
interface PostDao {
  /**
   * Retrieves all posts from the local database, ordered by the last update time.
   *
   * @return Observable LiveData containing a list of all posts.
   */
  @Query("SELECT * FROM posts ORDER BY updatedAt DESC")
  fun getAllPosts(): LiveData<MutableList<Post>>

  /**
   * Retrieves all posts created by a specific user, identified by their email.
   *
   * @param email The email of the creator.
   * @return Observable LiveData containing a list of posts created by the user.
   */
  @Query("SELECT * FROM posts WHERE creatorEmail = :email COLLATE NOCASE ORDER BY updatedAt DESC")
  fun getPostsByEmail(email: String): LiveData<MutableList<Post>>

  /**
   * Inserts a list of posts into the local database.
   * Replaces existing posts if there is a primary key conflict.
   *
   * @param posts The list of posts to insert or update.
   */
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insertPosts(posts: List<Post>)

  /**
   * Inserts or updates a single post in the local database.
   *
   * @param post The post to save.
   */
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insertPost(post: Post)
}
