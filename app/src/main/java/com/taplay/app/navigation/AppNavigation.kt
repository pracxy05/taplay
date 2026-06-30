package com.taplay.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.taplay.app.screens.*

// ── All screen routes defined in one place
object Routes {
    const val SPLASH       = "splash"
    const val ONBOARDING   = "onboarding"
    const val LOGIN        = "login"
    const val HOME         = "home"
    const val TAP          = "tap"
    const val SESSION      = "session"
    const val TRANSACTIONS = "transactions"
    const val PROFILE      = "profile"
    const val AVATAR       = "avatar"
}

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH
    ) {
        composable(Routes.SPLASH) {
            SplashScreen(
                onNavigateNext = {
                    navController.navigate(Routes.ONBOARDING) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                onGetStarted = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.HOME) {
            HomeScreen(
                onTapToPlay = {
                    navController.navigate(Routes.TAP)
                },
                onViewTransactions = {
                    navController.navigate(Routes.TRANSACTIONS)
                },
                onOpenProfile = {
                    navController.navigate(Routes.PROFILE)
                },
                onOpenAvatar = {
                    navController.navigate(Routes.AVATAR)
                }
            )
        }

        composable(Routes.TAP) {
            TapScreen(
                onSessionStarted = {
                    navController.navigate(Routes.SESSION) {
                        popUpTo(Routes.TAP) { inclusive = true }
                    }
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.SESSION) {
            SessionActiveScreen(
                onSessionEnded = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.HOME) { inclusive = false }
                    }
                }
            )
        }

        composable(Routes.TRANSACTIONS) {
            TransactionScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.PROFILE) {
            ProfileScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        // Avatar Screen
        composable(Routes.AVATAR) {
            AvatarScreen(
                onBack = {
                    navController.popBackStack()
                },
                onCharacterConfirmed = { charId ->
                    navController.popBackStack()
                }
            )
        }
    }
}