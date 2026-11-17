package com.mitsschools.parent.ui.screens

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.EventAvailable
import androidx.compose.material.icons.rounded.EventBusy
import androidx.compose.material.icons.rounded.Today
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mitsschools.parent.data.model.EventType
import com.mitsschools.parent.data.model.SchoolClass
import com.mitsschools.parent.data.model.SchoolEvent
import com.mitsschools.parent.ui.AppViewModel
import com.mitsschools.parent.ui.components.AppCard
import com.mitsschools.parent.ui.components.DateBlock
import com.mitsschools.parent.ui.components.EmptyState
import com.mitsschools.parent.ui.components.Fmt
import com.mitsschools.parent.ui.components.IconTile
import com.mitsschools.parent.ui.components.MitsTopBar
import com.mitsschools.parent.ui.components.Pill
import com.mitsschools.parent.ui.components.eventColor
import com.mitsschools.parent.ui.components.eventIcon
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(vm: AppViewModel, schoolClass: SchoolClass) {
    val events by vm.events.collectAsStateWithLifecycle()
    val today = remember { LocalDate.now() }
    var monthKey by rememberSaveable { mutableStateOf(YearMonth.from(today).toString()) }
    val month = YearMonth.parse(monthKey)
    var selectedEpoch by rememberSaveable { mutableStateOf<Long?>(null) }
    val selectedDay = selectedEpoch?.let { LocalDate.ofEpochDay(it) }
    var filter by rememberSaveable { mutableStateOf<EventType?>(null) }
    var openEventId by rememberSaveable { mutableStateOf<String?>(null) }

    val visible = events.filter { filter == null || it.type == filter }
    val monthStart = month.atDay(1)
    val monthEnd = month.atEndOfMonth()
    val listed = visible
        .filter { e ->
            if (selectedDay != null) e.covers(selectedDay)
            else !e.end.isBefore(monthStart) && !e.start.isAfter(monthEnd)
        }
        .sortedBy { it.start }

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        MitsTopBar(
            title = "Yearly Calendar",
            subtitle = "${schoolClass.label} • Academic year",
            actions = {
                if (month != YearMonth.from(today) || selectedDay != null) {
                    IconButton(onClick = { monthKey = YearMonth.from(today).toString(); selectedEpoch = null }) {
                        Icon(Icons.Rounded.Today, contentDescription = "Go to this month")
                    }
                }
            },
        )

        LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
            item {
                Column(
                    Modifier
                        .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                        .padding(bottom = 12.dp),
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconButton(onClick = { monthKey = month.minusMonths(1).toString(); selectedEpoch = null }) {
                            Icon(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, contentDescription = "Previous month")
                        }
                        Text(
                            Fmt.monthYear(monthStart),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                        )
                        IconButton(onClick = { monthKey = month.plusMonths(1).toString(); selectedEpoch = null }) {
                            Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = "Next month")
                        }
                    }
                    MonthGrid(
                        month = month,
                        today = today,
                        selected = selectedDay,
                        events = visible,
                        onSelect = { day -> selectedEpoch = if (day == selectedDay) null else day.toEpochDay() },
                    )
                    // Legend / filter
                    Row(
                        Modifier
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        FilterChip(
                            selected = filter == null,
                            onClick = { filter = null },
                            label = { Text("All") },
                        )
                        EventType.entries.forEach { type ->
                            val c = eventColor(type)
                            FilterChip(
                                selected = filter == type,
                                onClick = { filter = if (filter == type) null else type },
                                label = { Text(type.plural) },
                                leadingIcon = {
                                    Box(Modifier.size(10.dp).clip(CircleShape).background(c))
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = c.copy(alpha = 0.16f),
                                    selectedLabelColor = MaterialTheme.colorScheme.onSurface,
                                ),
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    if (selectedDay != null) Fmt.dayLong(selectedDay) else "In ${monthStart.month.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.getDefault())}",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 8.dp),
                )
            }

            if (listed.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Rounded.EventBusy,
                        title = "Nothing scheduled",
                        message = if (selectedDay != null) "A regular school day — no special events." else "No events this month for the selected filter.",
                    )
                }
            } else {
                items(listed, key = { it.id }) { event ->
                    EventRow(event, today) { openEventId = event.id }
                }
            }
        }
    }

    val open = events.firstOrNull { it.id == openEventId }
    if (open != null) {
        val context = LocalContext.current
        ModalBottomSheet(onDismissRequest = { openEventId = null }) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(start = 24.dp, end = 24.dp, bottom = 24.dp),
            ) {
                val c = eventColor(open.type)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconTile(eventIcon(open.type), tint = c, background = c.copy(alpha = 0.14f), size = 48.dp)
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Pill(open.type.label, c)
                        Spacer(Modifier.height(4.dp))
                        Text(open.title, style = MaterialTheme.typography.titleLarge)
                    }
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    if (open.isMultiDay) "${Fmt.dayShort(open.start)} – ${Fmt.dayShort(open.end)}" else Fmt.dayLong(open.start),
                    style = MaterialTheme.typography.titleSmall,
                    color = c,
                )
                Spacer(Modifier.height(8.dp))
                Text(open.description, style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = { addToPhoneCalendar(context, open) },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = MaterialTheme.shapes.small,
                ) {
                    Icon(Icons.Rounded.EventAvailable, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Add to my calendar")
                }
                TextButton(onClick = { openEventId = null }, modifier = Modifier.fillMaxWidth()) {
                    Text("Close")
                }
            }
        }
    }
}

@Composable
private fun MonthGrid(
    month: YearMonth,
    today: LocalDate,
    selected: LocalDate?,
    events: List<SchoolEvent>,
    onSelect: (LocalDate) -> Unit,
) {
    val first = month.atDay(1)
    val leading = first.dayOfWeek.value - DayOfWeek.MONDAY.value // Monday-first grid
    val cells = leading + month.lengthOfMonth()
    val rows = (cells + 6) / 7

    Column(Modifier.padding(horizontal = 8.dp)) {
        Row(Modifier.fillMaxWidth()) {
            (0 until 7).forEach { i ->
                Text(
                    Fmt.weekday1(first.with(DayOfWeek.MONDAY).plusDays(i.toLong())),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f),
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        for (r in 0 until rows) {
            Row(Modifier.fillMaxWidth()) {
                for (c in 0 until 7) {
                    val index = r * 7 + c - leading
                    Box(Modifier.weight(1f).aspectRatio(1f).padding(2.dp), contentAlignment = Alignment.Center) {
                        if (index in 0 until month.lengthOfMonth()) {
                            val day = month.atDay(index + 1)
                            DayCell(
                                day = day,
                                isToday = day == today,
                                isSelected = day == selected,
                                isWeekend = c >= 5,
                                dayEvents = events.filter { it.covers(day) },
                                onClick = { onSelect(day) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    day: LocalDate,
    isToday: Boolean,
    isSelected: Boolean,
    isWeekend: Boolean,
    dayEvents: List<SchoolEvent>,
    onClick: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val holiday = dayEvents.firstOrNull { it.type == EventType.HOLIDAY }
    val holidayTint = holiday?.let { eventColor(it.type).copy(alpha = 0.12f) } ?: Color.Transparent
    Column(
        Modifier
            .fillMaxSize()
            .clip(CircleShape)
            .background(
                when {
                    isSelected -> scheme.primary
                    else -> holidayTint
                },
            )
            .then(if (isToday && !isSelected) Modifier.border(1.5.dp, scheme.primary, CircleShape) else Modifier)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            day.dayOfMonth.toString(),
            style = MaterialTheme.typography.bodyMedium,
            color = when {
                isSelected -> scheme.onPrimary
                isWeekend -> scheme.onSurfaceVariant.copy(alpha = 0.7f)
                else -> scheme.onSurface
            },
        )
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.height(6.dp)) {
            dayEvents.map { it.type }.distinct().take(3).forEach { t ->
                val c = eventColor(t)
                Box(
                    Modifier
                        .padding(top = 1.dp)
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) scheme.onPrimary else c),
                )
            }
        }
    }
}

@Composable
private fun EventRow(event: SchoolEvent, today: LocalDate, onClick: () -> Unit) {
    val c = eventColor(event.type)
    AppCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 5.dp),
        contentPadding = 12.dp,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            DateBlock(event.start, c)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(event.title, style = MaterialTheme.typography.titleSmall)
                Text(
                    "${event.type.label} • ${Fmt.range(event.start, event.end)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (!event.end.isBefore(today)) {
                Pill(Fmt.inDays(event.start, today), c)
            }
        }
    }
}

private fun addToPhoneCalendar(context: Context, event: SchoolEvent) {
    val zone = ZoneId.systemDefault()
    val begin = event.start.atStartOfDay(zone).toInstant().toEpochMilli()
    val end = event.end.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
    val intent = Intent(Intent.ACTION_INSERT)
        .setData(CalendarContract.Events.CONTENT_URI)
        .putExtra(CalendarContract.Events.TITLE, "${event.title} — MITS Schools")
        .putExtra(CalendarContract.Events.DESCRIPTION, event.description)
        .putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, true)
        .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, begin)
        .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, end)
    try {
        context.startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        Toast.makeText(context, "No calendar app found on this device", Toast.LENGTH_SHORT).show()
    }
}
