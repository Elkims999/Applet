package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.LuminaDatabase
import com.example.data.LuminaRepository
import com.example.model.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class LuminaViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: LuminaRepository

    init {
        val database = LuminaDatabase.getDatabase(application)
        repository = LuminaRepository(database.luminaDao())

        // Ensure database has premium mock data on startup
        viewModelScope.launch {
            repository.prepopulateDatabaseIfEmpty()
        }
    }

    // Active User
    val me: StateFlow<UserEntity?> = repository.getMeFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Current timeline tab: 0 = For You, 1 = Following
    private val _timelineTab = MutableStateFlow(0)
    val timelineTab = _timelineTab.asStateFlow()

    fun setTimelineTab(tab: Int) {
        _timelineTab.value = tab
    }

    // Live Feed combined with active filters (For You vs Following)
    @OptIn(ExperimentalCoroutinesApi::class)
    val feeds: StateFlow<List<LuminaPost>> = combine(
        repository.postsFlow,
        _timelineTab
    ) { posts, tab ->
        if (tab == 0) {
            posts // For You: Show all
        } else {
            posts.filter { it.author.isFollowing || it.author.isMe } // Following: Only those followed
        }
    }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Search / Discover
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    val searchedPosts: StateFlow<List<LuminaPost>> = combine(
        repository.postsFlow,
        _searchQuery
    ) { posts, query ->
        if (query.isBlank()) {
            posts
        } else {
            posts.filter {
                it.post.content.contains(query, ignoreCase = true) ||
                it.author.displayName.contains(query, ignoreCase = true) ||
                it.author.username.contains(query, ignoreCase = true)
            }
        }
    }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selected Post (for Thread detail screens)
    private val _selectedPostId = MutableStateFlow<String?>(null)
    val selectedPostId = _selectedPostId.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedPost: StateFlow<LuminaPost?> = _selectedPostId
        .flatMapLatest { id ->
            if (id == null) flowOf(null)
            else repository.getPostByIdFlow(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedPostComments: StateFlow<List<LuminaComment>> = _selectedPostId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList())
            else repository.getCommentsForPostFlow(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectPost(postId: String?) {
        _selectedPostId.value = postId
    }

    // Direct Messages Thread
    private val _selectedConversationUserId = MutableStateFlow<String?>(null)
    val selectedConversationUserId = _selectedConversationUserId.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val activeMessages: StateFlow<List<MessageEntity>> = _selectedConversationUserId
        .flatMapLatest { userId ->
            if (userId == null) flowOf(emptyList())
            else repository.getMessagesWithUserFlow(userId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectConversationUser(userId: String?) {
        _selectedConversationUserId.value = userId
    }

    // Notifications Feed
    val notifications: StateFlow<List<LuminaNotification>> = repository.notificationsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active User Profile screen selection
    private val _profileUserId = MutableStateFlow<String?>("me")
    val profileUserId = _profileUserId.asStateFlow()

    fun selectProfileUser(userId: String?) {
        _profileUserId.value = userId ?: "me"
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val profileUser: StateFlow<UserEntity?> = _profileUserId
        .flatMapLatest { id ->
            val userId = id ?: "me"
            // Simple helper: get individual user from database flow
            repository.postsFlow.map { list ->
                list.map { it.author }.find { it.id == userId } ?: if (userId == "me") {
                    UserEntity(
                        id = "me",
                        username = "nas",
                        displayName = "N. Albert",
                        avatarUrl = "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=150&q=80",
                        bio = "Digital architect & pioneer of the Glass UI standard in Compose. Building future interfaces.",
                        followersCount = 1337,
                        followingCount = 42,
                        isFollowing = false,
                        accentColorHex = "#7F00FF",
                        isMe = true
                    )
                } else null
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val profilePosts: StateFlow<List<LuminaPost>> = combine(
        repository.postsFlow,
        _profileUserId
    ) { posts, profileId ->
        val targetId = profileId ?: "me"
        posts.filter { it.post.authorId == targetId }
    }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI actions
    fun composeNewPost(content: String, imageUrl: String? = null, voiceDurationSec: Int? = null, pollOptions: String? = null) {
        viewModelScope.launch {
            repository.createPost(content, imageUrl, voiceDurationSec, pollOptions)
        }
    }

    fun submitComment(postId: String, text: String) {
        viewModelScope.launch {
            repository.insertComment(postId, text)
        }
    }

    fun toggleLike(postId: String) {
        viewModelScope.launch {
            repository.toggleLike(postId)
        }
    }

    fun toggleRepost(postId: String) {
        viewModelScope.launch {
            repository.toggleRepost(postId)
        }
    }

    fun toggleBookmark(postId: String) {
        viewModelScope.launch {
            repository.toggleBookmark(postId)
        }
    }

    fun voteInPoll(postId: String, optionIndex: Int) {
        viewModelScope.launch {
            repository.voteInPoll(postId, optionIndex)
        }
    }

    fun toggleFollowUser(userId: String) {
        viewModelScope.launch {
            repository.toggleFollow(userId)
        }
    }

    fun sendDirectMessage(recipientId: String, text: String) {
        viewModelScope.launch {
            repository.sendDirectMessage(recipientId, text)
        }
    }

    fun updateUserAccentColor(hex: String) {
        viewModelScope.launch {
            val user = me.value ?: return@launch
            val updated = user.copy(accentColorHex = hex)
            val database = LuminaDatabase.getDatabase(getApplication())
            database.luminaDao().updateUser(updated)
        }
    }
}
