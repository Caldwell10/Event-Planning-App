package com.example.eventplanningapp.events

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.eventplanningapp.events.widgets.EventItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class Event(
    val name: String,
    val location: String,
    val price: Double,
    val imageUrl: String,
    val description: String,
    val date: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    val firestore = FirebaseFirestore.getInstance()
    val firebaseAuth = FirebaseAuth.getInstance()
    val scope = rememberCoroutineScope()

    var isLoading by remember { mutableStateOf(true) }
    var events by remember { mutableStateOf<List<Event>>(emptyList()) }
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
    var filteredEvents by remember { mutableStateOf<List<Event>>(emptyList()) }
    var userName by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        scope.launch {
            val currentUser = firebaseAuth.currentUser
            if (currentUser != null) {
                try {
                    val userDoc = firestore.collection("users").document(currentUser.uid).get().await()
                    userName = userDoc.getString("name") ?: "User"
                } catch (e: Exception) {
                    Toast.makeText(context, "Error fetching user details: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            fetchEventsRealTime(
                firestore = firestore,
                onEventsChanged = { newEvents ->
                    events = newEvents
                    filteredEvents = newEvents
                    isLoading = false
                },
                onError = { error ->
                    Toast.makeText(context, "Error: ${error.message}", Toast.LENGTH_LONG).show()
                    isLoading = false
                }
            )
        }
    }

    LaunchedEffect(searchQuery.text) {
        filteredEvents = if (searchQuery.text.isEmpty()) {
            events
        } else {
            events.filter { event ->
                event.name.contains(searchQuery.text, ignoreCase = true) ||
                        event.location.contains(searchQuery.text, ignoreCase = true)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(userName = userName, onLogout = {
                firebaseAuth.signOut()
                navController.navigate("login") {
                    popUpTo(0) { inclusive = true }
                }
            })
        },
        bottomBar = {
            BottomNavigationBar(
                selectedTab = "home",
                onTabSelected = { selectedTab ->
                    when (selectedTab) {
                        "home" -> navController.navigate("home") {
                            popUpTo("home") { inclusive = true }
                        }
                        "myEvents" -> navController.navigate("myEvents") {
                            popUpTo("home") { inclusive = false }
                        }
                        "profile" -> navController.navigate("profile") {
                            popUpTo("home") { inclusive = false }
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            HomeContent(
                events = filteredEvents,
                isLoading = isLoading,
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                navController = navController
            )
        }
    }
}

@Composable
fun TopAppBar(userName: String, onLogout: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Hello, $userName!",
            color = MaterialTheme.colorScheme.onPrimary,
            style = MaterialTheme.typography.headlineSmall
        )

        IconButton(onClick = onLogout) {
            Icon(
                imageVector = Icons.Filled.PowerSettingsNew,
                contentDescription = "Logout",
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
fun BottomNavigationBar(selectedTab: String, onTabSelected: (String) -> Unit) {
    NavigationBar {
        NavigationBarItem(
            selected = selectedTab == "home",
            onClick = { onTabSelected("home") },
            icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
            label = { Text("Home") }
        )
        NavigationBarItem(
            selected = selectedTab == "myEvents",
            onClick = { onTabSelected("myEvents") },
            icon = { Icon(Icons.Filled.Event, contentDescription = "My Events") },
            label = { Text("My Events") }
        )
        NavigationBarItem(
            selected = selectedTab == "profile",
            onClick = { onTabSelected("profile") },
            icon = { Icon(Icons.Filled.Person, contentDescription = "Profile") },
            label = { Text("Profile") }
        )
    }
}

@Composable
fun HomeContent(
    events: List<Event>,
    isLoading: Boolean,
    searchQuery: TextFieldValue,
    onSearchQueryChange: (TextFieldValue) -> Unit,
    navController: NavController
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            label = { Text("Search events...") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else if (events.isEmpty()) {
            Text("No events available", color = Color.Gray)
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(events) { event ->
                    EventItem(event = event, navController = navController)
                }
            }
        }
    }
}

fun fetchEventsRealTime(
    firestore: FirebaseFirestore,
    onEventsChanged: (List<Event>) -> Unit,
    onError: (Exception) -> Unit
) {
    firestore.collection("events")
        .addSnapshotListener { snapshot, error ->
            if (error != null) {
                onError(error)
                return@addSnapshotListener
            }

            val events = snapshot?.documents?.mapNotNull { doc ->
                val name = doc.getString("name")
                val location = doc.getString("location")
                val price = doc.getDouble("price")
                val description = doc.getString("eventDescription")
                val date = doc.getString("eventDate")
                val imageUrl = doc.getString("image") ?: ""

                if (name != null && location != null && price != null && description != null && date != null) {
                    Event(name, location, price, imageUrl, description, date)
                } else null
            } ?: emptyList()

            onEventsChanged(events)
        }
}
