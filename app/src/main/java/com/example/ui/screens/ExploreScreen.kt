package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.*
import com.example.ui.viewmodel.LuminaViewModel

@Composable
fun ExploreScreen(
    viewModel: LuminaViewModel,
    onPostClick: (String) -> Unit,
    onProfileClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val query by viewModel.searchQuery.collectAsState()
    val searchedPosts by viewModel.searchedPosts.collectAsState()
    
    val me by viewModel.me.collectAsState()
    val activeAccentHex = me?.accentColorHex ?: "#00F2FE"
    val activeColor = LuminaColors.getAccent(activeAccentHex)

    val trendingTopics = listOf(
        Pair("#RefractionGrid", "94.2K glass cells"),
        Pair("#MercuryShaders", "88.1K mercury flows"),
        Pair("#ZeroGravityUI", "41.6K suspensions"),
        Pair("#GlassMorphism", "137.9K refractive indexes"),
        Pair("#ObsidianFlux", "12.4K shadows")
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
    ) {
        // Search header title
        Text(
            text = "EXPLORE DISCOVER",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 2.sp,
            modifier = Modifier.padding(vertical = 16.dp, horizontal = 4.dp)
        )

        // Glass search input
        GlassInput(
            value = query,
            onValueChange = { viewModel.updateSearchQuery(it) },
            placeholder = "Search the crystal grid...",
            accentColor = activeColor,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search grid",
                    tint = activeColor.copy(alpha = 0.7f)
                )
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    Text(
                        text = "CLEAR",
                        color = activeColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.clickable { viewModel.updateSearchQuery("") }
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Conditional display: list of trending nodes if search is empty
        if (query.isBlank()) {
            Text(
                text = "HOLOGRAPHIC TRENDING NODES",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
                modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(trendingTopics) { (tag, stats) ->
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        accentColor = activeColor,
                        onClick = { viewModel.updateSearchQuery(tag) }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TrendingUp,
                                    contentDescription = "Trend Vector",
                                    tint = activeColor,
                                    modifier = Modifier.size(20.dp)
                                )
                                Column {
                                    Text(
                                        text = tag,
                                        color = Color.White,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = stats,
                                        color = Color.White.copy(alpha = 0.4f),
                                        fontSize = 12.sp
                                    )
                                }
                            }
                            
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Query Vector",
                                tint = Color.White.copy(alpha = 0.3f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        } else {
            // Searched results Display
            Text(
                text = "TRANSMISSION RESULTS MATCHING \"${query.uppercase()}\"",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
            )

            if (searchedPosts.isEmpty()) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Negative search index matches found.",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(searchedPosts, key = { it.post.id }) { item ->
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
