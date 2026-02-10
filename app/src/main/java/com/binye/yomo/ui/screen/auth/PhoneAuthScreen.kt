package com.binye.yomo.ui.screen.auth

import android.app.Activity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.binye.yomo.ui.component.FormField
import com.binye.yomo.ui.component.GradientBackground
import com.binye.yomo.ui.component.PrimaryButton
import com.binye.yomo.ui.theme.Spacing
import com.binye.yomo.ui.theme.YomoColors
import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhoneAuthScreen(
    onSignedIn: () -> Unit,
    onBack: () -> Unit,
    viewModel: LoginScreenViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var phoneNumber by remember { mutableStateOf("") }
    var verificationCode by remember { mutableStateOf("") }
    var verificationId by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    GradientBackground {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = { Text("Phone Sign In") },
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
                    containerColor = Color.Transparent,
                    titleContentColor = YomoColors.TextPrimary
                )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = Spacing.xl),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (verificationId == null) {
                    Text(
                        text = "Enter your phone number",
                        style = MaterialTheme.typography.headlineMedium,
                        color = YomoColors.TextPrimary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(Spacing.sm))

                    Text(
                        text = "We'll send you a verification code",
                        style = MaterialTheme.typography.bodyMedium,
                        color = YomoColors.TextSecondary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(Spacing.xl))

                    FormField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it },
                        label = "Phone number",
                        placeholder = "+1 234 567 8900",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )

                    Spacer(modifier = Modifier.height(Spacing.lg))

                    PrimaryButton(
                        text = "Send Code",
                        onClick = {
                            isLoading = true
                            errorMessage = null
                            val activity = context as Activity
                            viewModel.authRepository.sendVerificationCode(
                                phoneNumber = phoneNumber,
                                activity = activity,
                                callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                                    override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                                        scope.launch {
                                            try {
                                                viewModel.authRepository.signInWithPhoneCredential(credential)
                                                onSignedIn()
                                            } catch (e: Exception) {
                                                errorMessage = e.message
                                            } finally {
                                                isLoading = false
                                            }
                                        }
                                    }

                                    override fun onVerificationFailed(e: FirebaseException) {
                                        errorMessage = e.message ?: "Verification failed"
                                        isLoading = false
                                    }

                                    override fun onCodeSent(
                                        id: String,
                                        token: PhoneAuthProvider.ForceResendingToken
                                    ) {
                                        verificationId = id
                                        isLoading = false
                                    }
                                }
                            )
                        },
                        enabled = phoneNumber.isNotBlank(),
                        isLoading = isLoading
                    )
                } else {
                    Text(
                        text = "Enter verification code",
                        style = MaterialTheme.typography.headlineMedium,
                        color = YomoColors.TextPrimary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(Spacing.sm))

                    Text(
                        text = "Sent to $phoneNumber",
                        style = MaterialTheme.typography.bodyMedium,
                        color = YomoColors.TextSecondary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(Spacing.xl))

                    FormField(
                        value = verificationCode,
                        onValueChange = { verificationCode = it },
                        label = "6-digit code",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    Spacer(modifier = Modifier.height(Spacing.lg))

                    PrimaryButton(
                        text = "Verify",
                        onClick = {
                            scope.launch {
                                isLoading = true
                                errorMessage = null
                                try {
                                    val credential = PhoneAuthProvider.getCredential(
                                        verificationId!!,
                                        verificationCode
                                    )
                                    viewModel.authRepository.signInWithPhoneCredential(credential)
                                    onSignedIn()
                                } catch (e: Exception) {
                                    errorMessage = e.message ?: "Verification failed"
                                } finally {
                                    isLoading = false
                                }
                            }
                        },
                        enabled = verificationCode.length == 6,
                        isLoading = isLoading
                    )
                }

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
}
