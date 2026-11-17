package com.mitsschools.parent.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mitsschools.parent.data.model.SchoolClass
import com.mitsschools.parent.data.model.Stage
import com.mitsschools.parent.ui.components.MitsTopBar
import com.mitsschools.parent.ui.theme.Brand

@Composable
fun ClassSelectScreen(
    current: SchoolClass?,
    onBack: (() -> Unit)?,
    onConfirm: (SchoolClass) -> Unit,
) {
    var selected by rememberSaveable { mutableStateOf(current) }

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        MitsTopBar(
            title = "Select child's class",
            subtitle = "You can change this anytime",
            onBack = onBack,
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    "Choose the class your child is enrolled in. The diary, calendar and notifications will be tailored to it.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Stage.entries.forEach { stage ->
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Text(
                        stage.label.uppercase(),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
                items(SchoolClass.entries.filter { it.stage == stage }, key = { it.name }) { cls ->
                    ClassCard(cls, selected == cls) { selected = cls }
                }
            }
        }

        Surface(tonalElevation = 3.dp, shadowElevation = 8.dp) {
            Column(Modifier.navigationBarsPadding().padding(16.dp)) {
                Button(
                    onClick = { selected?.let(onConfirm) },
                    enabled = selected != null,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = MaterialTheme.shapes.small,
                ) {
                    Text(
                        selected?.let { "Continue with ${it.label}" } ?: "Select a class to continue",
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }
        }
    }
}

@Composable
private fun ClassCard(cls: SchoolClass, isSelected: Boolean, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val accent = if (cls.isEarlyYears) Brand.Gold else scheme.primary
    Card(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) scheme.primaryContainer.copy(alpha = 0.55f) else scheme.surfaceContainerLowest,
        ),
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, if (isSelected) scheme.primary else scheme.outlineVariant),
    ) {
        Box(Modifier.fillMaxWidth()) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 18.dp, horizontal = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(accent.copy(alpha = if (isSelected) 1f else 0.15f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        cls.badge,
                        style = MaterialTheme.typography.titleMedium,
                        color = when {
                            !isSelected -> accent
                            cls.isEarlyYears -> Brand.Charcoal
                            else -> scheme.onPrimary
                        },
                    )
                }
                Spacer(Modifier.height(10.dp))
                Text(cls.label, style = MaterialTheme.typography.labelLarge, textAlign = TextAlign.Center)
            }
            if (isSelected) {
                Icon(
                    Icons.Rounded.CheckCircle,
                    contentDescription = "Selected",
                    tint = scheme.primary,
                    modifier = Modifier.align(Alignment.TopEnd).padding(6.dp).size(20.dp),
                )
            }
        }
    }
}
