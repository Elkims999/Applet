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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material3.Divider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LuminaPost
import com.example.model.UserEntity
import com.example.ui.components.*
import com.example.ui.viewmodel.LuminaViewModel

@Composable
fun ProfileScreen(
    viewModel: LuminaViewModel,
    onPostClick: (String) -> Unit,
    onProfileClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val activeUser by viewModel.profileUser.collectAsState()
    val posts by viewModel.profilePosts.collectAsState()
    
    val me by viewModel.me.collectAsState()
    val activeAccentHex = me?.accentColorHex ?: "#00F2FE"
    val activeColor = LuminaColors.getAccent(activeAccentHex)

    val currentProfileColor = activeUser?.let { LuminaColors.getAccent(it.accentColorHex, activeColor) } ?: activeColor

    // Colors selector palette for modifying active user accent spectrum
    val accentPalettes = listOf(
        Pair("#00F2FE", "Cyber Cyan"),
        Pair("#FF2592", "Electric Magenta"),
        Pair("#00FF87", "Neon Emerald"),
        Pair("#7F00FF", "Obsidian Violet")
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        if (activeUser == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                MercuryDropletLoader(accentColor = activeColor)
            }
        } else {
            val user = activeUser!!
            LazyColumn(
                contentPadding = PaddingValues(bottom = 90.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Banner element
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        currentProfileColor.copy(alpha = 0.45f),
                                        Color.Transparent
                                    )
                                )
                            )
                            .border(
                                width = 1.dp,
                                color = Color.White.copy(alpha = 0.08f)
                            )
                    ) {
                        Text(
                            text = "GRID COORDINATE ${user.username.uppercase()}",
                            color = Color.White.copy(alpha = 0.15f),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Alignment.Center.let { Modifier.align(it).graphicsLayer(alpha = 0.5f) }
                        )
                    }
                }

                // Profile Avatar + Stats Glass Card
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Offset matching top banner boundary
                        Box(
                            modifier = Modifier
                                .offset(y = (-45).dp)
                        ) {
                            OrbAvatar(
                                avatarUrl = user.avatarUrl,
                                size = 96.dp,
                                accentColor = currentProfileColor
                            )
                        }

                        // Display name handle
                        Text(
                            text = user.displayName,
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.offset(y = (-25).dp)
                        )
                        Text(
                            text = "@${user.username}",
                            color = Color.White.copy(alpha = 0.45f),
                            fontSize = 13.sp,
                            modifier = Modifier.offset(y = (-25).dp)
                        )

                        // Follow info
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(24.dp),
                            modifier = Modifier
                                .offset(y = (-15).dp)
                                .padding(vertical = 4.dp)
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(text = formattedMetric(user.followingCount), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(text = "Following", color = Color.White.copy(alpha = 0.4f), fontSize = 14.sp)
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(text = formattedMetric(user.followersCount), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(text = "Followers", color = Color.White.copy(alpha = 0.4f), fontSize = 14.sp)
                            }
                        }

                        // BIO information Card
                        GlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .offset(y = (-5).dp),
                            accentColor = currentProfileColor
                        ) {
                            Text(
                                text = user.bio,
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 13.sp,
                                lineHeight = 19.sp,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // Action Panel (Unfollow/Follow or SPECTRUM palettes if me)
                        Spacer(modifier = Modifier.height(12.dp))

                        if (user.isMe) {
                            // spectrum customizer
                            GlassCard(
                                modifier = Modifier.fillMaxWidth(),
                                accentColor = currentProfileColor
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.padding(bottom = 12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Palette,
                                        contentDescription = "Palette Core",
                                        tint = currentProfileColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "GLASS SPECTRUM ACCENTS",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 1.5.sp
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    accentPalettes.forEach { (hex, label) ->
                                        val isSelected = me?.accentColorHex == hex
                                        val itemColor = LuminaColors.getAccent(hex)

                                        Box(
                                            modifier = Modifier
                                                .background(
                                                    color = if (isSelected) itemColor.copy(alpha = 0.25f) else Color.Transparent,
                                                    shape = RoundedCornerShape(12.dp)
                                                )
                                                .border(
                                                    width = 1.dp,
                                                    color = if (isSelected) itemColor else Color.White.copy(alpha = 0.1f),
                                                    shape = RoundedCornerShape(12.dp)
                                                )
                                                .clip(RoundedCornerShape(12.dp))
                                                .clickable {
                                                    viewModel.updateUserAccentColor(hex)
                                                }
                                                .padding(horizontal = 10.dp, vertical = 8.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Box(modifier = Modifier.size(12.dp).background(itemColor, CircleShape))
                                                Text(
                                                    text = label.split(" ").last(),
                                                    color = if (isSelected) Color.White else Color.White.copy(alpha = 0.4f),
                                                    fontSize = 11.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            // Follow/Unfollow toggle
                            val isFollowing = user.isFollowing
                            GlassButton(
                                onClick = { viewModel.toggleFollowUser(user.id) },
                                accentColor = currentProfileColor,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = if (isFollowing) "RESONATING (UNFOLLOW)" else "CONNECT (FOLLOW)",
                                    color = Color.Black,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 12.sp,
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                    }
                }

                // Header tabs list
                item {
                    Text(
                        text = "TRANSMISSION FEED",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                    )
                }

                // List items posted by user
                if (posts.isEmpty()) {
                    item {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth().height(140.dp)
                        ) {
                            Text(
                                text = "Zero posts matching coordinate.",
                                color = Color.White.copy(alpha = 0.35f),
                                fontSize = 13.sp
                            )
                        }
                    }
                } else {
                    items(posts, key = { it.post.id }) { item ->
                        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                            PostCardItem(
                                item = item,
                                activeColor = activeColor,
                                onPostClick = { onPostClick(item.post.id) },
                                onProfileClick = { onProfileClick(item.author.id) },
                                onLike = { viewModel.toggleLike(item.post.id) },
                                onRepost = { viewModel.toggleRepost(item.post.id) },
                                onBookmark = { viewModel.toggleBookmark(item.post.id) },
                                onVote = { optionIdx -> viewModel.voteInPoll(item.post.id, optionIdx) }
                            )
                        }
                    }
                }
            }
        }
    }
}

// Numerical formatter helper
fun formattedMetric(count: Int): String {
    return if (count >= 1000) {
        val thousands = count.toFloat() / 1000f
        String.format("%.1fk", thousands)
    } else {
        count.toString()
    }
}
