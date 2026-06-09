package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LuminaComment
import com.example.model.LuminaPost
import com.example.ui.components.*
import com.example.ui.viewmodel.LuminaViewModel

@Composable
fun DetailScreen(
    viewModel: LuminaViewModel,
    onBack: () -> Unit,
    onProfileClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val activePost by viewModel.selectedPost.collectAsState()
    val comments by viewModel.selectedPostComments.collectAsState()
    
    var replyText by remember { mutableStateOf("") }
    
    val me by viewModel.me.collectAsState()
    val activeAccentHex = me?.accentColorHex ?: "#00F2FE"
    val activeColor = LuminaColors.getAccent(activeAccentHex)

    val pColor = activePost?.let { LuminaColors.getAccent(it.author.accentColorHex, activeColor) } ?: activeColor

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        // High-end header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Return",
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "TRANSMISSION THREAD",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
        }

        Box(modifier = Modifier.weight(1f)) {
            if (activePost == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    MercuryDropletLoader(accentColor = activeColor)
                }
            } else {
                val post = activePost!!
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 1. Core Parent Post display
                    item {
                        PostCardItem(
                            item = post,
                            activeColor = activeColor,
                            onPostClick = {},
                            onProfileClick = { onProfileClick(post.author.id) },
                            onLike = { viewModel.toggleLike(post.post.id) },
                            onRepost = { viewModel.toggleRepost(post.post.id) },
                            onBookmark = { viewModel.toggleBookmark(post.post.id) },
                            onVote = { optionIdx -> viewModel.voteInPoll(post.post.id, optionIdx) }
                        )
                    }

                    // Separation label
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp, horizontal = 4.dp)
                        ) {
                            Text(
                                text = "Transmitting Responses (${comments.size})",
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    // 2. Comments connected by beautiful glassy lines
                    items(comments, key = { it.comment.id }) { reply ->
                        CommentItemRow(
                            item = reply,
                            activeColor = activeColor,
                            onProfileClick = { onProfileClick(reply.author.id) }
                        )
                    }
                }
            }
        }

        // Reply Input Dock
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .navigationBarsPadding(),
            accentColor = pColor,
            cornerRadius = 18.dp
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OrbAvatar(
                    avatarUrl = me?.avatarUrl ?: "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=150&q=80",
                    size = 36.dp,
                    accentColor = activeColor
                )

                Box(modifier = Modifier.weight(1f)) {
                    GlassInput(
                        value = replyText,
                        onValueChange = { replyText = it },
                        placeholder = "Transmit reply details...",
                        accentColor = pColor,
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    if (replyText.isNotBlank()) {
                                        activePost?.let { p ->
                                            viewModel.submitComment(p.post.id, replyText)
                                            replyText = ""
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(pColor.copy(alpha = 0.2f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription = "Send",
                                    tint = pColor,
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

// Custom threaded replying row with glass connector lines
@Composable
fun CommentItemRow(
    item: LuminaComment,
    activeColor: Color,
    onProfileClick: () -> Unit
) {
    val pAccColor = LuminaColors.getAccent(item.author.accentColorHex, activeColor)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Left Column representing vertical avatars + Custom Canvas Thread lines
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(48.dp)
        ) {
            OrbAvatar(
                avatarUrl = item.author.avatarUrl,
                size = 40.dp,
                accentColor = pAccColor,
                onClick = onProfileClick
            )
            
            // Connecting thread canvas
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(30.dp)
            ) {
                // Glass-morphic connecting path
                drawLine(
                    color = pAccColor.copy(alpha = 0.25f),
                    start = Offset(center.x, 0f),
                    end = Offset(center.x, size.height),
                    strokeWidth = 2.dp.toPx()
                )
            }
        }

        // Comment content card
        GlassCard(
            modifier = Modifier.weight(1f),
            accentColor = pAccColor,
            cornerRadius = 16.dp
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.clickable(onClick = onProfileClick)) {
                        Text(
                            text = item.author.displayName,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "@${item.author.username}",
                            color = Color.White.copy(alpha = 0.4f),
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = item.comment.content,
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }
        }
    }
}
