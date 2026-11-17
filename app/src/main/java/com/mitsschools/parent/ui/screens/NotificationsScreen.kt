package com.mitsschools.parent.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DoneAll
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mitsschools.parent.data.model.NoticeCategory
import com.mitsschools.parent.data.model.SchoolNotification
import com.mitsschools.parent.ui.AppViewModel
import com.mitsschools.parent.ui.components.AppCard
import com.mitsschools.parent.ui.components.EmptyState
import com.mitsschools.parent.ui.components.Fmt
import com.mitsschools.parent.ui.components.IconTile
import com.mitsschools.parent.ui.components.noticeColor
import com.mitsschools.parent.ui.components.noticeIcon
import com.mitsschools.parent.ui.components.MitsTopBar
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Composable
fun NotificationsScreen(vm: AppViewModel) {
    val all by vm.notifications.collectAsStateWithLifecycle()
    val readIds by vm.readIds.collectAsStateWithLifecycle()
    var filter by rememberSaveable { mutableStateOf<NoticeCategory?>(null) }
    var expandedId by rememberSaveable { mutableStateOf<String?>(null) }

    val list = all.filter { filter == null || it.category == filter }
    val unread = all.count { it.id !in readIds }
    val today = LocalDate.now()
    val groups = list.groupBy {
        val days = ChronoUnit.DAYS.between(it.time.toLocalDate(), today)
        when {
            days == 0L -> "Today"
            days == 1L -> "Yesterday"
            days < 7 -> "This week"
            else -> "Earlier"
        }
    }

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        MitsTopBar(
            title = "Notifications",
            subtitle = if (unread > 0) "$unread unread" else "You're all caught up",
            actions = {
                if (unread > 0) {
                    IconButton(onClick = { vm.markAllRead() }) {
                        Icon(Icons.Rounded.DoneAll, contentDescription = "Mark all as read")
                    }
                }
            },
        )
        Row(
            Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilterChip(selected = filter == null, onClick = { filter = null }, label = { Text("All") })
            NoticeCategory.entries.forEach { c ->
                FilterChip(
                    selected = filter == c,
                    onClick = { filter = if (filter == c) null else c },
                    label = { Text(c.label) },
                    leadingIcon = { Icon(noticeIcon(c), contentDescription = null, modifier = Modifier.size(18.dp)) },
                )
            }
        }

        if (list.isEmpty()) {
            EmptyState(
                icon = Icons.Rounded.NotificationsNone,
                title = "No notifications",
                message = "New diaries, homework reminders and school announcements will appear here.",
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                listOf("Today", "Yesterday", "This week", "Earlier").forEach { label ->
                    val group = groups[label].orEmpty()
                    if (group.isNotEmpty()) {
                        item(key = "h-$label") {
                            Text(
                                label,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 6.dp),
                            )
                        }
                        items(group, key = { it.id }) { n ->
                            NotificationCard(
                                n = n,
                                isUnread = n.id !in readIds,
                                expanded = expandedId == n.id,
                                onClick = {
                                    vm.markRead(n.id)
                                    expandedId = if (expandedId == n.id) null else n.id
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationCard(n: SchoolNotification, isUnread: Boolean, expanded: Boolean, onClick: () -> Unit) {
    val color = noticeColor(n.category)
    AppCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().animateContentSize(),
        containerColor = if (isUnread) MaterialTheme.colorScheme.surfaceContainerLowest else MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Row {
            IconTile(noticeIcon(n.category), tint = color, background = color.copy(alpha = 0.13f), size = 42.dp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        n.category.label.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = color,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        Fmt.relative(n.time),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (isUnread) {
                        Spacer(Modifier.width(6.dp))
                        Box(Modifier.size(8.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
                    }
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    n.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = if (isUnread) FontWeight.Bold else FontWeight.Medium,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    n.body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = if (expanded) Int.MAX_VALUE else 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
