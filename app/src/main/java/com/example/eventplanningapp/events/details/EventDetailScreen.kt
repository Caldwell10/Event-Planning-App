package com.example.eventplanningapp.events

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.net.URLDecoder
import java.util.*
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

@Composable
fun EventDetailScreen(
    name: String,
    location: String,
    price: Float,
    imageUrl: String,
    description: String,
    date: String,
    navController: NavController
) {
    // Decode the URL-encoded strings to remove `+` signs
    val decodedDescription = URLDecoder.decode(description, "UTF-8")
    val decodedDate = URLDecoder.decode(date, "UTF-8")

    val context = LocalContext.current
    val firestore = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val scope = rememberCoroutineScope()
    var isBooking by remember { mutableStateOf(false) }

    Scaffold { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Event Image
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Event Image",
                    modifier = Modifier
                        .height(200.dp)
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )

                // Event Name
                Text(
                    text = name,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Event Location
                Text(
                    text = "Location: $location",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.padding(bottom = 8.dp),
                    textAlign = TextAlign.Center
                )

                // Event Price
                Text(
                    text = "Price: Ksh ${price}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )

                // Event Date
                Text(
                    text = "Date: $decodedDate",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.secondary
                    ),
                    modifier = Modifier.padding(vertical = 8.dp),
                    textAlign = TextAlign.Center
                )

                // Event Description
                Text(
                    text = "Description: $decodedDescription",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.padding(vertical = 8.dp),
                    textAlign = TextAlign.Start
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Book Ticket Button
                Button(
                    onClick = {
                        if (currentUser != null) {
                            scope.launch {
                                isBooking = true
                                val ticketId = UUID.randomUUID().toString()
                                val email = currentUser.email

                                if (email.isNullOrEmpty()) {
                                    Toast.makeText(
                                        context,
                                        "No email found for user!",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    isBooking = false
                                    return@launch
                                }

                                val ticketData = mapOf(
                                    "eventName" to name,
                                    "location" to location,
                                    "price" to price,
                                    "description" to decodedDescription,
                                    "date" to decodedDate,
                                    "userEmail" to email,
                                    "timestamp" to System.currentTimeMillis()
                                )

                                try {
                                    firestore.collection("tickets")
                                        .add(ticketData)
                                        .await()
                                    sendEmail(
                                        recipientEmail = email,
                                        subject = "Your Ticket for $name",
                                        body = """
                                        Hello,
                                        
                                        Thank you for booking your ticket for $name.
                                        
                                        Location: $location
                                        Price: Ksh ${price}
                                        Date: $decodedDate
                                        
                                        Please show this email at the event entry.
                                        
                                        Best regards,
                                        Evently Team
                                        """.trimIndent()
                                    )
                                    Toast.makeText(
                                        context,
                                        "Ticket booked successfully!",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    withContext(Dispatchers.Main) {
                                        navController.navigate("myEvents")
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(
                                        context,
                                        "Error booking ticket: ${e.message}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                } finally {
                                    isBooking = false
                                }
                            }
                        } else {
                            Toast.makeText(
                                context,
                                "Please log in to book a ticket",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    enabled = !isBooking,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    if (isBooking) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Book Ticket",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }
            }
        }
    }
}

suspend fun sendEmail(recipientEmail: String, subject: String, body: String) {
    withContext(Dispatchers.IO) {
        try {
            val host = "smtp.gmail.com"
            val port = "587"
            val senderEmail = "evently112@gmail.com"
            val senderPassword = "pkdr eluz sjel bsls"

            val properties = Properties().apply {
                put("mail.smtp.auth", "true")
                put("mail.smtp.starttls.enable", "true")
                put("mail.smtp.host", host)
                put("mail.smtp.port", port)
            }

            val session = Session.getInstance(properties, object : Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication {
                    return PasswordAuthentication(senderEmail, senderPassword)
                }
            })

            val message = MimeMessage(session).apply {
                setFrom(InternetAddress(senderEmail))
                setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail))
                this.subject = subject
                setText(body)
            }

            Transport.send(message)
        } catch (e: Exception) {
            println("Failed to send email: ${e.message}")
            e.printStackTrace()
        }
    }
}
