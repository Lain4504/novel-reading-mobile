package com.miraimagiclab.novelreadingapp.ui.auth

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.miraimagiclab.novelreadingapp.ui.navigation.Route
import io.lain4504.novelreadingapp.api.ui.LocalNavController

fun NavGraphBuilder.authNavigation() {
    navigation<Route.Auth>(
        startDestination = Route.Auth.Login
    ) {
        authDestination()
    }
}

fun NavGraphBuilder.authDestination() {
    composable<Route.Auth.Login> {
        val navController = LocalNavController.current
        val authViewModel = hiltViewModel<AuthViewModel>()
        
        LoginScreen(
            viewModel = authViewModel,
            onNavigateToRegister = {
                navController.navigate(Route.Auth.Register)
            },
            onNavigateToForgotPassword = {
                navController.navigate(Route.Auth.ForgotPassword)
            },
            onLoginSuccess = {
                navController.popBackStack()
            }
        )
    }
    
    composable<Route.Auth.Register> {
        val navController = LocalNavController.current
        val authViewModel = hiltViewModel<AuthViewModel>()
        
        RegisterScreen(
            viewModel = authViewModel,
            onNavigateToLogin = {
                navController.navigate(Route.Auth.Login) {
                    popUpTo(Route.Auth.Login) { inclusive = true }
                }
            },
            onNavigateToVerifyEmail = { email ->
                // Navigate to verify email screen if needed
                navController.popBackStack()
            }
        )
    }
    
    composable<Route.Auth.ForgotPassword> {
        val navController = LocalNavController.current
        val authViewModel = hiltViewModel<AuthViewModel>()
        
        ForgotPasswordScreen(
            viewModel = authViewModel,
            onResetSuccess = {
                navController.navigate(Route.Auth.Login) {
                    popUpTo(Route.Auth.Login) { inclusive = true }
                }
            },
            onNavigateToLogin = {
                navController.navigate(Route.Auth.Login) {
                    popUpTo(Route.Auth.Login) { inclusive = true }
                }
            }
        )
    }
}

@Suppress("unused")
fun NavController.navigateToAuthDestination() {
    navigate(Route.Auth)
}

fun NavController.navigateToLogin() {
    navigate(Route.Auth.Login)
}

