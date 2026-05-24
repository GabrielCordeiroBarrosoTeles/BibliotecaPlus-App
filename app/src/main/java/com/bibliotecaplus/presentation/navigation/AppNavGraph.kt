package com.bibliotecaplus.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.bibliotecaplus.presentation.auth.LoginScreen
import com.bibliotecaplus.presentation.home.HomeScreen
import com.bibliotecaplus.presentation.books.BookListScreen
import com.bibliotecaplus.presentation.books.BookDetailScreen
import com.bibliotecaplus.presentation.loans.LoansScreen
import com.bibliotecaplus.presentation.profile.ProfileScreen

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Home : Screen("home")
    object BookList : Screen("books")
    object BookDetail : Screen("books/{bookId}") {
        fun createRoute(bookId: String) = "books/$bookId"
    }
    object Loans : Screen("loans")
    object Profile : Screen("profile")
}

@Composable
fun AppNavGraph(
    navController: NavHostController,
    isLoggedIn: Boolean,
) {
    NavHost(
        navController = navController,
        startDestination = if (isLoggedIn) Screen.Home.route else Screen.Login.route,
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToBooks = { navController.navigate(Screen.BookList.route) },
                onNavigateToLoans = { navController.navigate(Screen.Loans.route) },
                onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
            )
        }

        composable(Screen.BookList.route) {
            BookListScreen(
                onBookClick = { id -> navController.navigate(Screen.BookDetail.createRoute(id)) },
                onBack = { navController.popBackStack() },
            )
        }

        composable(
            route = Screen.BookDetail.route,
            arguments = listOf(navArgument("bookId") { type = NavType.StringType }),
        ) { backStackEntry ->
            BookDetailScreen(
                bookId = backStackEntry.arguments?.getString("bookId") ?: "",
                onBack = { navController.popBackStack() },
            )
        }

        composable(Screen.Loans.route) {
            LoansScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() },
            )
        }
    }
}
