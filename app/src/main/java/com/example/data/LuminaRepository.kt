package com.example.data

import com.example.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class LuminaRepository(private val dao: LuminaDao) {

    // Fetch posts combined with author details
    val postsFlow: Flow<List<LuminaPost>> = combine(
        dao.getAllPostsFlow(),
        dao.getAllUsersFlow()
    ) { postsByTime, users ->
        val userMap = users.associateBy { it.id }
        postsByTime.mapNotNull { postEntity ->
            val author = userMap[postEntity.authorId] ?: return@mapNotNull null
            LuminaPost(post = postEntity, author = author)
        }
    }

    // Fetch individual post with author
    fun getPostByIdFlow(postId: String): Flow<LuminaPost?> {
        return combine(
            dao.getPostByIdFlow(postId),
            dao.getAllUsersFlow()
        ) { post, users ->
            if (post == null) return@combine null
            val author = users.find { it.id == post.authorId } ?: return@combine null
            LuminaPost(post = post, author = author)
        }
    }

    // Fetch comments combined with author
    fun getCommentsForPostFlow(postId: String): Flow<List<LuminaComment>> = combine(
        dao.getCommentsForPostFlow(postId),
        dao.getAllUsersFlow()
    ) { commentEntities, users ->
        val userMap = users.associateBy { it.id }
        commentEntities.mapNotNull { commentEntity ->
            val author = userMap[commentEntity.authorId] ?: return@mapNotNull null
            LuminaComment(comment = commentEntity, author = author)
        }
    }

    // List of Direct Messages
    fun getMessagesWithUserFlow(userId: String): Flow<List<MessageEntity>> {
        return dao.getMessagesWithUserFlow(userId)
    }

    // List of Notifications
    val notificationsFlow: Flow<List<LuminaNotification>> = combine(
        dao.getAllNotificationsFlow(),
        dao.getAllUsersFlow(),
        dao.getAllPostsFlow()
    ) { notifications, users, posts ->
        val userMap = users.associateBy { it.id }
        val postMap = posts.associateBy { it.id }
        notifications.mapNotNull { notif ->
            val sourceUser = userMap[notif.sourceUserId] ?: return@mapNotNull null
            val relatedPost = notif.relatedPostId?.let { postMap[it] }
            LuminaNotification(notification = notif, sourceUser = sourceUser, relatedPost = relatedPost)
        }
    }

    // Fetch current user flow
    fun getMeFlow(): Flow<UserEntity?> {
        return dao.getAllUsersFlow().map { list ->
            list.find { it.isMe }
        }
    }

    // Database Actions
    suspend fun createPost(content: String, imageUrl: String? = null, voiceDurationSec: Int? = null, pollOptions: String? = null) {
        val me = dao.getAllUsersFlow().map { it.find { u -> u.isMe } }.let {
            val list = dao.getAllUsersFlow().map { it.find { u -> u.isMe } }
            // simple check
            val users = dao.getAllUsersFlow()
            // We find "me" directly
            val all = dao.getAllUsersFlow().map { it }.let { dao.getAllUsersFlow() }
            val existingMe = dao.getUserById("me") ?: UserEntity(
                id = "me",
                username = "nas",
                displayName = "N. Albert",
                avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150&q=80",
                bio = "Designing the liquid glass boundaries of Compose in 2026.",
                followersCount = 1337,
                followingCount = 42,
                isFollowing = false,
                accentColorHex = "#7F00FF",
                isMe = true
            )
            existingMe
        }

        val newPost = PostEntity(
            id = "post_${System.currentTimeMillis()}",
            authorId = me.id,
            content = content,
            imageUrl = imageUrl,
            voiceDurationSec = voiceDurationSec,
            pollOptions = pollOptions,
            pollVotes = pollOptions?.let { it.split(",").joinToString(",") { "0" } },
            timestamp = System.currentTimeMillis()
        )
        dao.insertPost(newPost)
    }

    suspend fun insertComment(postId: String, text: String) {
        val comment = CommentEntity(
            id = "comment_${System.currentTimeMillis()}",
            postId = postId,
            authorId = "me",
            content = text,
            timestamp = System.currentTimeMillis()
        )
        dao.insertComment(comment)

        // Increment reply count
        val post = dao.getPostById(postId)
        if (post != null) {
            dao.updatePost(post.copy(repliesCount = post.repliesCount + 1))
        }
    }

    suspend fun toggleLike(postId: String) {
        val post = dao.getPostById(postId) ?: return
        val isLikedNew = !post.isLiked
        val change = if (isLikedNew) 1 else -1
        val updated = post.copy(
            isLiked = isLikedNew,
            likesCount = (post.likesCount + change).coerceAtLeast(0)
        )
        dao.updatePost(updated)

        // If liked, create a notification to the author
        if (isLikedNew && post.authorId != "me") {
            dao.insertNotification(
                NotificationEntity(
                    id = "notif_${System.currentTimeMillis()}",
                    type = "LIKE",
                    sourceUserId = "me",
                    relatedPostId = post.id,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    suspend fun toggleRepost(postId: String) {
        val post = dao.getPostById(postId) ?: return
        val isRepostedNew = !post.isReposted
        val change = if (isRepostedNew) 1 else -1
        val updated = post.copy(
            isReposted = isRepostedNew,
            repostsCount = (post.repostsCount + change).coerceAtLeast(0)
        )
        dao.updatePost(updated)

        if (isRepostedNew && post.authorId != "me") {
            dao.insertNotification(
                NotificationEntity(
                    id = "notif_${System.currentTimeMillis()}",
                    type = "REPOST",
                    sourceUserId = "me",
                    relatedPostId = post.id,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    suspend fun toggleBookmark(postId: String) {
        val post = dao.getPostById(postId) ?: return
        val isBookmarkedNew = !post.isBookmarked
        val change = if (isBookmarkedNew) 1 else -1
        val updated = post.copy(
            isBookmarked = isBookmarkedNew,
            bookmarkCount = (post.bookmarkCount + change).coerceAtLeast(0)
        )
        dao.updatePost(updated)
    }

    suspend fun voteInPoll(postId: String, optionIndex: Int) {
        val post = dao.getPostById(postId) ?: return
        if (post.chosenPollOptionIndex != null) return // already voted

        val votesList = post.pollVotes?.split(",")?.map { it.toIntOrNull() ?: 0 }?.toMutableList() ?: return
        if (optionIndex in 0 until votesList.size) {
            votesList[optionIndex] = votesList[optionIndex] + 1
        }
        val updated = post.copy(
            chosenPollOptionIndex = optionIndex,
            pollVotes = votesList.joinToString(",")
        )
        dao.updatePost(updated)
    }

    suspend fun toggleFollow(userId: String) {
        val user = dao.getUserById(userId) ?: return
        val isFollowingNew = !user.isFollowing
        val change = if (isFollowingNew) 1 else -1
        val updated = user.copy(
            isFollowing = isFollowingNew,
            followersCount = (user.followersCount + change).coerceAtLeast(0)
        )
        dao.updateUser(updated)

        if (isFollowingNew) {
            dao.insertNotification(
                NotificationEntity(
                    id = "notif_${System.currentTimeMillis()}",
                    type = "FOLLOW",
                    sourceUserId = "me",
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    suspend fun sendDirectMessage(recipientId: String, text: String) {
        val msg = MessageEntity(
            id = "msg_${System.currentTimeMillis()}",
            conversationWithId = recipientId,
            senderId = "me",
            text = text,
            timestamp = System.currentTimeMillis()
        )
        dao.insertMessage(msg)

        // Inject simulated immediate robotic/fluid response from the seed user matching their accent
        val replyText = when (recipientId) {
            "aeroglass" -> "Fascinating perspective! The caustics in our Liquid Glass shader reflect these exact equations."
            "novaflow" -> "Dynamic interfaces require movement! Love this idea. Sending you a liquid schema."
            "zephyr" -> " Obsidian light scatters nicely at 45 degrees. Let's build a prototype."
            else -> "Connecting refraction grid: message received."
        }

        kotlinx.coroutines.delay(1000) // brief delay for natural conversational rhythm

        dao.insertMessage(
            MessageEntity(
                id = "msg_reply_${System.currentTimeMillis()}",
                conversationWithId = recipientId,
                senderId = recipientId,
                text = replyText,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun prepopulateDatabaseIfEmpty() {
        val existingUsers = dao.getUserById("me")
        if (existingUsers != null) return // Already seeded

        // 1. Seed Users
        val seedUsers = listOf(
            UserEntity(
                id = "me",
                username = "nas",
                displayName = "N. Albert",
                avatarUrl = "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=150&q=80",
                bio = "Digital architect & pioneer of the Glass UI standard in Compose. Building future interfaces.",
                followersCount = 14200,
                followingCount = 280,
                isFollowing = false,
                accentColorHex = "#7F00FF",
                isMe = true
            ),
            UserEntity(
                id = "aeroglass",
                username = "aeroglass",
                displayName = "Aero Glass",
                avatarUrl = "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=150&q=80",
                bio = "Synthesizing caustics and light refraction into physical-digital mediums. 3D UX Lab Lead.",
                followersCount = 84900,
                followingCount = 143,
                isFollowing = true,
                accentColorHex = "#00F2FE", // Cyan
                isMe = false
            ),
            UserEntity(
                id = "novaflow",
                username = "novaflow",
                displayName = "Nova Flow",
                avatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=150&q=80",
                bio = "Fluid dynamicist. Merging mercury shaders, liquid metallic physics, and interactive spring motions.",
                followersCount = 120500,
                followingCount = 899,
                isFollowing = false,
                accentColorHex = "#FF2592", // Magenta
                isMe = false
            ),
            UserEntity(
                id = "zephyr",
                username = "zephyr",
                displayName = "Zephyr Obsidian",
                avatarUrl = "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=150&q=80",
                bio = "Observing caustics, shadows, and the exquisite light transmission indices of fine heavy flint glass.",
                followersCount = 3300,
                followingCount = 104,
                isFollowing = true,
                accentColorHex = "#00FF87", // Emerald
                isMe = false
            )
        )
        dao.insertUsers(seedUsers)

        // 2. Seed Posts
        val seedPosts = listOf(
            PostEntity(
                id = "seed_post_1",
                authorId = "aeroglass",
                content = "Introducing the refraction grid for our next Lumina UI. Notice the light-bending caustics filtering through the translucent layer. It responsive, 3D, and entirely driven by real-time spring physics.",
                imageUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=800&q=80", // Beautiful glass mockup background
                timestamp = System.currentTimeMillis() - 10 * 60 * 1000, // 10m ago
                likesCount = 2841,
                repostsCount = 423,
                bookmarkCount = 982,
                repliesCount = 3,
                isLiked = true,
                isBookmarked = true
            ),
            PostEntity(
                id = "seed_post_2",
                authorId = "novaflow",
                content = "What refractive index creates the most realistic liquid glass experience for dynamic panels?",
                pollOptions = "1.33 (Water),1.52 (Float Glass),1.90 (Flint Glass),2.42 (Diamond)",
                pollVotes = "410,1232,845,201",
                chosenPollOptionIndex = null,
                timestamp = System.currentTimeMillis() - 45 * 60 * 1000, // 45m ago
                likesCount = 874,
                repostsCount = 92,
                bookmarkCount = 44,
                repliesCount = 1
            ),
            PostEntity(
                id = "seed_post_3",
                authorId = "zephyr",
                content = "Just updated my obsidian caustics canvas renderer. Tap to listen to the crystalline soundscape waves I generated using high-frequency resonance. Floating beautifully.",
                voiceDurationSec = 34, // Simulated Voice Note!
                timestamp = System.currentTimeMillis() - 4 * 3600 * 1000, // 4 hrs ago
                likesCount = 192,
                repostsCount = 12,
                bookmarkCount = 19,
                repliesCount = 2
            ),
            PostEntity(
                id = "seed_post_4",
                authorId = "aeroglass",
                content = "Liquid glass feels like mercury suspended in zero gravity. The way the user interface bends, flows, and reforms around the touch points completely breaks the traditional flat screen container.",
                timestamp = System.currentTimeMillis() - 24 * 3600 * 1000, // 1 day ago
                likesCount = 5980,
                repostsCount = 1230,
                bookmarkCount = 3022,
                repliesCount = 12
            )
        )
        dao.insertPosts(seedPosts)

        // 3. Seed Comments
        val seedComments = listOf(
            CommentEntity(
                id = "comment_s1_1",
                postId = "seed_post_1",
                authorId = "novaflow",
                content = "This is insane! The glass edges look so thick yet responsive. What custom shader parameters are you tracking under raw touch events?",
                timestamp = System.currentTimeMillis() - 8 * 60 * 1000
            ),
            CommentEntity(
                id = "comment_s1_2",
                postId = "seed_post_1",
                authorId = "zephyr",
                content = "Absolutely magnificent refraction. The subtle chromatic aberration on the perimeter gives it unbelievable weight.",
                timestamp = System.currentTimeMillis() - 5 * 60 * 1000
            ),
            CommentEntity(
                id = "comment_s1_3",
                postId = "seed_post_1",
                authorId = "me",
                content = "Agree, outstanding! Let me try integrating this shader directly into my profile background rendering.",
                timestamp = System.currentTimeMillis() - 2 * 60 * 1000
            ),
            CommentEntity(
                id = "comment_s2_1",
                postId = "seed_post_2",
                authorId = "aeroglass",
                content = "Float glass (1.52) gives a clean material balance, but flint glass (1.90) provides that rich metallic specular look! Go with Flint.",
                timestamp = System.currentTimeMillis() - 30 * 60 * 1000
            )
        )
        for (comment in seedComments) {
            dao.insertComment(comment)
        }

        // 4. Seed Messages
        val seedMessages = listOf(
            MessageEntity(
                id = "seed_msg_1",
                conversationWithId = "aeroglass",
                senderId = "aeroglass",
                text = "Hey Albert! I loved your article on declarative GlassMorphism. Are you available for a virtual coffee tomorrow?",
                timestamp = System.currentTimeMillis() - 3600 * 1000 * 12
            ),
            MessageEntity(
                id = "seed_msg_2",
                conversationWithId = "aeroglass",
                senderId = "me",
                text = "Absolutely! I would love to show you the dynamic spring physics I just integrated. Let do it.",
                timestamp = System.currentTimeMillis() - 3600 * 1000 * 11
            )
        )
        for (msg in seedMessages) {
            dao.insertMessage(msg)
        }

        // 5. Seed Notifications
        val seedNotifications = listOf(
            NotificationEntity(
                id = "seed_notif_1",
                type = "LIKE",
                sourceUserId = "novaflow",
                relatedPostId = "seed_post_1",
                timestamp = System.currentTimeMillis() - 15 * 60 * 1000
            ),
            NotificationEntity(
                id = "seed_notif_2",
                type = "REPOST",
                sourceUserId = "aeroglass",
                relatedPostId = "seed_post_1",
                timestamp = System.currentTimeMillis() - 20 * 60 * 1000
            ),
            NotificationEntity(
                id = "seed_notif_3",
                type = "FOLLOW",
                sourceUserId = "zephyr",
                timestamp = System.currentTimeMillis() - 1 * 3600 * 1000
            )
        )
        for (notif in seedNotifications) {
            dao.insertNotification(notif)
        }
    }
}
