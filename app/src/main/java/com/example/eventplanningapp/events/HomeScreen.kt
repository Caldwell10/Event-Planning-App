package com.example.eventplanningapp.events

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.eventplanningapp.R
import com.example.eventplanningapp.events.widgets.EventItem
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

data class Event(
    val name: String,
    val location: String,
    val price: Double,
    val imageUrl: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    val firestore = FirebaseFirestore.getInstance()
    val scope = rememberCoroutineScope()

    var isLoading by remember { mutableStateOf(true) }
    var events by remember { mutableStateOf<List<Event>>(emptyList()) }
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
    var filteredEvents by remember { mutableStateOf<List<Event>>(emptyList()) }

    // Fetch events from Firestore
    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val fetchedEvents = fetchEvents(firestore)
                withContext(Dispatchers.Main) {
                    events = fetchedEvents
                    filteredEvents = fetchedEvents
                    isLoading = false
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    isLoading = false
                }
            }
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

    // Main Content
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo
            Image(
                painter = painterResource(id = R.drawable.evently_high_resolution_logo),
                contentDescription = "App Logo",
                modifier = Modifier
                    .height(80.dp)
                    .padding(vertical = 16.dp)
            )

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search events...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(6.dp, MaterialTheme.shapes.medium), // Elevation for better design
                colors = OutlinedTextFieldDefaults.run {
                    colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = Color.Gray,

                            )
                },
                shape = MaterialTheme.shapes.medium
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Title
            Text(
                text = "Upcoming Events",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                textAlign = TextAlign.Start,
                color = MaterialTheme.colorScheme.onSurface
            )


            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else if (filteredEvents.isEmpty()) {
                Text(
                    text = "No events match your search.",
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray),
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Center
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(filteredEvents, key = { it.name }) { event ->
                        EventItem(event = event, navController = navController)
                    }
                }
            }
        }
    }
}

suspend fun fetchEvents(firestore: FirebaseFirestore): List<Event> {
    return firestore.collection("events")
        .get()
        .await()
        .documents
        .mapNotNull { doc ->
            val name = doc.getString("name") ?: return@mapNotNull null
            val location = doc.getString("location") ?: return@mapNotNull null
            val price = doc.getDouble("price") ?: return@mapNotNull null
            val imageUrl = doc.getString("image") ?: ""
            Event(name, location, price, imageUrl)
        }
}
