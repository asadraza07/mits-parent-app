package com.mitsschools.parent.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.Book
import androidx.compose.material.icons.rounded.BeachAccess
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.Campaign
import androidx.compose.material.icons.rounded.Celebration
import androidx.compose.material.icons.rounded.Computer
import androidx.compose.material.icons.rounded.Eco
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.PriorityHigh
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Quiz
import androidx.compose.material.icons.rounded.Science
import androidx.compose.material.icons.rounded.SportsSoccer
import androidx.compose.material.icons.rounded.Spellcheck
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mitsschools.parent.data.model.EventType
import com.mitsschools.parent.data.model.NoticeCategory
import com.mitsschools.parent.data.model.Subject
import com.mitsschools.parent.ui.theme.LocalFeatureColors
import com.mitsschools.parent.ui.theme.headerColor
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale

/* ------------------------------------------------------------------ */
/*  Top bar                                                            */
/* ------------------------------------------------------------------ */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MitsTopBar(
    title: String,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    TopAppBar(
        title = {
            Column {
                Text(title, style = MaterialTheme.typography.titleLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (subtitle != null) {
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.72f),
                        maxLines = 1,
                    )
                }
            }
        },
        navigationIcon = {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                }
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = headerColor(),
            titleContentColor = Color.White,
            navigationIconContentColor = Color.White,
            actionIconContentColor = Color.White,
        ),
    )
}

/* ------------------------------------------------------------------ */
/*  Cards & sections                                                   */
/* ------------------------------------------------------------------ */

@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerLowest,
    border: BorderStroke? = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
    contentPadding: Dp = 16.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = CardDefaults.cardColors(containerColor = containerColor)
    val shape = MaterialTheme.shapes.medium
    if (onClick != null) {
        Card(onClick = onClick, modifier = modifier, shape = shape, colors = colors, border = border) {
            Column(Modifier.padding(contentPadding), content = content)
        }
    } else {
        Card(modifier = modifier, shape = shape, colors = colors, border = border) {
            Column(Modifier.padding(contentPadding), content = content)
        }
    }
}

@Composable
fun SectionTitle(
    text: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f),
        )
        if (actionLabel != null && onAction != null) {
            TextButton(onClick = onAction) { Text(actionLabel) }
        }
    }
}

@Composable
fun IconTile(
    icon: ImageVector,
    tint: Color,
    background: Color,
    size: Dp = 44.dp,
    shape: Shape = RoundedCornerShape(12.dp),
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(shape)
            .background(background),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(size * 0.55f))
    }
}

@Composable
fun Pill(text: String, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(color.copy(alpha = 0.14f))
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(text, color = color, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    message: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        IconTile(
            icon = icon,
            tint = MaterialTheme.colorScheme.primary,
            background = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
            size = 72.dp,
            shape = CircleShape,
        )
        Spacer(Modifier.height(16.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
        Spacer(Modifier.height(6.dp))
        Text(
            message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun LoadingBlock(modifier: Modifier = Modifier) {
    Box(
        modifier
            .fillMaxWidth()
            .padding(48.dp),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(strokeWidth = 3.dp)
    }
}

@Composable
fun InitialsAvatar(name: String, size: Dp = 40.dp, background: Color, content: Color) {
    val initials = name.split(" ").filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercase() }
        .ifEmpty { "P" }
    Box(
        Modifier
            .size(size)
            .clip(CircleShape)
            .background(background),
        contentAlignment = Alignment.Center,
    ) {
        Text(initials, color = content, style = MaterialTheme.typography.titleSmall)
    }
}

/** Calendar-style date block, e.g. “20 / NOV”. */
@Composable
fun DateBlock(date: LocalDate, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .width(52.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            date.dayOfMonth.toString(),
            style = MaterialTheme.typography.titleLarge,
            color = color,
        )
        Text(
            date.month.getDisplayName(TextStyle.SHORT, Locale.getDefault()).uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = color,
        )
    }
}

@Composable
fun Bullet(text: String, color: Color = MaterialTheme.colorScheme.primary) {
    Row(Modifier.padding(vertical = 2.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(
            Modifier
                .padding(top = 8.dp)
                .size(6.dp)
                .clip(CircleShape)
                .background(color),
        )
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}

/* ------------------------------------------------------------------ */
/*  Visual mapping for domain types                                    */
/* ------------------------------------------------------------------ */

data class SubjectStyle(val icon: ImageVector, val color: Color)

fun subjectStyle(subject: Subject): SubjectStyle = when (subject) {
    Subject.PHONICS -> SubjectStyle(Icons.Rounded.Spellcheck, Color(0xFFC0392B))
    Subject.NUMBERS -> SubjectStyle(Icons.Rounded.Category, Color(0xFF2F6CB3))
    Subject.GENERAL -> SubjectStyle(Icons.Rounded.Lightbulb, Color(0xFFB7791F))
    Subject.RHYMES -> SubjectStyle(Icons.Rounded.MusicNote, Color(0xFF8E44AD))
    Subject.PLAY -> SubjectStyle(Icons.Rounded.SportsSoccer, Color(0xFF2E7D4F))
    Subject.ENGLISH -> SubjectStyle(Icons.Rounded.Book, Color(0xFFC0392B))
    Subject.MATHS -> SubjectStyle(Icons.Rounded.Calculate, Color(0xFF2F6CB3))
    Subject.SCIENCE -> SubjectStyle(Icons.Rounded.Science, Color(0xFF16897A))
    Subject.EVS -> SubjectStyle(Icons.Rounded.Eco, Color(0xFF2E7D4F))
    Subject.SOCIAL -> SubjectStyle(Icons.Rounded.Public, Color(0xFFB7791F))
    Subject.COMPUTER -> SubjectStyle(Icons.Rounded.Computer, Color(0xFF455A64))
    Subject.ART -> SubjectStyle(Icons.Rounded.Palette, Color(0xFF8E44AD))
}

@Composable
fun SubjectIcon(subject: Subject, size: Dp = 40.dp) {
    val style = subjectStyle(subject)
    IconTile(style.icon, tint = style.color, background = style.color.copy(alpha = 0.13f), size = size)
}

@Composable
fun eventColor(type: EventType): Color {
    val f = LocalFeatureColors.current
    return when (type) {
        EventType.HOLIDAY -> f.holiday
        EventType.EXAM -> f.exam
        EventType.EVENT -> f.event
        EventType.MEETING -> f.meeting
    }
}

fun eventIcon(type: EventType): ImageVector = when (type) {
    EventType.HOLIDAY -> Icons.Rounded.BeachAccess
    EventType.EXAM -> Icons.Rounded.Quiz
    EventType.EVENT -> Icons.Rounded.Celebration
    EventType.MEETING -> Icons.Rounded.Groups
}

fun noticeIcon(category: NoticeCategory): ImageVector = when (category) {
    NoticeCategory.DIARY -> Icons.AutoMirrored.Rounded.MenuBook
    NoticeCategory.HOMEWORK -> Icons.Rounded.Edit
    NoticeCategory.ANNOUNCEMENT -> Icons.Rounded.Campaign
    NoticeCategory.NOTICE -> Icons.Rounded.PriorityHigh
}

@Composable
fun noticeColor(category: NoticeCategory): Color {
    val f = LocalFeatureColors.current
    return when (category) {
        NoticeCategory.DIARY -> f.daily
        NoticeCategory.HOMEWORK -> f.weekly
        NoticeCategory.ANNOUNCEMENT -> f.calendar
        NoticeCategory.NOTICE -> f.meeting
    }
}

/* ------------------------------------------------------------------ */
/*  Formatting                                                         */
/* ------------------------------------------------------------------ */

object Fmt {
    private val locale get() = Locale.getDefault()
    fun dayLong(d: LocalDate): String = d.format(DateTimeFormatter.ofPattern("EEEE, d MMMM", locale))
    fun dayShort(d: LocalDate): String = d.format(DateTimeFormatter.ofPattern("EEE, d MMM", locale))
    fun dayMonth(d: LocalDate): String = d.format(DateTimeFormatter.ofPattern("d MMM", locale))
    fun monthYear(d: LocalDate): String = d.format(DateTimeFormatter.ofPattern("MMMM yyyy", locale))
    fun weekday1(d: LocalDate): String = d.dayOfWeek.getDisplayName(TextStyle.NARROW, locale)
    fun weekdayShort(d: LocalDate): String = d.dayOfWeek.getDisplayName(TextStyle.SHORT, locale)

    fun range(start: LocalDate, end: LocalDate): String = when {
        start == end -> dayShort(start)
        start.month == end.month && start.year == end.year ->
            "${start.dayOfMonth} – ${end.dayOfMonth} ${end.format(DateTimeFormatter.ofPattern("MMM", locale))}"
        else -> "${dayMonth(start)} – ${dayMonth(end)}"
    }

    fun relative(time: LocalDateTime, now: LocalDateTime = LocalDateTime.now()): String {
        val mins = ChronoUnit.MINUTES.between(time, now)
        val days = ChronoUnit.DAYS.between(time.toLocalDate(), now.toLocalDate())
        return when {
            mins < 1 -> "Just now"
            mins < 60 -> "$mins min ago"
            days == 0L -> "${mins / 60} h ago"
            days == 1L -> "Yesterday"
            days < 7 -> "$days days ago"
            else -> dayMonth(time.toLocalDate())
        }
    }

    fun inDays(date: LocalDate, today: LocalDate = LocalDate.now()): String {
        val d = ChronoUnit.DAYS.between(today, date)
        return when {
            d < 0 -> "Ongoing"
            d == 0L -> "Today"
            d == 1L -> "Tomorrow"
            else -> "In $d days"
        }
    }

    fun greeting(hour: Int = LocalDateTime.now().hour): String = when (hour) {
        in 5..11 -> "Good morning"
        in 12..16 -> "Good afternoon"
        else -> "Good evening"
    }
}
