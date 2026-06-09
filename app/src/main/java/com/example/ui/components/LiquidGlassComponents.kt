package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.animation.animateColorAsState
import kotlinx.coroutines.launch
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlin.math.sin

// Theme color definitions for Liquid Glass
object LuminaColors {
    val DeepSpace = Color(0xFF030406) // Elegant Dark deep pitch-black
    val GlassBase = Color(0x14FFFFFF) // white/8 backdrop
    val GlassBorder = Color(0x33FFFFFF) // white/20 border
    
    // Iridescent Accents
    val Cyan = Color(0xFF00F2FE)
    val Magenta = Color(0xFFFF2592)
    val Emerald = Color(0xFF00FF87)
    val Violet = Color(0xFF7F00FF)
    
    val IridescentGrid = listOf(Cyan, Magenta, Violet, Emerald, Cyan)
    
    fun getAccent(hex: String, defaultColor: Color = Cyan): Color {
        return try {
            Color(android.graphics.Color.parseColor(hex))
        } catch (e: Exception) {
            defaultColor
        }
    }
}

// Animate a slow-moving caustics/liquid background with top-left blue and bottom-right purple ambient glows
@Composable
fun LuminaBackground(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "BackgroundCaustics")
    
    val phase1 by infiniteTransition.animateFloat(
        initialValue = -100f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "p1"
    )
    
    val phase2 by infiniteTransition.animateFloat(
        initialValue = -120f,
        targetValue = 120f,
        animationSpec = infiniteRepeatable(
            animation = tween(16000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "p2"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(LuminaColors.DeepSpace)
            .drawBehind {
                // Background Glow 1 (Top-Left Elegant Blue Ambient Glow)
                // top-[-10%] left-[-20%] w-[80%] h-[50%] bg-blue-600/10 rounded-full blur-[120px]
                val x1 = size.width * -0.1f + phase1
                val y1 = size.height * -0.05f + phase1 * 0.5f
                val r1 = size.width * 0.95f
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0x222563EB), Color.Transparent),
                        center = Offset(x1, y1),
                        radius = r1
                    ),
                    center = Offset(x1, y1),
                    radius = r1
                )

                // Background Glow 2 (Bottom-Right Elegant Purple Ambient Glow)
                // bottom-[-5%] right-[-10%] w-[60%] h-[40%] bg-purple-600/10 rounded-full blur-[100px]
                val x2 = size.width * 1.1f + phase2
                val y2 = size.height * 1.05f + phase2 * 0.6f
                val r2 = size.width * 0.85f
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0x229333EA), Color.Transparent),
                        center = Offset(x2, y2),
                        radius = r2
                    ),
                    center = Offset(x2, y2),
                    radius = r2
                )

                // Subtle horizontal grid lines simulating high-end glassy panels
                val gridOpacity = 0.04f
                for (i in 0..size.height.toInt() step 120) {
                    drawLine(
                        color = Color.White.copy(alpha = gridOpacity),
                        start = Offset(0f, i.toFloat()),
                        end = Offset(size.width, i.toFloat()),
                        strokeWidth = 1f
                    )
                }
            }
    ) {
        content()
    }
}

// Reusable Translucent Liquid Glass Card
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    borderGlowColor: Color = Color.Transparent,
    accentColor: Color = LuminaColors.Cyan,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed = remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed.value) 0.97f else 1.0f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessLow),
        label = "click_feedback"
    )

    val shadowAlpha by animateFloatAsState(
        targetValue = if (isPressed.value) 0.15f else 0.45f,
        animationSpec = twinSpring(),
        label = "shadow_depth"
    )

    // Dynamic Iridescent Border brush
    val borderBrush = Brush.linearGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.35f),
            accentColor.copy(alpha = 0.15f),
            Color.White.copy(alpha = 0.05f),
            accentColor.copy(alpha = 0.45f),
            Color.White.copy(alpha = 0.35f)
        ),
        start = Offset(0f, 0f),
        end = Offset(400f, 600f)
    )

    var cardModifier = modifier
        .graphicsLayer(scaleX = scale, scaleY = scale)
        .shadow(
            elevation = 12.dp,
            shape = RoundedCornerShape(cornerRadius),
            ambientColor = Color.Black.copy(alpha = 0.35f),
            spotColor = Color.Black.copy(alpha = 0.45f)
        )
        .background(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.06f), // White/6 frosted base
                    Color.White.copy(alpha = 0.02f)
                )
            ),
            shape = RoundedCornerShape(cornerRadius)
        )
        .border(
            width = 1.dp,
            brush = Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.16f), // Border white/15 base
                    accentColor.copy(alpha = 0.22f), // Iridescent accent flow
                    Color.White.copy(alpha = 0.04f),
                    accentColor.copy(alpha = 0.12f),
                    Color.White.copy(alpha = 0.16f)
                )
            ),
            shape = RoundedCornerShape(cornerRadius)
        )

    if (onClick != null) {
        cardModifier = cardModifier.clickable(
            interactionSource = interactionSource,
            indication = androidx.compose.material3.ripple(color = accentColor),
            onClick = onClick
        )
    }

    Column(
        modifier = cardModifier.padding(18.dp)
    ) {
        content()
    }
}

// Sparkly Glass Buttons with dynamic Spring mechanics
@Composable
fun GlassButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color = LuminaColors.Cyan,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    
    val scale = remember { Animatable(1f) }
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = modifier
            .graphicsLayer(scaleX = scale.value, scaleY = scale.value)
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(18.dp),
                ambientColor = accentColor.copy(alpha = 0.5f),
                spotColor = accentColor.copy(alpha = 0.5f)
            )
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        accentColor.copy(alpha = 0.9f),
                        accentColor.copy(alpha = 0.4f)
                    )
                ),
                shape = RoundedCornerShape(18.dp)
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.3f),
                shape = RoundedCornerShape(18.dp)
            )
            .clip(RoundedCornerShape(18.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = androidx.compose.material3.ripple(color = Color.White),
                enabled = enabled,
                onClick = {
                    coroutineScope.launch {
                        scale.animateTo(
                            0.93f,
                            animationSpec = spring(dampingRatio = 0.4f, stiffness = Spring.StiffnessMedium)
                        )
                        scale.animateTo(
                            1f,
                            animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessLow)
                        )
                    }
                    onClick()
                }
            )
            .padding(horizontal = 22.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            content()
        }
    }
}

// Secondary glass buttons
@Composable
fun GlassButtonSecondary(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color = Color.White,
    content: @Composable RowScope.() -> Unit
) {
    Box(
        modifier = modifier
            .background(
                color = Color(0x1EFFFFFF),
                shape = RoundedCornerShape(16.dp)
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.15f),
                shape = RoundedCornerShape(16.dp)
            )
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 11.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            content()
        }
    }
}

// Orb Avatar: Circular profile display with 3D crystal edge glare
@Composable
fun OrbAvatar(
    avatarUrl: String,
    size: Dp = 56.dp,
    accentColor: Color = LuminaColors.Cyan,
    isFollowing: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    var modifier = Modifier
        .size(size)
        .graphicsLayer {
            shadowElevation = 8.dp.toPx()
            shape = CircleShape
        }
        .border(
            width = 2.dp,
            brush = Brush.sweepGradient(
                colors = listOf(
                    accentColor, 
                    Color.White.copy(alpha = 0.8f), 
                    accentColor.copy(alpha = 0.2f), 
                    accentColor
                )
            ),
            shape = CircleShape
        )
        .padding(3.dp)
        .clip(CircleShape)

    if (onClick != null) {
        modifier = modifier.clickable(onClick = onClick)
    }

    Box(modifier = modifier) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(avatarUrl)
                .crossfade(true)
                .build(),
            contentDescription = "User Avatar",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // 3D Glass shine glaze layer
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White.copy(alpha = 0.25f), Color.Transparent),
                    center = Offset(size.toPx() * 0.25f, size.toPx() * 0.25f),
                    radius = size.toPx() * 0.45f
                )
            )
        }
    }
}

// Modern Fluid Glass Text Input
@Composable
fun GlassInput(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    accentColor: Color = LuminaColors.Cyan
) {
    var isFocused by remember { mutableStateOf(false) }
    
    val animatedBorderAlpha by animateFloatAsState(
        targetValue = if (isFocused) 0.65f else 0.15f,
        animationSpec = twinSpring(),
        label = "input_focus_border"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 54.dp)
            .background(
                color = Color(0x291A1E31),
                shape = RoundedCornerShape(18.dp)
            )
            .border(
                width = 1.2.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.2f),
                        accentColor.copy(alpha = animatedBorderAlpha),
                        Color.White.copy(alpha = 0.05f)
                    )
                ),
                shape = RoundedCornerShape(18.dp)
            )
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (leadingIcon != null) {
            leadingIcon()
            Spacer(modifier = Modifier.width(12.dp))
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .onFocusChanged { state -> isFocused = state.isFocused }
        ) {
            if (value.isEmpty()) {
                Text(
                    text = placeholder,
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 15.sp
                )
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = TextStyle(
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Normal
                ),
                cursorBrush = SolidColor(accentColor),
                keyboardOptions = keyboardOptions,
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (trailingIcon != null) {
            Spacer(modifier = Modifier.width(12.dp))
            trailingIcon()
        }
    }
}

// Liquid Tab Bar with animated flowing sliding highlight capsule
@Composable
fun LiquidTabBar(
    tabs: List<String>,
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color = LuminaColors.Cyan
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(58.dp)
            .background(
                color = Color(0x18FFFFFF),
                shape = RoundedCornerShape(20.dp)
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.1f),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(4.dp)
    ) {
        val tabCount = tabs.size
        val containerWidth = maxWidth
        val tabWidth = containerWidth / tabCount

        // Slide animation for dynamic glass highlighting indicator capsule
        val offsetTransition = updateTransition(selectedTabIndex, label = "TabSlide")
        val animatedLeftOffset by offsetTransition.animateDp(
            transitionSpec = {
                spring(dampingRatio = 0.65f, stiffness = Spring.StiffnessLow)
            },
            label = "pill_offset"
        ) { index ->
            tabWidth * index
        }

        // Animated capsule highlight mimicking fluid mercury
        Box(
            modifier = Modifier
                .offset(x = animatedLeftOffset)
                .width(tabWidth)
                .fillMaxHeight()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            accentColor.copy(alpha = 0.25f),
                            accentColor.copy(alpha = 0.08f)
                        )
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                .border(
                    width = 1.2.dp,
                    color = accentColor.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(16.dp)
                )
        )

        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabs.forEachIndexed { index, title ->
                val isSelected = selectedTabIndex == index
                val textColor by animateColorAsState(
                    targetValue = if (isSelected) Color.White else Color.White.copy(alpha = 0.5f),
                    animationSpec = tween(200),
                    label = "tab_text"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onTabSelected(index) }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        color = textColor,
                        fontSize = 14.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }
    }
}

// Utility spring spec
fun <T> twinSpring() = spring<T>(
    dampingRatio = 0.7f,
    stiffness = Spring.StiffnessLow
)
