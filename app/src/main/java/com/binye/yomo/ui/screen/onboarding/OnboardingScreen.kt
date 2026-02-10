package com.binye.yomo.ui.screen.onboarding

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.binye.yomo.R
import com.binye.yomo.ui.component.GradientBackground
import com.binye.yomo.ui.component.PrimaryButton
import com.binye.yomo.ui.theme.Spacing
import com.binye.yomo.ui.theme.YomoColors
import kotlinx.coroutines.delay

@Composable
fun OnboardingScreen(onComplete: () -> Unit) {
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ ->
        onComplete()
    }

    var showLogo by remember { mutableStateOf(false) }
    var showTitle by remember { mutableStateOf(false) }
    var showBody by remember { mutableStateOf(false) }
    var showButtons by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        showLogo = true
        delay(200)
        showTitle = true
        delay(200)
        showBody = true
        delay(200)
        showButtons = true
    }

    GradientBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(Spacing.xl),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedVisibility(
                visible = showLogo,
                enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { -it / 4 }
            ) {
                Image(
                    painter = painterResource(id = R.drawable.yomo_logo),
                    contentDescription = "Yomo",
                    modifier = Modifier.size(100.dp)
                )
            }

            Spacer(modifier = Modifier.height(Spacing.lg))

            AnimatedVisibility(
                visible = showTitle,
                enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { it / 4 }
            ) {
                Text(
                    text = "Welcome to Yomo!",
                    style = MaterialTheme.typography.headlineLarge,
                    color = YomoColors.TextPrimary,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(Spacing.sm))

            AnimatedVisibility(
                visible = showBody,
                enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { it / 4 }
            ) {
                Text(
                    text = "Your moment. Don't miss it.\n\nWe need notification permission to remind you at the right time.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = YomoColors.TextSecondary,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(Spacing.xxl))

            AnimatedVisibility(
                visible = showButtons,
                enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { it / 4 }
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    PrimaryButton(
                        text = "Enable Notifications",
                        onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                onComplete()
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(Spacing.sm))

                    TextButton(onClick = onComplete) {
                        Text(
                            "Skip for now",
                            color = YomoColors.TextTertiary
                        )
                    }
                }
            }
        }
    }
}
