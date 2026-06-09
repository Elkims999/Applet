package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LuminaNotification
import com.example.ui.components.*
import com.example.ui.viewmodel.LuminaViewModel

@Composable
fun NotificationsScreen(
    viewModel: LuminaViewModel,
    onPostClick: (String) -> Unit,
    onProfileClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val list by viewModel.notifications.collectAsState()
    
    val me by viewModel.me.collectAsState()
    val activeAccentHex = me?.accentColorHex ?: "#00F2FE"
    val activeColor = LuminaColors.getAccent(activeAccentHex)

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "RESONANCE MATRIX",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 2.sp,
            modifier = Modifier.padding(vertical = 16.dp, horizontal = 4.dp)
        )

        Text(
            text = "LIVE INTERACTION INDICES",
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
        )

        if (list.isEmpty()) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No resonance metrics detected.",
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 13.sp
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 90.dp)
            ) {
                items(list, key = { it.notification.id }) { item ->
                    NotificationItemRow(
                        item = item,
                        activeColor = activeColor,
                        onPostClick = onPostClick,
                        onProfileClick = onProfileClick
                    )
                }
            }
        }
    }
}

// Sparkly, detailed Notification vector Card row
@Composable
fun NotificationItemRow(
    item: LuminaNotification,
    activeColor: Color,
    onPostClick: (String) -> Unit,
    onProfileClick: (String) -> Unit
) {
    val srcColor = LuminaColors.getAccent(item.sourceUser.accentColorHex, activeColor)
    
    val (icon, tint, label) = when (item.notification.type) {
        "LIKE" -> Triple(Icons.Default.Favorite, LuminaColors.Magenta, "liked your transmission")
        "REPOST" -> Triple(Icons.Default.Repeat, LuminaColors.Cyan, "reposted your broadcast")
        "REPLY" -> Triple(Icons.Default.Comment, LuminaColors.Violet, "responded to your transmission")
        "FOLLOW" -> Triple(Icons.Default.PersonAdd, LuminaColors.Emerald, "is now resonating with you")
        else -> Triple(Icons.Default.Notifications, activeColor, "transmitted signal alerts")
    }

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        accentColor = srcColor,
        onClick = {
            if (item.notification.relatedPostId != null) {
                onPostClick(item.notification.relatedPostId)
            } else {
                onProfileClick(item.sourceUser.id)
            }
        }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Signal type indicator
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(tint.copy(alpha = 0.15f), CircleShape)
                    .border(1.dp, tint.copy(alpha = 0.4f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = "Signal Vector",
                    tint = tint,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Information details
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OrbAvatar(
                        avatarUrl = item.sourceUser.avatarUrl,
                        size = 28.dp,
                        accentColor = srcColor,
                        onClick = { onProfileClick(item.sourceUser.id) }
                    )
                    Text(
                        text = item.sourceUser.displayName,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onProfileClick(item.sourceUser.id) }
                    )
                }

                Text(
                    text = label,
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )

                // Optional snippet link preview
                if (item.relatedPost != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = item.relatedPost.content,
                        color = Color.White.copy(alpha = 0.35f),
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
