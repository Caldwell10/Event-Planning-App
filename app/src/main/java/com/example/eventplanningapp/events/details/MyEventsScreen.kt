package com.example.eventplanningapp.events

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.eventplanningapp.events.widgets.EventItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.net.URLDecoder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyEventsScreen(navController: NavController) {
    val firestore = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    var isLoading by remember { mutableStateOf(true) }
    var myEvents by remember { mutableStateOf<List<Event>>(emptyList()) }
    val scope = rememberCoroutineScope()

    // Ensure coroutines are canceled when composable is removed
    DisposableEffect(Unit) {
        onDispose { scope.cancel() }
    }

    // Fetch data when the screen is loaded
    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            try {
                val bookedEvents = fetchBookedEvents(firestore, currentUser.email ?: "")
                myEvents = bookedEvents
            } catch (e: Exception) {
                // Handle error here
            } finally {
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Events") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate("home") }) {
                        Icon(Icons.Filled.Home, contentDescription = "Back to Home")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (myEvents.isEmpty()) {
                Text(
                    text = "You haven't booked any events yet.",
                    style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(myEvents) { event ->
                        EventItem(event = event, navController = navController)
                    }
                }
            }
        }
    }
}

suspend fun fetchBookedEvents(firestore: FirebaseFirestore, userEmail: String): List<Event> {
    return firestore.collection("tickets")
        .whereEqualTo("userEmail", userEmail)
        .get()
        .await()
        .documents
        .mapNotNull { doc ->
            val name = doc.getString("eventName")
            val location = doc.getString("location")
            val price = doc.getDouble("price")
            val imageUrl = doc.getString("imageUrl") ?: ""
            val encodedDescription = doc.getString("description") ?: "No description available."
            val encodedDate = doc.getString("date") ?: "No date available."

            // Decode description and date
            val description = URLDecoder.decode(encodedDescription, "UTF-8")
            val date = URLDecoder.decode(encodedDate, "UTF-8")

            if (name != null && location != null && price != null) {
                Event(
                    name = name,
                    location = location,
                    price = price,
                    imageUrl = imageUrl,
                    description = description,
                    date = date
                )
            } else {
                null
            }
        }
}
