package com.example.eventplanningapp.events.widgets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.eventplanningapp.events.Event
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun EventItem(event: Event, navController: NavController) {
    Card(
        shape = RoundedCornerShape(16.dp), // Rounded corners for modern design
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable {
                // Encode the imageUrl for safe navigation
                val encodedImageUrl = URLEncoder.encode(event.imageUrl, StandardCharsets.UTF_8.toString())
                navController.navigate("eventDetail/${event.name}/${event.location}/${event.price}/${encodedImageUrl}")
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp), // Depth effect
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Event Image
            AsyncImage(
                model = event.imageUrl,
                contentDescription = event.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp) // Consistent height for images
            )

            // Event Details
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Event Name
                Text(
                    text = event.name,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))

                // Event Location
                Text(
                    text = "Location: ${event.location}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Event Price
                Text(
                    text = "Price: $${event.price}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
