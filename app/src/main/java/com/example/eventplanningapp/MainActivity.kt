package com.example.eventplanningapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.eventplanningapp.admin.AdminPage
import com.example.eventplanningapp.events.EventDetailScreen
import com.example.eventplanningapp.events.HomeScreen
import com.example.eventplanningapp.events.LandingPage
import com.example.eventplanningapp.events.Login
import com.example.eventplanningapp.events.MyEventsScreen
import com.example.eventplanningapp.events.Register
import com.example.eventplanningapp.events.profile.ProfileManagementScreen
import com.example.eventplanningapp.events.profile.ProfileSetupScreen
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
                    // Landing Page
                    composable("landing") {
                        LandingPage(navController)
                    }

                    // Login Page
                    composable("login") {
                        Login(navController)
                    }

                    // Register Page
                    composable("register") {
                        Register(navController)
                    }

                    // Home Page
                    composable("home") {
                        HomeScreen(navController)
                    }

                    // Admin Page
                    composable("admin") {
                        AdminPage(navController)
                    }

                    // Profile Management Page
                    composable("profile") {
                        ProfileManagementScreen(navController = navController)
                    }

                    // Profile Setup Page
                    composable("profileSetup") {
                        ProfileSetupScreen(navController)
                    }

                    // My Events Page
                    composable("myEvents") {
                        MyEventsScreen(navController)
                    }

                    // Event Detail Page with arguments
                    composable(
                        route = "eventDetail/{name}/{location}/{price}/{imageUrl}/{description}/{date}",
                        arguments = listOf(
                            navArgument("name") { type = NavType.StringType },
                            navArgument("location") { type = NavType.StringType },
                            navArgument("price") { type = NavType.FloatType },
                            navArgument("imageUrl") { type = NavType.StringType },
                            navArgument("description") { type = NavType.StringType },
                            navArgument("date") { type = NavType.StringType }
                        )
                    ) { backStackEntry ->
                        // Extract arguments
                        val name = backStackEntry.arguments?.getString("name") ?: ""
                        val location = backStackEntry.arguments?.getString("location") ?: ""
                        val price = backStackEntry.arguments?.getFloat("price") ?: 0f
                        val imageUrl = backStackEntry.arguments?.getString("imageUrl") ?: ""
                        val description = backStackEntry.arguments?.getString("description") ?: ""
                        val date = backStackEntry.arguments?.getString("date") ?: ""

                        // Pass to EventDetailScreen
                        EventDetailScreen(
                            name = name,
                            location = location,
                            price = price,
                            imageUrl = imageUrl,
                            description = description,
                            date = date,
                            navController = navController
                        )
                    }
                }
            }
        }
    }
}
