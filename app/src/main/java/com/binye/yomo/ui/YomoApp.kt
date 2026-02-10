package com.binye.yomo.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.binye.yomo.ui.screen.auth.LoginScreen
import com.binye.yomo.ui.screen.auth.LoginViewModel
import com.binye.yomo.ui.screen.auth.PhoneAuthScreen
import com.binye.yomo.ui.screen.home.HomeScreen
import com.binye.yomo.ui.screen.onboarding.OnboardingScreen
import com.binye.yomo.ui.screen.reminder.CreateReminderScreen
import com.binye.yomo.ui.screen.reminder.EditReminderScreen
import com.binye.yomo.ui.screen.settings.SettingsScreen
import com.binye.yomo.ui.theme.YomoTheme

object Routes {
    const val LOGIN = "login"
    const val PHONE_AUTH = "phone_auth"
    const val ONBOARDING = "onboarding"
    const val HOME = "home"
    const val CREATE_REMINDER = "create_reminder"
    const val EDIT_REMINDER = "edit_reminder/{reminderId}"
    const val SETTINGS = "settings"

    fun editReminder(id: String) = "edit_reminder/$id"
}

@Composable
fun YomoApp() {
    YomoTheme {
        val navController = rememberNavController()
        val loginViewModel: LoginViewModel = hiltViewModel()
        val isSignedIn by loginViewModel.isSignedIn.collectAsState()
        val appViewModel: AppViewModel = hiltViewModel()

        LaunchedEffect(isSignedIn) {
            if (isSignedIn) {
                appViewModel.onSignedIn()
            }
        }

        val startDestination = if (isSignedIn) Routes.HOME else Routes.LOGIN

        NavHost(
            navController = navController,
            startDestination = startDestination
        ) {
            composable(Routes.LOGIN) {
                LoginScreen(
                    onSignedIn = {
                        navController.navigate(Routes.ONBOARDING) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    },
                    onPhoneAuth = {
                        navController.navigate(Routes.PHONE_AUTH)
                    }
                )
            }

            composable(Routes.PHONE_AUTH) {
                PhoneAuthScreen(
                    onSignedIn = {
                        navController.navigate(Routes.ONBOARDING) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Routes.ONBOARDING) {
                OnboardingScreen(
                    onComplete = {
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.ONBOARDING) { inclusive = true }
                        }
                    }
                )
            }

            composable(Routes.HOME) {
                HomeScreen(
                    onCreateReminder = {
                        navController.navigate(Routes.CREATE_REMINDER)
                    },
                    onEditReminder = { reminderId ->
                        navController.navigate(Routes.editReminder(reminderId))
                    },
                    onSettings = {
                        navController.navigate(Routes.SETTINGS)
                    }
                )
            }

            composable(Routes.CREATE_REMINDER) {
                CreateReminderScreen(
                    onBack = { navController.popBackStack() },
                    onCreated = { navController.popBackStack() }
                )
            }

            composable(
                Routes.EDIT_REMINDER,
                arguments = listOf(navArgument("reminderId") { type = NavType.StringType })
            ) { backStackEntry ->
                val reminderId = backStackEntry.arguments?.getString("reminderId") ?: return@composable
                EditReminderScreen(
                    reminderId = reminderId,
                    onBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() },
                    onDeleted = { navController.popBackStack() }
                )
            }

            composable(Routes.SETTINGS) {
                SettingsScreen(
                    onBack = { navController.popBackStack() },
                    onSignedOut = {
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
