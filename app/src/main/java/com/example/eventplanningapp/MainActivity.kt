package com.example.eventplanningapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.eventplanner.events.Register
import com.example.eventplanningapp.admin.AdminPage
import com.example.eventplanningapp.events.HomeScreen
import com.example.eventplanningapp.events.LandingPage
import com.example.eventplanningapp.events.Login
import com.example.eventplanningapp.ui.theme.EventPlannerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EventPlannerTheme {
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = "landing"
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
                    composable("home"){
                        HomeScreen(navController)
                    }
                    composable("admin") {
                        AdminPage(navController) }
                }
            }
        }
    }
}
