package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CommunityComment
import com.example.model.CommunityPost
import com.example.model.CommunityReply
import com.example.ui.components.KudulaEmptyStateCard
import com.example.ui.theme.*
import com.example.ui.util.LocalScreenWindowInfo

@Composable
fun CommunityScreen(
    posts: List<CommunityPost>,
    currentUserName: String = "Kamba Aprendiz",
    currentUserRole: String = "Estudante Cognitivo",
    onReact: (String, String) -> Unit = { _, _ -> },
    onAddComment: (String, String) -> Unit = { _, _ -> },
    onAddReply: (String, String, String) -> Unit = { _, _, _ -> },
    onAddPost: (String, String) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val windowInfo = LocalScreenWindowInfo.current
    val isNarrow = windowInfo.isNarrowCompact
    var floatingPostText by remember { mutableStateOf("") }
    var selectedTag by remember { mutableStateOf("Insight Cognitivo") }
    val availableTags = listOf("Insight Cognitivo", "Dica KCLS", "Proof of Learning", "Dúvida Algoritmos", "Parceria de Estudo")

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(KudulaNavyDark)
            .testTag("community_screen")
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = windowInfo.screenHorizontalPadding),
            contentPadding = PaddingValues(top = 14.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(if (isNarrow) 14.dp else 16.dp)
        ) {
            // Header
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(if (isNarrow) 38.dp else 44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.linearGradient(listOf(KudulaElectricBlue, KudulaViolet))
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Groups,
                            contentDescription = "Comunidade de Aprendizagem",
                            tint = Color.White,
                            modifier = Modifier.size(if (isNarrow) 22.dp else 26.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Comunidade de Aprendizagem",
                            style = if (isNarrow) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Text(
                            text = "Inteligência Coletiva & Aprendizagem Social em Rede",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            fontSize = if (isNarrow) 11.sp else 12.sp
                        )
                    }
                }
            }

            // Quick Topic Tags Filter / Selector
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(availableTags) { tag ->
                        val isSelected = selectedTag == tag
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) KudulaElectricBlue.copy(alpha = 0.25f) else KudulaNavyContainer,
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) KudulaElectricBlue else BorderSubtle
                            ),
                            modifier = Modifier.clickable { selectedTag = tag }
                        ) {
                            Text(
                                text = tag,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) KudulaCyanGlow else TextSecondary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            // Posts List
            if (posts.isEmpty()) {
                item {
                    KudulaEmptyStateCard(
                        icon = Icons.Default.Groups,
                        badge = "Comunidade Peer-to-Peer",
                        title = "A comunidade aguarda a tua primeira partilha",
                        description = "Conecta-te com estudantes e investigadores do ecossistema Kudula. Publica a tua primeira dúvida, resumo de aprendizagem ou reflexão na barra abaixo!",
                        testTag = "community_empty_card"
                    )
                }
            } else {
                items(posts, key = { it.id }) { post ->
                    CommunityPostCard(
                        post = post,
                        onReact = { reaction -> onReact(post.id, reaction) },
                        onAddComment = { text -> onAddComment(post.id, text) },
                        onAddReply = { commentId, text -> onAddReply(post.id, commentId, text) }
                    )
                }
            }
        }

        // Floating Composer Input at Bottom
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            shape = RoundedCornerShape(22.dp),
            color = KudulaNavySurface.copy(alpha = 0.96f),
            border = BorderStroke(1.dp, Brush.horizontalGradient(listOf(KudulaElectricBlue.copy(alpha = 0.6f), KudulaCyanGlow.copy(alpha = 0.4f)))),
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = floatingPostText,
                    onValueChange = { floatingPostText = it },
                    placeholder = {
                        Text(
                            text = "Partilhar com a comunidade...",
                            color = TextTertiary,
                            fontSize = 12.sp,
                            maxLines = 1
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 44.dp, max = 80.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = KudulaNavyContainer,
                        unfocusedContainerColor = KudulaNavyContainer,
                        focusedBorderColor = KudulaElectricBlue,
                        unfocusedBorderColor = BorderSubtle,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        if (floatingPostText.isNotBlank()) {
                            onAddPost(floatingPostText, selectedTag)
                            floatingPostText = ""
                        }
                    },
                    enabled = floatingPostText.isNotBlank(),
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(
                            if (floatingPostText.isNotBlank()) KudulaElectricBlue else KudulaNavyContainer
                        )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Publicar",
                        tint = if (floatingPostText.isNotBlank()) Color.White else TextTertiary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CommunityPostCard(
    post: CommunityPost,
    onReact: (String) -> Unit,
    onAddComment: (String) -> Unit,
    onAddReply: (String, String) -> Unit
) {
    var areCommentsExpanded by remember { mutableStateOf(false) }
    var newCommentText by remember { mutableStateOf("") }
    var replyingToCommentId by remember { mutableStateOf<String?>(null) }
    var replyText by remember { mutableStateOf("") }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = KudulaNavySurface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(listOf(BorderSubtle, BorderSubtle))
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("community_post_${post.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Author Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(listOf(KudulaElectricBlue, KudulaViolet))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = post.author.firstOrNull()?.toString() ?: "U",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 15.sp
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = post.author,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${post.role} • ${post.location}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = KudulaViolet.copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, KudulaViolet.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = post.tag,
                        style = MaterialTheme.typography.labelSmall,
                        color = KudulaVioletLight,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Post Content
            Text(
                text = post.content,
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = BorderSubtle)
            Spacer(modifier = Modifier.height(10.dp))

            // Reactions & Counter Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Interactive Reactions: Like, Dislike, Heart
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Like Button 👍
                    ReactionChip(
                        emoji = "👍",
                        count = post.likesCount,
                        isSelected = post.userReaction == "LIKE",
                        onClick = { onReact("LIKE") }
                    )

                    // Dislike Button 👎
                    ReactionChip(
                        emoji = "👎",
                        count = post.dislikesCount,
                        isSelected = post.userReaction == "DISLIKE",
                        onClick = { onReact("DISLIKE") }
                    )

                    // Heart Button ❤️
                    ReactionChip(
                        emoji = "❤️",
                        count = post.heartsCount,
                        isSelected = post.userReaction == "HEART",
                        onClick = { onReact("HEART") }
                    )
                }

                // Comment Button with counter
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (areCommentsExpanded) KudulaCyanGlow.copy(alpha = 0.2f) else KudulaNavyContainer,
                    border = BorderStroke(
                        1.dp,
                        if (areCommentsExpanded) KudulaCyanGlow.copy(alpha = 0.6f) else BorderSubtle
                    ),
                    modifier = Modifier.clickable { areCommentsExpanded = !areCommentsExpanded }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChatBubbleOutline,
                            contentDescription = "Comentários",
                            tint = if (areCommentsExpanded) KudulaCyanGlow else TextSecondary,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "${post.commentsCount}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (areCommentsExpanded) KudulaCyanGlow else TextSecondary
                        )
                    }
                }
            }

            // Expandable Comments Section
            AnimatedVisibility(
                visible = areCommentsExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp)
                ) {
                    HorizontalDivider(color = BorderSubtle.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "DISCUSSÃO & RESPOSTAS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = KudulaCyanGlow,
                        fontSize = 10.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Existing Comments List
                    if (post.comments.isEmpty()) {
                        Text(
                            text = "Ainda sem comentários. Sê o primeiro a enriquecer este tópico!",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextTertiary,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            post.comments.forEach { comment ->
                                CommentItemView(
                                    comment = comment,
                                    onReplyClick = {
                                        replyingToCommentId = if (replyingToCommentId == comment.id) null else comment.id
                                        replyText = ""
                                    }
                                )

                                // In-line reply form
                                if (replyingToCommentId == comment.id) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(start = 28.dp, top = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        OutlinedTextField(
                                            value = replyText,
                                            onValueChange = { replyText = it },
                                            placeholder = { Text("Responder a ${comment.author}...", fontSize = 11.sp, color = TextTertiary) },
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(42.dp),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedContainerColor = KudulaNavyContainer,
                                                unfocusedContainerColor = KudulaNavyContainer,
                                                focusedTextColor = Color.White,
                                                unfocusedTextColor = Color.White
                                            )
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        IconButton(
                                            onClick = {
                                                if (replyText.isNotBlank()) {
                                                    onAddReply(comment.id, replyText)
                                                    replyText = ""
                                                    replyingToCommentId = null
                                                }
                                            },
                                            enabled = replyText.isNotBlank()
                                        ) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.Send,
                                                contentDescription = "Enviar Resposta",
                                                tint = KudulaCyanGlow,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Comment Input for this post
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newCommentText,
                            onValueChange = { newCommentText = it },
                            placeholder = { Text("Adicionar comentário...", fontSize = 12.sp, color = TextTertiary) },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = KudulaNavyContainer,
                                unfocusedContainerColor = KudulaNavyContainer,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                if (newCommentText.isNotBlank()) {
                                    onAddComment(newCommentText)
                                    newCommentText = ""
                                }
                            },
                            enabled = newCommentText.isNotBlank()
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Comentar",
                                tint = if (newCommentText.isNotBlank()) KudulaCyanGlow else TextTertiary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReactionChip(
    emoji: String,
    count: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) KudulaElectricBlue.copy(alpha = 0.3f) else KudulaNavyContainer,
        border = BorderStroke(
            1.dp,
            if (isSelected) KudulaElectricBlue else BorderSubtle
        ),
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(text = emoji, fontSize = 13.sp)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "$count",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) Color.White else TextSecondary,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
fun CommentItemView(
    comment: CommunityComment,
    onReplyClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(KudulaNavyContainer.copy(alpha = 0.7f))
            .padding(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = comment.avatar, fontSize = 14.sp)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = comment.author,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "• ${comment.timeAgo}",
                style = MaterialTheme.typography.labelSmall,
                color = TextTertiary,
                fontSize = 10.sp
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = comment.content,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            lineHeight = 18.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(
                onClick = onReplyClick,
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                modifier = Modifier.height(24.dp)
            ) {
                Text(
                    text = "Responder",
                    style = MaterialTheme.typography.labelSmall,
                    color = KudulaCyanGlow,
                    fontSize = 10.sp
                )
            }
        }

        // Nested Replies Thread
        if (comment.replies.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, top = 6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                comment.replies.forEach { reply ->
                    ReplyItemView(reply = reply)
                }
            }
        }
    }
}

@Composable
fun ReplyItemView(reply: CommunityReply) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(KudulaNavySurface.copy(alpha = 0.8f))
            .padding(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = reply.avatar, fontSize = 12.sp)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = reply.author,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = KudulaVioletLight
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "• ${reply.timeAgo}",
                style = MaterialTheme.typography.labelSmall,
                color = TextTertiary,
                fontSize = 9.sp
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = reply.content,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            fontSize = 11.sp,
            lineHeight = 16.sp
        )
    }
}
