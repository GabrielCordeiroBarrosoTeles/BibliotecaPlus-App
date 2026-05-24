package com.bibliotecaplus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.rememberNavController
import com.bibliotecaplus.data.repository.AuthRepository
import com.bibliotecaplus.presentation.navigation.AppNavGraph
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var authRepository: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val isLoggedIn by authRepository.isLoggedIn.collectAsState(initial = false)
            val navController = rememberNavController()

            MaterialTheme {
                AppNavGraph(
                    navController = navController,
                    isLoggedIn = isLoggedIn,
                )
            }
        }
    }
}
