package com.mitsschools.parent.ui.screens

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.School
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mitsschools.parent.BuildConfigInfo
import com.mitsschools.parent.R
import com.mitsschools.parent.SchoolConfig
import com.mitsschools.parent.data.model.SchoolClass
import com.mitsschools.parent.ui.AppViewModel
import com.mitsschools.parent.ui.components.AppCard
import com.mitsschools.parent.ui.components.IconTile
import com.mitsschools.parent.ui.components.InitialsAvatar
import com.mitsschools.parent.ui.components.MitsTopBar
import com.mitsschools.parent.ui.theme.Brand
import com.mitsschools.parent.ui.theme.LocalIsDark
import com.mitsschools.parent.ui.theme.ThemeMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    vm: AppViewModel,
    schoolClass: SchoolClass,
    onBack: () -> Unit,
    onChangeClass: () -> Unit,
    onLoggedOut: () -> Unit,
) {
    val session by vm.session.collectAsStateWithLifecycle()
    val themeMode by vm.themeMode.collectAsStateWithLifecycle()
    var confirmLogout by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val name = session?.parentName ?: "Parent"

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        MitsTopBar(title = "Profile & settings", onBack = onBack)
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            AppCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    InitialsAvatar(name, size = 56.dp, background = Brand.Gold, content = Brand.Charcoal)
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(name, style = MaterialTheme.typography.titleLarge)
                        Text(
                            session?.mobile ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            Section("Child") {
                SettingRow(
                    icon = Icons.Rounded.School,
                    title = schoolClass.label,
                    subtitle = "${schoolClass.stage.label} • Tap to change class",
                    onClick = onChangeClass,
                )
            }

            Section("Appearance") {
                Column(Modifier.padding(16.dp)) {
                    Text("Theme", style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(10.dp))
                    SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                        ThemeMode.entries.forEachIndexed { i, mode ->
                            SegmentedButton(
                                selected = themeMode == mode,
                                onClick = { vm.setThemeMode(mode) },
                                shape = SegmentedButtonDefaults.itemShape(i, ThemeMode.entries.size),
                            ) { Text(mode.label) }
                        }
                    }
                }
            }

            Section("Contact the school") {
                SettingRow(
                    icon = Icons.Rounded.Call,
                    title = "Call the school office",
                    subtitle = "${SchoolConfig.OFFICE_PHONE} • ${SchoolConfig.OFFICE_HOURS}",
                    onClick = { open(context, Intent(Intent.ACTION_DIAL, Uri.parse("tel:${SchoolConfig.OFFICE_PHONE}"))) },
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                SettingRow(
                    icon = Icons.Rounded.Email,
                    title = "Email the school",
                    subtitle = SchoolConfig.OFFICE_EMAIL,
                    onClick = {
                        open(
                            context,
                            Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${SchoolConfig.OFFICE_EMAIL}"))
                                .putExtra(Intent.EXTRA_SUBJECT, "Parent query — ${schoolClass.label}"),
                        )
                    },
                )
            }

            Section("About") {
                SettingRow(
                    icon = Icons.Rounded.Info,
                    title = "MITS Parent",
                    subtitle = "Version ${BuildConfigInfo.versionName(context)}",
                    onClick = null,
                )
            }

            OutlinedButton(
                onClick = { confirmLogout = true },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = MaterialTheme.shapes.small,
            ) {
                Icon(Icons.AutoMirrored.Rounded.Logout, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Sign out")
            }

            Image(
                painter = painterResource(if (LocalIsDark.current) R.drawable.logo_horizontal_light else R.drawable.logo_horizontal),
                contentDescription = SchoolConfig.SCHOOL_NAME,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .height(56.dp),
            )
            Text(
                "© ${SchoolConfig.SCHOOL_NAME}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }

    if (confirmLogout) {
        AlertDialog(
            onDismissRequest = { confirmLogout = false },
            title = { Text("Sign out?") },
            text = { Text("You'll need your mobile number and password to sign in again.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmLogout = false
                    vm.logout()
                    onLoggedOut()
                }) { Text("Sign out") }
            },
            dismissButton = { TextButton(onClick = { confirmLogout = false }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Column {
        Text(
            title.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp),
        )
        AppCard(contentPadding = 0.dp) { content() }
    }
}

@Composable
private fun SettingRow(icon: ImageVector, title: String, subtitle: String, onClick: (() -> Unit)?) {
    Row(
        Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconTile(
            icon,
            tint = MaterialTheme.colorScheme.secondary,
            background = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
            size = 40.dp,
        )
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (onClick != null) {
            Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
        }
    }
}

private fun open(context: Context, intent: Intent) {
    try {
        context.startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        Toast.makeText(context, "No app available for this action", Toast.LENGTH_SHORT).show()
    }
}
