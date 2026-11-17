package com.mitsschools.parent.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.BeachAccess
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.Today
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mitsschools.parent.data.model.DailyDiary
import com.mitsschools.parent.data.model.DiaryEntry
import com.mitsschools.parent.data.model.SchoolClass
import com.mitsschools.parent.ui.AppViewModel
import com.mitsschools.parent.ui.components.AppCard
import com.mitsschools.parent.ui.components.EmptyState
import com.mitsschools.parent.ui.components.Fmt
import com.mitsschools.parent.ui.components.LoadingBlock
import com.mitsschools.parent.ui.components.MitsTopBar
import com.mitsschools.parent.ui.components.SubjectIcon
import com.mitsschools.parent.ui.theme.Brand
import com.mitsschools.parent.ui.theme.LocalFeatureColors
import com.mitsschools.parent.ui.theme.LocalIsDark
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

@Composable
fun DailyDiaryScreen(vm: AppViewModel, schoolClass: SchoolClass) {
    val today = remember { LocalDate.now() }
    var selectedEpoch by rememberSaveable { mutableStateOf(today.toEpochDay()) }
    val selected = LocalDate.ofEpochDay(selectedEpoch)
    val done by vm.doneHomework.collectAsStateWithLifecycle()

    var diary by remember { mutableStateOf<DailyDiary?>(null) }
    LaunchedEffect(schoolClass, selectedEpoch) {
        diary = null
        diary = vm.dailyDiary(schoolClass, selected)
    }

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        MitsTopBar(
            title = "Daily Diary",
            subtitle = "${schoolClass.label} • ${Fmt.dayShort(selected)}",
            actions = {
                if (selected != today) {
                    IconButton(onClick = { selectedEpoch = today.toEpochDay() }) {
                        Icon(Icons.Rounded.Today, contentDescription = "Go to today")
                    }
                }
            },
        )
        WeekStrip(
            selected = selected,
            today = today,
            onSelect = { selectedEpoch = it.toEpochDay() },
        )

        val d = diary
        when {
            d == null -> LoadingBlock()
            !d.isSchoolDay -> EmptyState(
                icon = Icons.Rounded.BeachAccess,
                title = "No school on ${Fmt.dayShort(d.date)}",
                message = (d.closedReason ?: "School is closed") + ". Choose another day to see the diary.",
            )
            else -> {
                val withHw = d.entries.filter { it.homework != null }
                val doneCount = withHw.count { vm.homeworkKey(schoolClass, d.date, it.subject) in done }
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    if (withHw.isNotEmpty()) {
                        item { HomeworkProgress(doneCount, withHw.size) }
                    }
                    d.teacherNote?.let { note ->
                        item { TeacherNote(note) }
                    }
                    items(d.entries, key = { it.subject.name }) { entry ->
                        val key = vm.homeworkKey(schoolClass, d.date, entry.subject)
                        DiaryEntryCard(entry, isDone = key in done, onToggle = { vm.toggleHomework(key) })
                    }
                }
            }
        }
    }
}

@Composable
private fun WeekStrip(selected: LocalDate, today: LocalDate, onSelect: (LocalDate) -> Unit) {
    val monday = selected.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    val days = (0L..5L).map { monday.plusDays(it) } // Mon – Sat
    Column(
        Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .padding(vertical = 10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 4.dp)) {
            IconButton(onClick = { onSelect(selected.minusWeeks(1)) }) {
                Icon(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, contentDescription = "Previous week")
            }
            Text(
                Fmt.monthYear(selected),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.weight(1f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
            IconButton(onClick = { onSelect(selected.plusWeeks(1)) }) {
                Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = "Next week")
            }
        }
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            days.forEach { day ->
                val isSel = day == selected
                val isToday = day == today
                Column(
                    Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isSel) MaterialTheme.colorScheme.primary else Color.Transparent)
                        .clickable { onSelect(day) }
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        Fmt.weekdayShort(day),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        day.dayOfMonth.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        color = if (isSel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                    )
                    Box(
                        Modifier
                            .padding(top = 3.dp)
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isToday && isSel -> MaterialTheme.colorScheme.onPrimary
                                    isToday -> MaterialTheme.colorScheme.primary
                                    else -> Color.Transparent
                                },
                            ),
                    )
                }
            }
        }
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
}

@Composable
private fun HomeworkProgress(done: Int, total: Int) {
    val f = LocalFeatureColors.current
    AppCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Homework", style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
            Text(
                if (done == total) "All done!" else "$done of $total done",
                style = MaterialTheme.typography.labelLarge,
                color = if (done == total) f.success else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.height(10.dp))
        LinearProgressIndicator(
            progress = { if (total == 0) 0f else done / total.toFloat() },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
            color = f.success,
            trackColor = f.success.copy(alpha = 0.15f),
            drawStopIndicator = {},
        )
        Spacer(Modifier.height(6.dp))
        Text(
            "Tick each subject's homework once your child has finished it.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun TeacherNote(note: String) {
    val dark = LocalIsDark.current
    AppCard(
        containerColor = if (dark) Color(0xFF2E2719) else Brand.Cream,
        border = null,
    ) {
        Row {
            Icon(Icons.Rounded.Lightbulb, contentDescription = null, tint = Brand.Gold)
            Spacer(Modifier.width(12.dp))
            Column {
                Text("Note from the class teacher", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(2.dp))
                Text(note, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun DiaryEntryCard(entry: DiaryEntry, isDone: Boolean, onToggle: () -> Unit) {
    val f = LocalFeatureColors.current
    AppCard(contentPadding = 0.dp) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            SubjectIcon(entry.subject)
            Spacer(Modifier.width(12.dp))
            Text(entry.subject.title, style = MaterialTheme.typography.titleMedium)
        }
        Column(Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)) {
            Label("CLASSWORK")
            Text(entry.classwork, style = MaterialTheme.typography.bodyMedium)
        }
        val hw = entry.homework
        if (hw != null) {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggle)
                    .background(if (isDone) f.success.copy(alpha = 0.07f) else Color.Transparent)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Label("HOMEWORK", color = f.daily)
                    Text(
                        hw,
                        style = MaterialTheme.typography.bodyMedium,
                        textDecoration = if (isDone) TextDecoration.LineThrough else null,
                        color = if (isDone) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                    )
                }
                Spacer(Modifier.width(12.dp))
                Icon(
                    if (isDone) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                    contentDescription = if (isDone) "Mark homework as not done" else "Mark homework as done",
                    tint = if (isDone) f.success else MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(28.dp),
                )
            }
        } else {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
            Text(
                "No homework for this subject today",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            )
        }
    }
}

@Composable
internal fun Label(text: String, color: Color = MaterialTheme.colorScheme.onSurfaceVariant) {
    Text(
        text,
        style = MaterialTheme.typography.labelSmall,
        color = color,
        modifier = Modifier.padding(bottom = 2.dp),
    )
}
