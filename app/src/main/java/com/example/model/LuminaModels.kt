package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val username: String,
    val displayName: String,
    val avatarUrl: String,
    val bio: String,
    val followersCount: Int,
    val followingCount: Int,
    val isFollowing: Boolean = false,
    val accentColorHex: String = "#00F2FE", // Accents: cyan, magenta, emerald etc.
    val isMe: Boolean = false
) : Serializable

@Entity(tableName = "posts")
data class PostEntity(
    @PrimaryKey val id: String,
    val authorId: String,
    val content: String,
    val imageUrl: String? = null,
    val voiceDurationSec: Int? = null, // for voice posts features
    val pollOptions: String? = null, // comma separated options like "Glass,Mercury,Glassmorphism"
    val pollVotes: String? = null, // comma separated vote counts like "42,88,12"
    val chosenPollOptionIndex: Int? = null,
    val timestamp: Long,
    val likesCount: Int = 0,
    val repostsCount: Int = 0,
    val bookmarkCount: Int = 0,
    val repliesCount: Int = 0,
    val isLiked: Boolean = false,
    val isReposted: Boolean = false,
    val isBookmarked: Boolean = false
) : Serializable

@Entity(tableName = "comments")
data class CommentEntity(
    @PrimaryKey val id: String,
    val postId: String,
    val authorId: String,
    val content: String,
    val timestamp: Long
) : Serializable

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: String,
    val conversationWithId: String, // to easily fetch chat logs
    val senderId: String,
    val text: String,
    val timestamp: Long
) : Serializable

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey val id: String,
    val type: String, // "LIKE", "REPOST", "REPLY", "FOLLOW"
    val sourceUserId: String,
    val relatedPostId: String? = null,
    val timestamp: Long,
    val isRead: Boolean = false
) : Serializable

// Rich UI Presentation wrapper combining entities (e.g. Post with Author)
data class LuminaPost(
    val post: PostEntity,
    val author: UserEntity
)

data class LuminaComment(
    val comment: CommentEntity,
    val author: UserEntity
)

data class LuminaNotification(
    val notification: NotificationEntity,
    val sourceUser: UserEntity,
    val relatedPost: PostEntity? = null
)
