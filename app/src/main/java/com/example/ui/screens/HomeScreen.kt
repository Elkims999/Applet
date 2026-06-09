package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.model.LuminaPost
import com.example.model.PostEntity
import com.example.model.UserEntity
import com.example.ui.components.*
import com.example.ui.viewmodel.LuminaViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.border
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.interaction.MutableInteractionSource
import kotlin.math.sin

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    viewModel: LuminaViewModel,
    onPostClick: (String) -> Unit,
    onProfileClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val posts by viewModel.feeds.collectAsState()
    val activeTab by viewModel.timelineTab.collectAsState()
    val me by viewModel.me.collectAsState()

    val activeAccentHex = me?.accentColorHex ?: "#00F2FE"
    val activeColor = LuminaColors.getAccent(activeAccentHex)

    // Pull to refresh simulation variables
    var isRefreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val rotationState = remember { Animatable(0f) }

    val triggerRefresh = {
        scope.launch {
            isRefreshing = true
            // Spin like crazy mimicking crystalline liquid reform
            rotationState.animateTo(
                targetValue = 360f,
                animationSpec = tween(1200, easing = FastOutSlowInEasing)
            )
            delay(400)
            rotationState.snapTo(0f)
            isRefreshing = false
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        // App top header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OrbAvatar(
                avatarUrl = me?.avatarUrl ?: "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=150&q=80",
                size = 40.dp,
                accentColor = activeColor,
                onClick = { onProfileClick("me") }
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Lumina",
                    style = androidx.compose.ui.text.TextStyle(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF60A5FA), // Blue 400
                                Color(0xFFA5B4FC), // Indigo 300
                                Color(0xFFC084FC)  // Purple 400
                            )
                        )
                    ),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "Cosmic sparkle",
                    tint = Color(0xFFA5B4FC),
                    modifier = Modifier
                        .size(18.dp)
                        .rotate(rotationState.value)
                        .clickable { triggerRefresh() }
                )
            }

            IconButton(
                onClick = { triggerRefresh() },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Simulated mercury refresh",
                    tint = activeColor.copy(alpha = 0.8f)
                )
            }
        }

        // Sliding tabs
        LiquidTabBar(
            tabs = listOf("For You", "Following"),
            selectedTabIndex = activeTab,
            onTabSelected = { viewModel.setTimelineTab(it) },
            accentColor = activeColor,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
        )

        // Refresh loader visualizer (mercury droplet ripple)
        AnimatedVisibility(
            visible = isRefreshing,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                contentAlignment = Alignment.Center
            ) {
                MercuryDropletLoader(accentColor = activeColor)
            }
        }

        // Home timeline list
        Box(modifier = Modifier.weight(1f)) {
            if (posts.isEmpty()) {
                EmptyStateView(accentColor = activeColor)
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 90.dp, start = 16.dp, end = 16.dp, top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(posts, key = { it.post.id }) { item ->
                        PostCardItem(
                            item = item,
                            activeColor = activeColor,
                            onPostClick = { onPostClick(item.post.id) },
                            onProfileClick = { onProfileClick(item.author.id) },
                            onLike = { viewModel.toggleLike(item.post.id) },
                            onRepost = { viewModel.toggleRepost(item.post.id) },
                            onBookmark = { viewModel.toggleBookmark(item.post.id) },
                            onVote = { optionIdx -> viewModel.voteInPoll(item.post.id, optionIdx) },
                            modifier = Modifier.animateItemPlacement()
                        )
                    }
                }
            }
        }
    }
}

// Sparkly, morphing mercury droplet refresher indicator
@Composable
fun MercuryDropletLoader(accentColor: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "MercuryLoader")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = Math.PI.toFloat() * 2,
        animationSpec = infiniteRepeatable(tween(1000, easing = LinearEasing), RepeatMode.Restart),
        label = "phase"
    )

    Canvas(modifier = Modifier.size(60.dp, 30.dp)) {
        val radiusMultiplier = 1f + sin(phase) * 0.25f
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(accentColor, accentColor.copy(alpha = 0.1f)),
                center = center,
                radius = 16.dp.toPx() * radiusMultiplier
            ),
            center = center,
            radius = 12.dp.toPx() * radiusMultiplier
        )

        // Glass glare spotlight reflection
        drawCircle(
            color = Color.White.copy(alpha = 0.5f),
            center = Offset(center.x - 3.dp.toPx(), center.y - 3.dp.toPx()),
            radius = 3.dp.toPx()
        )
    }
}

// Comprehensive interactive single Post Card structured styling
@Composable
fun PostCardItem(
    item: LuminaPost,
    activeColor: Color,
    onPostClick: () -> Unit,
    onProfileClick: () -> Unit,
    onLike: () -> Unit,
    onRepost: () -> Unit,
    onBookmark: () -> Unit,
    onVote: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val pAccColor = LuminaColors.getAccent(item.author.accentColorHex, activeColor)

    GlassCard(
        modifier = modifier.fillMaxWidth(),
        accentColor = pAccColor,
        onClick = onPostClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            OrbAvatar(
                avatarUrl = item.author.avatarUrl,
                size = 48.dp,
                accentColor = pAccColor,
                onClick = onProfileClick
            )

            Column(modifier = Modifier.weight(1f)) {
                // Name handle bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.clickable(onClick = onProfileClick)) {
                        Text(
                            text = item.author.displayName,
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "@${item.author.username}",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 12.sp
                        )
                    }

                    // Simulated 3D orb verification indicator if popular
                    if (item.author.followersCount > 10000) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .background(pAccColor, CircleShape)
                                .border(1.dp, Color.White, CircleShape)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Post Content
                Text(
                    text = item.post.content,
                    color = Color.White,
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    maxLines = 8,
                    overflow = TextOverflow.Ellipsis
                )

                // Optional Voice soundwave visualization component
                if (item.post.voiceDurationSec != null) {
                    Spacer(modifier = Modifier.height(14.dp))
                    VoicePlayerGlass(
                        durationSec = item.post.voiceDurationSec,
                        accentColor = pAccColor
                    )
                }

                // Optional Poll Card list
                if (item.post.pollOptions != null) {
                    Spacer(modifier = Modifier.height(14.dp))
                    PollGlassSelector(
                        postId = item.post.id,
                        optionsRaw = item.post.pollOptions,
                        votesRaw = item.post.pollVotes ?: "",
                        chosenIndex = item.post.chosenPollOptionIndex,
                        onVoteSelected = onVote,
                        accentColor = pAccColor
                    )
                }

                // Optional attached Media Graphic
                if (item.post.imageUrl != null) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .shadow(12.dp, RoundedCornerShape(16.dp))
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(item.post.imageUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Post Media Graphic",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )

                        // Glass shiny refraction edge overlay
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawLine(
                                color = Color.White.copy(alpha = 0.25f),
                                start = Offset(0f, 0f),
                                end = Offset(size.width, size.height),
                                strokeWidth = 2f
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = Color.White.copy(alpha = 0.08f), thickness = 1.dp)
                Spacer(modifier = Modifier.height(10.dp))

                // Action controls layout (Like, Comment, Repost, Bookmark)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Repost Action Node
                    ActionIconBtn(
                        icon = if (item.post.isReposted) Icons.Filled.Repeat else Icons.Outlined.Repeat,
                        count = item.post.repostsCount,
                        color = if (item.post.isReposted) pAccColor else Color.White.copy(alpha = 0.45f),
                        onClick = onRepost
                    )

                    // Like Action Node (using custom glowing core spark)
                    ActionIconBtn(
                        icon = if (item.post.isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        count = item.post.likesCount,
                        color = if (item.post.isLiked) LuminaColors.Magenta else Color.White.copy(alpha = 0.45f),
                        onClick = onLike
                    )

                    // Comments Readout Node
                    ActionIconBtn(
                        icon = Icons.Outlined.ModeComment,
                        count = item.post.repliesCount,
                        color = Color.White.copy(alpha = 0.45f),
                        onClick = onPostClick
                    )

                    // Save Bookmark Node
                    ActionIconBtn(
                        icon = if (item.post.isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                        count = item.post.bookmarkCount,
                        color = if (item.post.isBookmarked) LuminaColors.Emerald else Color.White.copy(alpha = 0.45f),
                        onClick = onBookmark
                    )
                }
            }
        }
    }
}

// Reusable micro-animated small action key link item
@Composable
fun ActionIconBtn(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    count: Int,
    color: Color,
    onClick: () -> Unit
) {
    val scale = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
            .graphicsLayer(scaleX = scale.value, scaleY = scale.value)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    scope.launch {
                        scale.animateTo(0.82f, spring(stiffness = Spring.StiffnessHigh))
                        scale.animateTo(1.15f, spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessMedium))
                        scale.animateTo(1f, spring(stiffness = Spring.StiffnessLow))
                    }
                    onClick()
                }
            )
            .padding(6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "Interact link",
            tint = color,
            modifier = Modifier.size(19.dp)
        )
        if (count >= 0) {
            Text(
                text = count.toString(),
                color = color,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// Dynamic Animated Voice wave display component
@Composable
fun VoicePlayerGlass(durationSec: Int, accentColor: Color) {
    var isPlaying by remember { mutableStateOf(false) }
    val infiniteTransition = rememberInfiniteTransition(label = "VoicewavePulse")

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutLinearInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wave_osc"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(Color(0x11FFFFFF), RoundedCornerShape(12.dp))
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        IconButton(
            onClick = { isPlaying = !isPlaying },
            modifier = Modifier
                .size(28.dp)
                .background(accentColor.copy(alpha = 0.25f), CircleShape)
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                contentDescription = "Liquid sound control",
                tint = accentColor,
                modifier = Modifier.size(16.dp)
            )
        }

        // Voice waves bars canvas
        Canvas(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(0.6f)
        ) {
            val barsCount = 28
            val spacing = size.width / barsCount
            val barWidth = spacing * 0.5f
            
            for (i in 0 until barsCount) {
                // Draw cool mathematical curves
                val x = i * spacing + barWidth
                var hFactor = sin(i.toFloat() * 0.3f) * 0.4f + 0.6f
                if (isPlaying) {
                    hFactor *= (0.5f + (sin((i + pulseScale * 10f)) * 0.4f))
                }
                val barHeight = size.height * hFactor
                
                drawLine(
                    color = if (isPlaying) accentColor else Color.White.copy(alpha = 0.3f),
                    start = Offset(x, size.height / 2 - barHeight / 2),
                    end = Offset(x, size.height / 2 + barHeight / 2),
                    strokeWidth = barWidth
                )
            }
        }

        Text(
            text = "0:${durationSec.toString().padStart(2, '0')}",
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

// Gorgeous responsive Glass Poll layout card
@Composable
fun PollGlassSelector(
    postId: String,
    optionsRaw: String,
    votesRaw: String,
    chosenIndex: Int?,
    onVoteSelected: (Int) -> Unit,
    accentColor: Color
) {
    val options = optionsRaw.split(",")
    val votes = votesRaw.split(",").map { it.toIntOrNull() ?: 0 }
    val totalVotes = votes.sum().coerceAtLeast(1)
    val hasVoted = chosenIndex != null

    val interactionSource = remember { MutableInteractionSource() }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        options.forEachIndexed { index, option ->
            val optVoteCount = votes.getOrElse(index) { 0 }
            val votePercent = (optVoteCount * 100) / totalVotes

            // Live progress state
            val animatedPercent by animateFloatAsState(
                targetValue = if (hasVoted) votePercent.toFloat() / 100f else 0.0f,
                animationSpec = spring(stiffness = Spring.StiffnessLow),
                label = "poll_fill_$index"
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .background(Color(0x0EFFFFFF), RoundedCornerShape(12.dp))
                    .border(
                        width = if (chosenIndex == index) 1.5.dp else 1.dp,
                        color = if (chosenIndex == index) accentColor else Color.White.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(
                        enabled = !hasVoted,
                        onClick = { onVoteSelected(index) }
                    )
            ) {
                // Poll fill gradient
                if (hasVoted) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(animatedPercent)
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        accentColor.copy(alpha = 0.35f),
                                        accentColor.copy(alpha = 0.08f)
                                    )
                                )
                            )
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = option,
                            color = Color.White.copy(alpha = if (hasVoted) 0.9f else 0.7f),
                            fontSize = 13.sp,
                            fontWeight = if (chosenIndex == index) FontWeight.Bold else FontWeight.Medium
                        )
                        if (chosenIndex == index) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Active vote",
                                tint = accentColor,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    if (hasVoted) {
                        Text(
                            text = "$votePercent%",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// Sparkly aesthetic Empty State placeholder
@Composable
fun EmptyStateView(accentColor: Color) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(30.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CloudQueue,
            contentDescription = "Void sign",
            tint = accentColor.copy(alpha = 0.25f),
            modifier = Modifier.size(72.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Grid Silhouette Empty",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Text(
            text = "No liquid transmissions observed in this sector yet. Check your connections or trigger a mercury refresh.",
            color = Color.White.copy(alpha = 0.45f),
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}
