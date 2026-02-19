package com.example.petrescue.base

import com.example.petrescue.model.Post

typealias PostsCompletion = (List<Post>) -> Unit

typealias PostCompletion = (Post) -> Unit

typealias Completion = () -> Unit

typealias StringCompletion = (String?) -> Unit