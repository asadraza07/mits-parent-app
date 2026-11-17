package com.mitsschools.parent.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.Today
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mitsschools.parent.data.model.SchoolClass
import com.mitsschools.parent.data.model.WeeklyDiary
import com.mitsschools.parent.data.model.WeeklyPlan
import com.mitsschools.parent.ui.AppViewModel
import com.mitsschools.parent.ui.components.AppCard
import com.mitsschools.parent.ui.components.Bullet
import com.mitsschools.parent.ui.components.Fmt
import com.mitsschools.parent.ui.components.LoadingBlock
import com.mitsschools.parent.ui.components.MitsTopBar
import com.mitsschools.parent.ui.components.Pill
import com.mitsschools.parent.ui.components.SubjectIcon
import com.mitsschools.parent.ui.components.subjectStyle
import com.mitsschools.parent.ui.theme.LocalFeatureColors
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

@Composable
fun WeeklyDiaryScreen(vm: AppViewModel, schoolClass: SchoolClass) {
    val thisMonday = remember { LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)) }
    var mondayEpoch by rememberSaveable { mutableStateOf(thisMonday.toEpochDay()) }
    val monday = LocalDate.ofEpochDay(mondayEpoch)
    val f = LocalFeatureColors.current

    var diary by remember { mutableStateOf<WeeklyDiary?>(null) }
    LaunchedEffect(schoolClass, mondayEpoch) {
        diary = null
        diary = vm.weeklyDiary(schoolClass, monday)
    }

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        MitsTopBar(
            title = "Weekly Diary",
            subtitle = schoolClass.label,
            actions = {
                if (monday != thisMonday) {
                    IconButton(onClick = { mondayEpoch = thisMonday.toEpochDay() }) {
                        Icon(Icons.Rounded.Today, contentDescription = "This week")
                    }
                }
            },
        )

        // Week selector
        Row(
            Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                .padding(horizontal = 4.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = { mondayEpoch = monday.minusWeeks(1).toEpochDay() }) {
                Icon(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, contentDescription = "Previous week")
            }
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "Week of ${Fmt.range(monday, monday.plusDays(5))}",
                    style = MaterialTheme.typography.titleSmall,
                    textAlign = TextAlign.Center,
                )
                Text(
                    when (monday) {
                        thisMonday -> "This week"
                        thisMonday.plusWeeks(1) -> "Next week"
                        thisMonday.minusWeeks(1) -> "Last week"
                        else -> monday.year.toString()
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = f.weekly,
                )
            }
            IconButton(onClick = { mondayEpoch = monday.plusWeeks(1).toEpochDay() }) {
                Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = "Next week")
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))

        val d = diary
        if (d == null) {
            LoadingBlock()
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    AppCard(containerColor = f.weeklyBg, border = null) {
                        Row {
                            Icon(Icons.Rounded.Flag, contentDescription = null, tint = f.weekly)
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text("This week's focus", style = MaterialTheme.typography.titleSmall)
                                Spacer(Modifier.height(2.dp))
                                Text(d.focus, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
                items(d.plans, key = { it.subject.name }) { plan ->
                    PlanCard(plan, initiallyExpanded = d.plans.indexOf(plan) < 2)
                }
            }
        }
    }
}

@Composable
private fun PlanCard(plan: WeeklyPlan, initiallyExpanded: Boolean) {
    var expanded by rememberSaveable(plan.subject, plan.topic) { mutableStateOf(initiallyExpanded) }
    val color = subjectStyle(plan.subject).color
    val f = LocalFeatureColors.current
    AppCard(contentPadding = 0.dp) {
        Row(
            Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SubjectIcon(plan.subject)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(plan.subject.title, style = MaterialTheme.typography.titleMedium)
                Text(
                    plan.topic,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(
                if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                contentDescription = if (expanded) "Collapse" else "Expand",
            )
        }
        AnimatedVisibility(expanded) {
            Column(Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)) {
                Label("SYLLABUS")
                Text(plan.topic, style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(12.dp))
                Label("LEARNING OBJECTIVES")
                plan.objectives.forEach { Bullet(it, color) }
                Spacer(Modifier.height(12.dp))
                Label("HOMEWORK", color = f.weekly)
                if (plan.homework != null) {
                    Text(plan.homework, style = MaterialTheme.typography.bodyMedium)
                } else {
                    Pill("No weekly homework", MaterialTheme.colorScheme.outline)
                }
            }
        }
    }
}
