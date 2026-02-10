package com.binye.yomo.ui.screen.auth

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.binye.yomo.R
import com.binye.yomo.data.repository.AuthRepository
import com.binye.yomo.ui.component.AuthButton
import com.binye.yomo.ui.component.GradientBackground
import com.binye.yomo.ui.theme.Spacing
import com.binye.yomo.ui.theme.YomoColors
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onSignedIn: () -> Unit,
    onPhoneAuth: () -> Unit,
    authRepository: AuthRepository = hiltViewModel<LoginScreenViewModel>().authRepository
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val infiniteTransition = rememberInfiniteTransition(label = "float")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 12f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatOffset"
    )

    GradientBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Spacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.yomo_logo),
                contentDescription = "Yomo",
                modifier = Modifier
                    .size(120.dp)
                    .offset { IntOffset(0, floatOffset.toInt()) }
            )

            Spacer(modifier = Modifier.height(Spacing.sm))

            Text(
                text = "Your moment. Don't miss it.",
                style = MaterialTheme.typography.bodyLarge,
                color = YomoColors.TextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(64.dp))

            AuthButton(
                text = "Continue with Google",
                onClick = {
                    scope.launch {
                        isLoading = true
                        errorMessage = null
                        try {
                            authRepository.signInWithGoogle(context)
                            onSignedIn()
                        } catch (e: Exception) {
                            errorMessage = e.message ?: "Sign in failed"
                        } finally {
                            isLoading = false
                        }
                    }
                },
                enabled = !isLoading,
                isLoading = isLoading
            )

            Spacer(modifier = Modifier.height(Spacing.md))

            AuthButton(
                text = "Continue with Phone",
                onClick = onPhoneAuth,
                enabled = !isLoading,
                icon = Icons.Default.Phone
            )

            errorMessage?.let { msg ->
                Spacer(modifier = Modifier.height(Spacing.md))
                Text(
                    text = msg,
                    color = YomoColors.OverdueRed,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
