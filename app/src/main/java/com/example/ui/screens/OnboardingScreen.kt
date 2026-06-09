package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassButton
import com.example.ui.components.GlassCard
import com.example.ui.components.LuminaBackground
import com.example.ui.components.LuminaColors
import com.example.ui.viewmodel.LuminaViewModel

@Composable
fun OnboardingScreen(
    viewModel: LuminaViewModel,
    onComplete: () -> Unit
) {
    var step by remember { mutableStateOf(0) }
    var selectedColorHex by remember { mutableStateOf("#00F2FE") } // Cyan first

    val colorsList = listOf(
        Pair("#00F2FE", "Cyber Cyan"),
        Pair("#FF2592", "Electric Magenta"),
        Pair("#00FF87", "Neon Emerald"),
        Pair("#7F00FF", "Obsidian Violet")
    )

    val activeColor = LuminaColors.getAccent(selectedColorHex)

    LuminaBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .statusBarsPadding()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 20.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "Lumina Logo",
                    tint = Color(0xFFA5B4FC),
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
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
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                )
            }

            // Animated content switcher based on step
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = step,
                    transitionSpec = {
                        slideInHorizontally { width -> width } + fadeIn() togetherWith
                        slideOutHorizontally { width -> -width } + fadeOut()
                    },
                    label = "OnboardingCrossfade"
                ) { currentStep ->
                    when (currentStep) {
                        0 -> {
                            GlassCard(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                accentColor = activeColor
                            ) {
                                Text(
                                    text = "Behold the Liquid Glass Grid",
                                    color = Color.White,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                                )
                                Text(
                                    text = "Welcome to a social timeline crafted like mercury suspended in quartz. Lumina bends light, responds with fluid physics, and honors deep visual clarity.",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 15.sp,
                                    lineHeight = 22.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                        1 -> {
                            GlassCard(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                accentColor = activeColor
                            ) {
                                Text(
                                    text = "Choose Your Spectrum",
                                    color = Color.White,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                                )
                                Text(
                                    text = "Lumina adapts. Pick a core caustic accent that mirrors your creative energy.",
                                    color = Color.White.copy(alpha = 0.6f),
                                    fontSize = 14.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    colorsList.forEach { (hex, name) ->
                                        val isSelected = selectedColorHex == hex
                                        val itemColor = LuminaColors.getAccent(hex)
                                        
                                        val scale by animateFloatAsState(
                                            targetValue = if (isSelected) 1.25f else 1.0f,
                                            animationSpec = spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessMedium)
                                        )

                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.clickable {
                                                selectedColorHex = hex
                                            }
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(48.dp)
                                                    .graphicsLayer(scaleX = scale, scaleY = scale)
                                                    .background(itemColor, CircleShape)
                                                    .border(
                                                        width = if (isSelected) 3.dp else 1.dp,
                                                        color = Color.White,
                                                        shape = CircleShape
                                                    )
                                                    .shadow(
                                                        elevation = if (isSelected) 12.dp else 0.dp,
                                                        shape = CircleShape,
                                                        ambientColor = itemColor,
                                                        spotColor = itemColor
                                                    )
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = name.split(" ").last(),
                                                color = if (isSelected) Color.White else Color.White.copy(alpha = 0.4f),
                                                fontSize = 12.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Bottom controls
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth().padding(bottom = 30.dp)
            ) {
                // Indicator dots
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(bottom = 24.dp)
                ) {
                    repeat(2) { index ->
                        val isSelected = step == index
                        val dotWidth by animateDpAsState(
                            targetValue = if (isSelected) 24.dp else 8.dp,
                            animationSpec = spring(stiffness = Spring.StiffnessLow)
                        )
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .height(8.dp)
                                .width(dotWidth)
                                .background(
                                    color = if (isSelected) activeColor else Color.White.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(4.dp)
                                )
                        )
                    }
                }

                GlassButton(
                    onClick = {
                        if (step < 1) {
                            step++
                        } else {
                            onComplete()
                        }
                    },
                    accentColor = activeColor,
                    modifier = Modifier.widthIn(min = 200.dp)
                ) {
                    Text(
                        text = if (step == 1) "ENTER THE VOID" else "CONTINUE",
                        color = Color.Black,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        letterSpacing = 1.5.sp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Arrow Forward",
                        tint = Color.Black,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
