package com.example.eventplanningapp.events

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
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

@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    val firestore = FirebaseFirestore.getInstance()
    val scope = rememberCoroutineScope()

    var isLoading by remember { mutableStateOf(true) }
    var events by remember { mutableStateOf<List<Event>>(emptyList()) }

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val fetchedEvents = fetchEvents(firestore)
                withContext(Dispatchers.Main) {
                    events = fetchedEvents
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Upcoming Events",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                items(events) { event ->
                    EventItem(event)
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
