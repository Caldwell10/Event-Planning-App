package com.example.eventplanningapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.eventplanningapp.events.Register
import com.example.eventplanningapp.admin.AdminPage
import com.example.eventplanningapp.events.EventDetailScreen
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
                    composable("home") {
                        HomeScreen(navController)
                    }
                    composable("admin") {
                        AdminPage(navController)
                    }
                    composable(
                        "eventDetail/{name}/{location}/{price}/{imageUrl}",
                        arguments = listOf(
                            navArgument("name") { type = NavType.StringType },
                            navArgument("location") { type = NavType.StringType },
                            navArgument("price") { type = NavType.FloatType },
                            navArgument("imageUrl") { type = NavType.StringType }
                        )
                    ) { backStackEntry ->
                        val name = backStackEntry.arguments?.getString("name")
                        val location = backStackEntry.arguments?.getString("location")
                        val price = backStackEntry.arguments?.getFloat("price")
                        val imageUrl = backStackEntry.arguments?.getString("imageUrl")

                        if (name != null && location != null && price != null && imageUrl != null) {
                            EventDetailScreen(name, location, price, imageUrl)
                        }
                    }
                }
            }
        }
    }
}
