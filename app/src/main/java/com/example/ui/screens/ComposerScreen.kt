package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.*
import com.example.ui.viewmodel.LuminaViewModel

@Composable
fun ComposerScreen(
    viewModel: LuminaViewModel,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var contentText by remember { mutableStateOf("") }
    
    // Media attachment simulations
    var attachedImageLink by remember { mutableStateOf<String?>(null) }
    var attachedVoiceSec by remember { mutableStateOf<Int?>(null) }
    
    // Poll options state
    var showPollComposer by remember { mutableStateOf(false) }
    var pollOpt1 by remember { mutableStateOf("") }
    var pollOpt2 by remember { mutableStateOf("") }
    var pollOpt3 by remember { mutableStateOf("") }

    val me by viewModel.me.collectAsState()
    val activeAccentHex = me?.accentColorHex ?: "#00F2FE"
    val activeColor = LuminaColors.getAccent(activeAccentHex)

    // Animated expansion scale mimicking liquid bubble
    var isExpanded by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isExpanded = true
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f))
            .clickable(onClick = onClose),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Glowing Liquid Glass pane container
        AnimatedVisibility(
            visible = isExpanded,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
            modifier = Modifier.clickable(enabled = false) { /* prevent background clicks dismiss */ }
        ) {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.85f)
                    .padding(16.dp),
                accentColor = activeColor,
                cornerRadius = 28.dp
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Top controls bar
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                OrbAvatar(
                                    avatarUrl = me?.avatarUrl ?: "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=150&q=80",
                                    size = 36.dp,
                                    accentColor = activeColor
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = me?.displayName ?: "N. Albert",
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Broadcasting globally",
                                        color = activeColor.copy(alpha = 0.7f),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            IconButton(
                                onClick = {
                                    isExpanded = false
                                    onClose()
                                },
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(Color.White.copy(alpha = 0.08f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Divider(color = Color.White.copy(alpha = 0.08f), thickness = 1.dp, modifier = Modifier.padding(vertical = 16.dp))

                        // Large Message Input
                        GlassInput(
                            value = contentText,
                            onValueChange = { contentText = it },
                            placeholder = "Compose liquid transmission context...",
                            accentColor = activeColor
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Attachments simulations options panels
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Simulation button: Attach futuristic aesthetic canvas background
                            IconButton(
                                onClick = {
                                    attachedImageLink = if (attachedImageLink == null) {
                                        "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=800&q=80"
                                    } else null
                                },
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .background(
                                        color = if (attachedImageLink != null) activeColor.copy(alpha = 0.2f) else Color.Transparent,
                                        shape = CircleShape
                                    )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Image,
                                    contentDescription = "Simulate image attachment",
                                    tint = if (attachedImageLink != null) activeColor else Color.White.copy(alpha = 0.5f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // Simulation button: Attach microphone stream sound note
                            IconButton(
                                onClick = {
                                    attachedVoiceSec = if (attachedVoiceSec == null) 34 else null
                                },
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .background(
                                        color = if (attachedVoiceSec != null) activeColor.copy(alpha = 0.2f) else Color.Transparent,
                                        shape = CircleShape
                                    )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = "Simulate audio spectrum",
                                    tint = if (attachedVoiceSec != null) activeColor else Color.White.copy(alpha = 0.5f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // Simulation button: Build interactive Poll
                            IconButton(
                                onClick = { showPollComposer = !showPollComposer },
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .background(
                                        color = if (showPollComposer) activeColor.copy(alpha = 0.2f) else Color.Transparent,
                                        shape = CircleShape
                                    )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Poll,
                                    contentDescription = "Generate grid poll",
                                    tint = if (showPollComposer) activeColor else Color.White.copy(alpha = 0.5f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        // Render optional attachments indicators inside scroll list
                        Spacer(modifier = Modifier.height(10.dp))

                        if (attachedImageLink != null) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0x11FFFFFF), RoundedCornerShape(12.dp))
                                    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                                    .padding(10.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Attachment, contentDescription = "Attached", tint = activeColor, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Aesthetic glass render attached.",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 12.sp,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "REMOVE",
                                    color = activeColor,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.clickable { attachedImageLink = null }
                                )
                            }
                        }

                        if (attachedVoiceSec != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0x11FFFFFF), RoundedCornerShape(12.dp))
                                    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                                    .padding(10.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Sensors, contentDescription = "Voice Stream", tint = activeColor, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Simulated 34s high frequency crystall voice node.",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 12.sp,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "REMOVE",
                                    color = activeColor,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.clickable { attachedVoiceSec = null }
                                )
                            }
                        }

                        if (showPollComposer) {
                            Spacer(modifier = Modifier.height(12.dp))
                            GlassCard(
                                modifier = Modifier.fillMaxWidth(),
                                accentColor = activeColor,
                                cornerRadius = 16.dp
                            ) {
                                Text(
                                    text = "POLL TRANSMISSION MATRIX",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    GlassInput(value = pollOpt1, onValueChange = { pollOpt1 = it }, placeholder = "Option 1 (e.g. Float Glass)", accentColor = activeColor)
                                    GlassInput(value = pollOpt2, onValueChange = { pollOpt2 = it }, placeholder = "Option 2 (e.g. Heavy Flint)", accentColor = activeColor)
                                    GlassInput(value = pollOpt3, onValueChange = { pollOpt3 = it }, placeholder = "Option 3 (e.g. Diamond)", accentColor = activeColor)
                                }
                            }
                        }
                    }

                    // Bottom Broadcast Trigger
                    GlassButton(
                        onClick = {
                            if (contentText.isNotBlank()) {
                                val pollOpts = if (showPollComposer && pollOpt1.isNotBlank() && pollOpt2.isNotBlank()) {
                                    listOf(pollOpt1, pollOpt2, if (pollOpt3.isNotBlank()) pollOpt3 else "").filter { it.isNotEmpty() }.joinToString(",")
                                } else null

                                viewModel.composeNewPost(
                                    content = contentText,
                                    imageUrl = attachedImageLink,
                                    voiceDurationSec = attachedVoiceSec,
                                    pollOptions = pollOpts
                                )
                                onClose()
                            }
                        },
                        accentColor = activeColor,
                        enabled = contentText.isNotBlank(),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
                    ) {
                        Text(
                            text = "TRANSMIT TO CRYSTAL GRID",
                            color = Color.Black,
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(imageVector = Icons.Default.Send, contentDescription = "Transmit", tint = Color.Black, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}
