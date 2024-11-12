package com.example.eventplanner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.eventplanner.events.LandingPage
import com.example.eventplanner.events.Login
import com.example.eventplanner.events.Register
import com.example.eventplanner.ui.theme.EventPlannerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EventPlannerTheme {
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = "landing" // Start with the Landing Page
                ) {
                    composable("landing") {
                        LandingPage(navController)
                    }
                    composable("login") {
                        Login(navController)
                    }
                    composable("register") {
                        Register(navController)
                    }
                }
            }
        }
    }
}
