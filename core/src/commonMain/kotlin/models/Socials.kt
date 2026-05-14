package com.toro.models

import kotlinx.serialization.Serializable

enum class ChatStatus { PENDING, ACCEPTED, REJECTED }

@Serializable
data class User(
    val id: String,
    val username: String,
    val profileImageUrl: String? = null
)

@Serializable
data class ChatRequest(
    val id: String,
    val senderId: String,
    val senderName: String,
    val status: ChatStatus = ChatStatus.PENDING
)

@Serializable
data class ChatMessage(
    val id: String,
    val senderId: String,
    val content: String,
    val timestamp: Long,
)

@Serializable
data class Post(
    val id: String,
    val authorId: String,
    val authorName: String,
    val content: String,
    val timestamp: Long,
    val likesCount: Int = 0,
    val isLiked: Boolean = false,
    var isBookmarked: Boolean = false
)

@Serializable
data class Tag(
    val id: String,
    val postId: String,
    val content: String
)

@Serializable
data class Comment(
    val id: String,
    val postId: String,
    val authorId: String,
    val authorName: String,
    val content: String,
    val timestamp: Long
)

@Serializable
data class CommentRequest(
    val content: String
)

@Serializable
data class PostRequest(
    val content: String,
    val tags: List<String> = emptyList()
)

@Serializable
data class Conversation(
    val conversationId: String,
    val otherUserId: String,
    val otherUserName: String,
    val lastMessage: String?,
    val timestamp: Long
)

@Serializable
data class Bookmark(
    val id: String = "",
    val userId: String,
    val postId: String,
    val timestamp: Long
)