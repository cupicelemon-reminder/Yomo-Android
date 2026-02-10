package com.binye.yomo.ui.screen.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.binye.yomo.ui.component.GlassCard
import com.binye.yomo.ui.theme.Spacing
import com.binye.yomo.ui.theme.YomoColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onSignedOut: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    var showSignOutDialog by remember { mutableStateOf(false) }
    val user = viewModel.currentUser

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(YomoColors.Background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = YomoColors.TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = YomoColors.Background,
                    titleContentColor = YomoColors.TextPrimary
                )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = Spacing.md),
                verticalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                Spacer(modifier = Modifier.height(Spacing.sm))

                SectionTitle("Account")
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        SettingsRow(
                            icon = Icons.Default.Person,
                            label = "Signed in as",
                            value = user?.displayName
                                ?: user?.phoneNumber
                                ?: user?.email
                                ?: "Unknown"
                        )
                    }
                }

                SectionTitle("Appearance")
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        SettingsRow(
                            icon = Icons.Default.DarkMode,
                            label = "Theme",
                            value = "System default"
                        )
                    }
                }

                SectionTitle("About")
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        SettingsRow(
                            icon = Icons.Default.Info,
                            label = "Version",
                            value = "1.0.0"
                        )
                    }
                }

                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    TextButton(
                        onClick = { showSignOutDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Logout,
                            contentDescription = null,
                            tint = YomoColors.OverdueRed
                        )
                        Spacer(modifier = Modifier.padding(horizontal = Spacing.xs))
                        Text(
                            "Sign Out",
                            color = YomoColors.OverdueRed,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.xl))
            }
        }
    }

    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            title = { Text("Sign Out") },
            text = { Text("Are you sure you want to sign out?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.signOut()
                    showSignOutDialog = false
                    onSignedOut()
                }) {
                    Text("Sign Out", color = YomoColors.OverdueRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) {
                    Text("Cancel")
                }
            },
            containerColor = YomoColors.Surface,
            titleContentColor = YomoColors.TextPrimary,
            textContentColor = YomoColors.TextSecondary
        )
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = YomoColors.TextTertiary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = Spacing.xs)
    )
}

@Composable
private fun SettingsRow(icon: ImageVector, label: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = YomoColors.TextSecondary
        )
        Spacer(modifier = Modifier.padding(horizontal = 6.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = YomoColors.TextPrimary
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = YomoColors.TextTertiary
            )
        }
    }
}
