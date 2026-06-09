package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.Divider
import kotlinx.coroutines.launch
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.*
import com.example.ui.viewmodel.LuminaViewModel

@Composable
fun MainHostScreen(
    viewModel: LuminaViewModel,
    modifier: Modifier = Modifier
) {
    var showOnboarding by remember { mutableStateOf(true) }
    var currentTab by remember { mutableStateOf(0) } // 0=Home, 1=Search, 2=DMs, 3=Notifications, 4=Profile
    var showComposer by remember { mutableStateOf(false) }

    val me by viewModel.me.collectAsState()
    val activeAccentHex = me?.accentColorHex ?: "#00F2FE"
    val activeColor = LuminaColors.getAccent(activeAccentHex)

    val selectedPostId by viewModel.selectedPostId.collectAsState()

    // Smooth root transition: Onboarding compiled with Crossfade
    Crossfade(
        targetState = showOnboarding,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "OnboardingCrossfade"
    ) { onboarding ->
        if (onboarding) {
            OnboardingScreen(
                viewModel = viewModel,
                onComplete = { showOnboarding = false }
            )
        } else {
            LuminaBackground {
                Scaffold(
                    containerColor = Color.Transparent,
                    bottomBar = {
                        // Display bottom bar ONLY if not viewing sub Detail Screen
                        if (selectedPostId == null) {
                            GlassBottomDock(
                                activeColor = activeColor,
                                selectedTab = currentTab,
                                onTabSelected = { tab ->
                                    if (tab == 4) {
                                        viewModel.selectProfileUser("me")
                                    }
                                    currentTab = tab
                                },
                                onComposeTrigger = { showComposer = true }
                            )
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = if (selectedPostId == null) 80.dp else 0.dp) // Offset for bottom dock
                    ) {
                        // Core Stack Screens
                        AnimatedContent(
                            targetState = if (selectedPostId != null) "Detail" else currentTab.toString(),
                            transitionSpec = {
                                fadeIn() togetherWith fadeOut()
                            },
                            label = "PageSwitcher"
                        ) { viewState ->
                            when (viewState) {
                                "Detail" -> {
                                    DetailScreen(
                                        viewModel = viewModel,
                                        onBack = { viewModel.selectPost(null) },
                                        onProfileClick = { authorId ->
                                            viewModel.selectPost(null)
                                            viewModel.selectProfileUser(authorId)
                                            currentTab = 4 // switch to profile
                                        }
                                    )
                                }
                                "0" -> {
                                    HomeScreen(
                                        viewModel = viewModel,
                                        onPostClick = { postId -> viewModel.selectPost(postId) },
                                        onProfileClick = { authorId ->
                                            viewModel.selectProfileUser(authorId)
                                            currentTab = 4 // Switch to profile
                                        }
                                    )
                                }
                                "1" -> {
                                    ExploreScreen(
                                        viewModel = viewModel,
                                        onPostClick = { postId -> viewModel.selectPost(postId) },
                                        onProfileClick = { authorId ->
                                            viewModel.selectProfileUser(authorId)
                                            currentTab = 4
                                        }
                                    )
                                }
                                "2" -> {
                                    DirectMessagesScreen(viewModel = viewModel)
                                }
                                "3" -> {
                                    NotificationsScreen(
                                        viewModel = viewModel,
                                        onPostClick = { postId -> viewModel.selectPost(postId) },
                                        onProfileClick = { authorId ->
                                            viewModel.selectProfileUser(authorId)
                                            currentTab = 4
                                        }
                                    )
                                }
                                "4" -> {
                                    ProfileScreen(
                                        viewModel = viewModel,
                                        onPostClick = { postId -> viewModel.selectPost(postId) },
                                        onProfileClick = { authorId ->
                                            viewModel.selectProfileUser(authorId)
                                            currentTab = 4
                                        }
                                    )
                                }
                            }
                        }

                        // Expanding Liquid Post Composer screen bubble
                        AnimatedVisibility(
                            visible = showComposer,
                            enter = fadeIn() + expandIn(),
                            exit = fadeOut() + shrinkOut()
                        ) {
                            ComposerScreen(
                                viewModel = viewModel,
                                onClose = { showComposer = false }
                            )
                        }
                    }
                }
            }
        }
    }
}

// tactile bottom glassy doc
@Composable
fun BoxScope.GlassBottomDock(
    activeColor: Color,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    onComposeTrigger: () -> Unit
) {
    Box(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .fillMaxWidth()
            .padding(16.dp)
            .navigationBarsPadding()
    ) {
        // High-end glass capsule floating layout
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp) // Updated to match HTML h-[68px]
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.08f), // elegant bg-white/8 backdrop
                            Color.White.copy(alpha = 0.03f)
                        )
                    ),
                    shape = RoundedCornerShape(34.dp) // rounded-[34px]
                )
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.16f), // border border-white/15
                    shape = RoundedCornerShape(34.dp)
                )
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Tab index 0: Feed Home
            DockItem(
                icon = if (selectedTab == 0) Icons.Filled.Home else Icons.Outlined.Home,
                label = "Home",
                isSelected = selectedTab == 0,
                activeColor = activeColor,
                onClick = { onTabSelected(0) }
            )

            // Tab index 1: Search Explore
            DockItem(
                icon = Icons.Default.Search,
                label = "Explore",
                isSelected = selectedTab == 1,
                activeColor = activeColor,
                onClick = { onTabSelected(1) }
            )

            // Center: Circular liquid shiny composing trigger bubble with beautiful gradient and drop shadow
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .shadow(
                        elevation = 16.dp,
                        shape = CircleShape,
                        ambientColor = Color(0xFF4F46E5).copy(alpha = 0.4f), // Indigo shadow to match HTML shadow-[0_0_30px_rgba(79,70,229,0.4)]
                        spotColor = Color(0xFF4F46E5).copy(alpha = 0.5f)
                    )
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF2563EB), // Blue 600
                                Color(0xFF6366F1), // Indigo 500
                                Color(0xFF8B5CF6)  // Purple 500
                            )
                        ),
                        shape = CircleShape
                    )
                    .border(1.2.dp, Color.White.copy(alpha = 0.3f), CircleShape) // border border-white/30
                    .clip(CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = androidx.compose.material3.ripple(color = Color.White),
                        onClick = onComposeTrigger
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "New Transmission",
                    tint = Color.White, // White '+' to match HTML
                    modifier = Modifier.size(26.dp)
                )
            }

            // Tab index 2: Messages
            DockItem(
                icon = if (selectedTab == 2) Icons.Filled.Mail else Icons.Outlined.Mail,
                label = "Chats",
                isSelected = selectedTab == 2,
                activeColor = activeColor,
                onClick = { onTabSelected(2) }
            )

            // Tab index 3: Notifications / Alerts
            DockItem(
                icon = if (selectedTab == 3) Icons.Filled.Notifications else Icons.Outlined.Notifications,
                label = "Alerts",
                isSelected = selectedTab == 3,
                activeColor = activeColor,
                onClick = { onTabSelected(3) }
            )
        }
    }
}

// tactical action dock btn
@Composable
fun RowScope.DockItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSelected: Boolean,
    activeColor: Color,
    onClick: () -> Unit
) {
    val scale = remember { androidx.compose.animation.core.Animatable(1f) }
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    scope.launch {
                        scale.animateTo(0.85f, spring(stiffness = Spring.StiffnessHigh))
                        scale.animateTo(1.15f, spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessMedium))
                        scale.animateTo(1f, spring(stiffness = Spring.StiffnessLow))
                    }
                    onClick()
                }
            )
            .graphicsLayer(scaleX = scale.value, scaleY = scale.value),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) activeColor else Color.White.copy(alpha = 0.4f),
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                color = if (isSelected) activeColor else Color.White.copy(alpha = 0.4f),
                fontSize = 10.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}
