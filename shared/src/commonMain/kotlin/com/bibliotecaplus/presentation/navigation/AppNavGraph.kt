package com.bibliotecaplus.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bibliotecaplus.presentation.auth.LoginScreen
import com.bibliotecaplus.presentation.books.BookDetailScreen
import com.bibliotecaplus.presentation.home.HomeScreen
import com.bibliotecaplus.presentation.notifications.NotificationsScreen
import com.bibliotecaplus.presentation.profile.EditProfileScreen

private object Routes {
    const val LOGIN = "login"
    const val HOME = "home"
    const val BOOK_DETAIL = "book/{bookId}"
    const val NOTIFICATIONS = "notifications"
    const val PROFILE_EDIT = "profile/edit"
    fun bookDetail(id: String) = "book/$id"
}

@Composable
fun AppNavGraph(startLoggedIn: Boolean) {
    val navController = rememberNavController()
    val startDest = if (startLoggedIn) Routes.HOME else Routes.LOGIN

    NavHost(navController = navController, startDestination = startDest) {

        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
            )
        }

        composable(Routes.HOME) {
            HomeScreen(
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                },
                onBookDetail = { id -> navController.navigate(Routes.bookDetail(id)) },
                onNotifications = { navController.navigate(Routes.NOTIFICATIONS) },
                onEditProfile = { navController.navigate(Routes.PROFILE_EDIT) },
            )
        }

        composable(Routes.BOOK_DETAIL) { backStack ->
            val bookId = backStack.arguments?.getString("bookId") ?: return@composable
            BookDetailScreen(
                bookId = bookId,
                onBack = { navController.popBackStack() },
            )
        }

        composable(Routes.NOTIFICATIONS) {
            NotificationsScreen(
                onBack = { navController.popBackStack() },
            )
        }

        composable(Routes.PROFILE_EDIT) {
            EditProfileScreen(
                onBack = { navController.popBackStack() },
            )
        }
    }
}
