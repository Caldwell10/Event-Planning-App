package com.example.eventplanningapp.admin

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text

@Composable
fun AdminPage(navController: NavController) {
    var selectedTab by remember { mutableStateOf("createEvent") }

    Scaffold(
        bottomBar = {
            AdminBottomNavigationBar(selectedTab = selectedTab) { tab ->
                selectedTab = tab
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedTab) {
                "createEvent" -> CreateEventScreen(navController)
                "viewEvents" -> ViewEventsScreen(navController)
            }
        }
    }
}

@Composable
fun AdminBottomNavigationBar(selectedTab: String, onTabSelected: (String) -> Unit) {
    NavigationBar {
        NavigationBarItem(
            selected = selectedTab == "createEvent",
            onClick = { onTabSelected("createEvent") },
            icon = { Icon(Icons.Filled.Add, contentDescription = "Create Event") },
            label = { Text("Create Event") }
        )
        NavigationBarItem(
            selected = selectedTab == "viewEvents",
            onClick = { onTabSelected("viewEvents") },
            icon = { Icon(Icons.Filled.List, contentDescription = "View Events") },
            label = { Text("View Events") }
        )
    }
}
