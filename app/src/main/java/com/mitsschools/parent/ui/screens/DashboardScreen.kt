package com.mitsschools.parent.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.EventNote
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mitsschools.parent.R
import com.mitsschools.parent.data.model.DailyDiary
import com.mitsschools.parent.data.model.SchoolClass
import com.mitsschools.parent.data.model.SchoolEvent
import com.mitsschools.parent.ui.AppViewModel
import com.mitsschools.parent.ui.components.AppCard
import com.mitsschools.parent.ui.components.DateBlock
import com.mitsschools.parent.ui.components.Fmt
import com.mitsschools.parent.ui.components.IconTile
import com.mitsschools.parent.ui.components.InitialsAvatar
import com.mitsschools.parent.ui.components.Pill
import com.mitsschools.parent.ui.components.SectionTitle
import com.mitsschools.parent.ui.components.eventColor
import com.mitsschools.parent.ui.theme.Brand
import com.mitsschools.parent.ui.theme.LocalFeatureColors
import com.mitsschools.parent.ui.theme.headerColor
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    vm: AppViewModel,
    schoolClass: SchoolClass,
    onOpenNotifications: () -> Unit,
    onOpenDaily: () -> Unit,
    onOpenWeekly: () -> Unit,
    onOpenCalendar: () -> Unit,
    onOpenProfile: () -> Unit,
    onChangeClass: () -> Unit,
) {
    val session by vm.session.collectAsStateWithLifecycle()
    val notifications by vm.notifications.collectAsStateWithLifecycle()
    val readIds by vm.readIds.collectAsStateWithLifecycle()
    val events by vm.events.collectAsStateWithLifecycle()
    val done by vm.doneHomework.collectAsStateWithLifecycle()
    val refreshing by vm.refreshing.collectAsStateWithLifecycle()

    val today = remember { LocalDate.now() }
    var todayDiary by remember { mutableStateOf<DailyDiary?>(null) }
    LaunchedEffect(schoolClass, refreshing) {
        if (!refreshing) todayDiary = vm.dailyDiary(schoolClass, today)
    }

    val unread = notifications.filter { it.id !in readIds }
    val upcoming = events.filter { !it.end.isBefore(today) }.sortedBy { it.start }.take(3)
    val homework = todayDiary?.entries?.filter { it.homework != null }.orEmpty()
    val pendingHw = homework.count { vm.homeworkKey(schoolClass, today, it.subject) !in done }
    val f = LocalFeatureColors.current

    PullToRefreshBox(
        isRefreshing = refreshing,
        onRefresh = { vm.refresh() },
        modifier = Modifier.fillMaxSize(),
    ) {
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp),
        ) {
            item {
                Header(
                    parentName = session?.parentName ?: "Parent",
                    schoolClass = schoolClass,
                    unreadCount = unread.size,
                    onBell = onOpenNotifications,
                    onProfile = onOpenProfile,
                    onChangeClass = onChangeClass,
                )
            }

            // ---- Notifications bar (from flowchart) ----
            item {
                val latest = unread.firstOrNull() ?: notifications.firstOrNull()
                Column(
                    Modifier
                        .padding(horizontal = 16.dp)
                        .offset(y = (-28).dp)
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.medium)
                        .background(f.noticeBg)
                        .border(1.dp, f.noticeBorder, MaterialTheme.shapes.medium)
                        .clickable(onClick = onOpenNotifications)
                        .padding(16.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (unread.isNotEmpty()) Icons.Rounded.NotificationsActive else Icons.Rounded.Notifications,
                            contentDescription = null,
                            tint = Brand.Gold,
                        )
                        Spacer(Modifier.width(10.dp))
                        Text("Notifications", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                        if (unread.isNotEmpty()) {
                            Pill("${unread.size} new", MaterialTheme.colorScheme.primary)
                        } else {
                            Pill("All caught up", f.success)
                        }
                    }
                    if (latest != null) {
                        Spacer(Modifier.height(8.dp))
                        Text(latest.title, style = MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(
                            latest.body,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    } else {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "New diary • Homework reminders • School announcements • Important notices",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            // ---- Today at a glance ----
            item {
                Column(Modifier.padding(horizontal = 16.dp).offset(y = (-12).dp)) {
                    SectionTitle("Today at a glance")
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        val diary = todayDiary
                        GlanceTile(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Rounded.Edit,
                            tint = f.daily,
                            value = when {
                                diary == null -> "–"
                                !diary.isSchoolDay -> "0"
                                else -> pendingHw.toString()
                            },
                            label = if (homework.isNotEmpty() && pendingHw == 0) "Homework done" else "Homework due",
                            onClick = onOpenDaily,
                        )
                        GlanceTile(
                            modifier = Modifier.weight(1f),
                            icon = Icons.AutoMirrored.Rounded.MenuBook,
                            tint = f.weekly,
                            value = when {
                                diary == null -> "–"
                                !diary.isSchoolDay -> "Off"
                                else -> diary.entries.size.toString()
                            },
                            label = if (diary?.isSchoolDay == false) (diary?.closedReason ?: "No school") else "Subjects today",
                            onClick = onOpenDaily,
                        )
                        val next = upcoming.firstOrNull()
                        GlanceTile(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Rounded.Event,
                            tint = f.calendar,
                            value = next?.let { Fmt.inDays(it.start, today) } ?: "–",
                            label = next?.title ?: "Next event",
                            onClick = onOpenCalendar,
                        )
                    }
                }
            }

            // ---- Main features (from flowchart) ----
            item {
                Column(
                    Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    SectionTitle("Diary & calendar")
                    FeatureCard(
                        title = "Daily Diary",
                        description = "Daily classwork, homework and teacher notes",
                        icon = Icons.AutoMirrored.Rounded.MenuBook,
                        tint = f.daily,
                        background = f.dailyBg,
                        onClick = onOpenDaily,
                    )
                    FeatureCard(
                        title = "Weekly Diary",
                        description = "Weekly syllabus, learning objectives and homework",
                        icon = Icons.AutoMirrored.Rounded.EventNote,
                        tint = f.weekly,
                        background = f.weeklyBg,
                        onClick = onOpenWeekly,
                    )
                    FeatureCard(
                        title = "Yearly Calendar",
                        description = "Holidays, exams, events and academic schedule",
                        icon = Icons.Rounded.CalendarMonth,
                        tint = f.calendar,
                        background = f.calendarBg,
                        onClick = onOpenCalendar,
                    )
                }
            }

            // ---- Coming up ----
            if (upcoming.isNotEmpty()) {
                item {
                    SectionTitle(
                        "Coming up",
                        modifier = Modifier.padding(start = 16.dp, end = 4.dp, top = 16.dp),
                        actionLabel = "See all",
                        onAction = onOpenCalendar,
                    )
                }
                items(upcoming, key = { it.id }) { event ->
                    UpcomingRow(event, today, onOpenCalendar)
                }
            }
        }
    }
}

@Composable
private fun Header(
    parentName: String,
    schoolClass: SchoolClass,
    unreadCount: Int,
    onBell: () -> Unit,
    onProfile: () -> Unit,
    onChangeClass: () -> Unit,
) {
    val header = headerColor()
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
            .background(Brush.verticalGradient(listOf(header, Brand.CharcoalDeep))),
    ) {
        Image(
            painter = painterResource(R.drawable.leaf),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(190.dp)
                .offset(x = 50.dp, y = 30.dp)
                .alpha(0.07f),
        )
        Column(
            Modifier
                .statusBarsPadding()
                .padding(start = 20.dp, end = 12.dp, top = 8.dp, bottom = 48.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(R.drawable.logo_horizontal_light),
                    contentDescription = "MITS Schools",
                    modifier = Modifier.height(40.dp),
                )
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onBell) {
                    BadgedBox(badge = {
                        if (unreadCount > 0) Badge { Text(unreadCount.toString()) }
                    }) {
                        Icon(Icons.Rounded.Notifications, contentDescription = "Notifications", tint = Color.White)
                    }
                }
                IconButton(onClick = onProfile) {
                    InitialsAvatar(parentName, size = 34.dp, background = Brand.Gold, content = Brand.Charcoal)
                }
            }
            Spacer(Modifier.height(22.dp))
            Text(
                Fmt.dayLong(LocalDate.now()),
                style = MaterialTheme.typography.labelLarge,
                color = Brand.GoldLight,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                "${Fmt.greeting()}, $parentName",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
            )
            Spacer(Modifier.height(12.dp))
            Row(
                Modifier
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.12f))
                    .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.25f)), CircleShape)
                    .clickable(onClick = onChangeClass)
                    .padding(start = 14.dp, end = 8.dp, top = 6.dp, bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "${schoolClass.label} • ${schoolClass.stage.label}",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White,
                )
                Icon(Icons.Rounded.KeyboardArrowDown, contentDescription = "Change class", tint = Color.White)
            }
        }
    }
}

@Composable
private fun GlanceTile(
    modifier: Modifier,
    icon: ImageVector,
    tint: Color,
    value: String,
    label: String,
    onClick: () -> Unit,
) {
    AppCard(modifier = modifier, onClick = onClick, contentPadding = 12.dp) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
        Spacer(Modifier.height(8.dp))
        Text(value, style = MaterialTheme.typography.titleLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            minLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun FeatureCard(
    title: String,
    description: String,
    icon: ImageVector,
    tint: Color,
    background: Color,
    onClick: () -> Unit,
) {
    AppCard(onClick = onClick, containerColor = background, border = null, modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconTile(icon, tint = tint, background = tint.copy(alpha = 0.14f), size = 48.dp)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(
                    description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = null, tint = tint)
        }
    }
}

@Composable
private fun UpcomingRow(event: SchoolEvent, today: LocalDate, onClick: () -> Unit) {
    val color = eventColor(event.type)
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DateBlock(event.start, color)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(event.title, style = MaterialTheme.typography.titleSmall)
            Text(
                "${event.type.label} • ${Fmt.range(event.start, event.end)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Pill(Fmt.inDays(event.start, today), color)
    }
}
