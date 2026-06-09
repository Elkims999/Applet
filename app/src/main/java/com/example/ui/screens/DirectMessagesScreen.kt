package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.components.*
import com.example.ui.viewmodel.LuminaViewModel

@Composable
fun DirectMessagesScreen(
    viewModel: LuminaViewModel,
    modifier: Modifier = Modifier
) {
    val activeChatUserId by viewModel.selectedConversationUserId.collectAsState()
    val activeMessages by viewModel.activeMessages.collectAsState()

    val me by viewModel.me.collectAsState()
    val activeAccentHex = me?.accentColorHex ?: "#00F2FE"
    val activeColor = LuminaColors.getAccent(activeAccentHex)

    // Hardcoded seed users for contacts directory
    val contacts = listOf(
        UserEntity(
            id = "aeroglass",
            username = "aeroglass",
            displayName = "Aero Glass",
            avatarUrl = "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=150&q=80",
            bio = "", followersCount = 0, followingCount = 0, isFollowing = true, accentColorHex = "#00F2FE"
        ),
        UserEntity(
            id = "novaflow",
            username = "novaflow",
            displayName = "Nova Flow",
            avatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=150&q=80",
            bio = "", followersCount = 0, followingCount = 0, isFollowing = false, accentColorHex = "#FF2592"
        ),
        UserEntity(
            id = "zephyr",
            username = "zephyr",
            displayName = "Zephyr Obsidian",
            avatarUrl = "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=150&q=80",
            bio = "", followersCount = 0, followingCount = 0, isFollowing = true, accentColorHex = "#00FF87"
        )
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        // Animation container to slide between Chat List and Active Thread Chat Room
        AnimatedContent(
            targetState = activeChatUserId,
            transitionSpec = {
                if (targetState != null) {
                    // Slide chat room in
                    slideInHorizontally { width -> width } + fadeIn() togetherWith
                    slideOutHorizontally { width -> -width } + fadeOut()
                } else {
                    // Slide list back in
                    slideInHorizontally { width -> -width } + fadeIn() togetherWith
                    slideOutHorizontally { width -> width } + fadeOut()
                }
            },
            label = "DirectMessagesNavigation"
        ) { targetUserId ->
            if (targetUserId == null) {
                // PART 1: Contacts & Chat Previews directory
                Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                    Text(
                        text = "DIRECT TRANSMISSIONS",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp,
                        modifier = Modifier.padding(vertical = 16.dp, horizontal = 4.dp)
                    )

                    // Active Contacts horizontal Orb scroll row
                    Text(
                        text = "RESONANCE PIPELINES",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        contacts.forEach { user ->
                            val uColor = LuminaColors.getAccent(user.accentColorHex, activeColor)
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.clickable { viewModel.selectConversationUser(user.id) }
                            ) {
                                OrbAvatar(
                                    avatarUrl = user.avatarUrl,
                                    size = 56.dp,
                                    accentColor = uColor
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = user.displayName.split(" ").first(),
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    Divider(color = Color.White.copy(alpha = 0.08f), thickness = 1.dp, modifier = Modifier.padding(bottom = 16.dp))

                    Text(
                        text = "ACTIVE MESSAGES LOGS",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
                    )

                    // Vertical Inbox logs
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(contacts) { contact ->
                            val contactColor = LuminaColors.getAccent(contact.accentColorHex, activeColor)
                            InboxRowPreview(
                                contact = contact,
                                accentColor = contactColor,
                                onClick = { viewModel.selectConversationUser(contact.id) }
                            )
                        }
                    }
                }
            } else {
                // PART 2: Chat Room Thread Room layout
                val contact = contacts.find { it.id == targetUserId } ?: contacts.first()
                val contactColor = LuminaColors.getAccent(contact.accentColorHex, activeColor)
                var currentInputMessage by remember { mutableStateOf("") }

                Column(modifier = Modifier.fillMaxSize()) {
                    // Chat header back bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { viewModel.selectConversationUser(null) }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(6.dp))

                        OrbAvatar(
                            avatarUrl = contact.avatarUrl,
                            size = 38.dp,
                            accentColor = contactColor
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = contact.displayName,
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Quantum tunnel: active",
                                color = contactColor.copy(alpha = 0.7f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Divider(color = Color.White.copy(alpha = 0.08f), thickness = 1.dp)

                    // Chat bubbles list
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        if (activeMessages.isEmpty()) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Start crystalline conversation.",
                                    color = Color.White.copy(alpha = 0.4f),
                                    fontSize = 14.sp
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                contentPadding = PaddingValues(vertical = 16.dp)
                            ) {
                                items(activeMessages, key = { it.id }) { msg ->
                                    val isMe = msg.senderId == "me"
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        ChatBubbleItem(
                                            messageEntity = msg,
                                            isMe = isMe,
                                            activeColor = if (isMe) activeColor else contactColor
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Chat input dock
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .navigationBarsPadding(),
                        accentColor = contactColor,
                        cornerRadius = 18.dp
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                GlassInput(
                                    value = currentInputMessage,
                                    onValueChange = { currentInputMessage = it },
                                    placeholder = "Transmit secure message...",
                                    accentColor = contactColor,
                                    trailingIcon = {
                                        IconButton(
                                            onClick = {
                                                if (currentInputMessage.isNotBlank()) {
                                                    viewModel.sendDirectMessage(contact.id, currentInputMessage)
                                                    currentInputMessage = ""
                                                }
                                            },
                                            modifier = Modifier
                                                .size(36.dp)
                                                .background(contactColor.copy(alpha = 0.2f), CircleShape)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Send,
                                                contentDescription = "Send Direct",
                                                tint = contactColor,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Gorgeous Inbox Item Row Preview
@Composable
fun InboxRowPreview(
    contact: UserEntity,
    accentColor: Color,
    onClick: () -> Unit
) {
    val simulatedSubtext = when (contact.id) {
        "aeroglass" -> "Fascinating perspective! The caustics in our..."
        "novaflow" -> "Dynamic interfaces require movement! Love this..."
        "zephyr" -> "Obsidian light scatters nicely at 45 degrees..."
        else -> "Secure grid connection open."
    }

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        accentColor = accentColor,
        onClick = onClick,
        cornerRadius = 18.dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            OrbAvatar(
                avatarUrl = contact.avatarUrl,
                size = 46.dp,
                accentColor = trackingOrSignalColor(contact)
            )

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = contact.displayName,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "12m ago",
                        color = Color.White.copy(alpha = 0.35f),
                        fontSize = 11.sp
                    )
                }
                Text(
                    text = simulatedSubtext,
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

// Sparkly, physical glass Chat bubble item
@Composable
fun ColumnScope.ChatBubbleItem(
    messageEntity: MessageEntity,
    isMe: Boolean,
    activeColor: Color
) {
    val alignment = if (isMe) Alignment.End else Alignment.Start
    val shape = if (isMe) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 2.dp)
    } else {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 2.dp, bottomEnd = 16.dp)
    }

    val bubbleBg = if (isMe) {
        Brush.linearGradient(
            colors = listOf(activeColor.copy(alpha = 0.45f), activeColor.copy(alpha = 0.15f))
        )
    } else {
        Brush.linearGradient(
            colors = listOf(Color(0x35FFFFFF), Color(0x0FFFFFFF))
        )
    }

    Box(
        modifier = Modifier
            .align(alignment)
            .widthIn(max = 280.dp)
            .background(bubbleBg, shape)
            .border(
                width = 1.dp,
                color = if (isMe) activeColor.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.12f),
                shape = shape
            )
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Column {
            Text(
                text = messageEntity.text,
                color = Color.White,
                fontSize = 14.sp,
                lineHeight = 19.sp
            )
            Text(
                text = "19:45",
                color = Color.White.copy(alpha = 0.3f),
                fontSize = 9.sp,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
            )
        }
    }
}

// Simple color helper
fun trackingOrSignalColor(user: UserEntity): Color {
    return try {
        Color(android.graphics.Color.parseColor(user.accentColorHex))
    } catch (e: Exception) {
        Color(0xFF00F2FE)
    }
}
