package com.example.petrescue.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.petrescue.model.Post

@Dao
interface PostDao {
  @Query("SELECT * FROM posts ORDER BY updatedAt DESC")
  fun getAllPosts(): LiveData<MutableList<Post>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insertPosts(posts: List<Post>)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insertPost(post: Post)
}