package com.example.eventplanningapp.events

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.eventplanningapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

@Composable
fun Register(navController: NavController) {
    val context = LocalContext.current
    val firebaseAuth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // App Logo
            Image(
                painter = painterResource(id = R.drawable.evently_high_resolution_logo),
                contentDescription = "App Logo",
                modifier = Modifier
                    .height(100.dp)
                    .padding(bottom = 24.dp)
            )

            // Title
            Text(
                text = "Create an Account",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Email Input
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, shape = RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.background, shape = RoundedCornerShape(12.dp)),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color.Gray
                )
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Password Input
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, shape = RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.background, shape = RoundedCornerShape(12.dp)),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color.Gray
                )
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Confirm Password Input
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirm Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, shape = RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.background, shape = RoundedCornerShape(12.dp)),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color.Gray
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Register Button
            Button(
                onClick = {
                    if (password != confirmPassword) {
                        errorMessage = "Passwords do not match!"
                    } else {
                        registerUser(
                            email,
                            password,
                            firebaseAuth,
                            firestore,
                            onSuccess = {
                                Toast.makeText(context, "Registration successful!", Toast.LENGTH_LONG).show()
                                navController.navigate("home") { popUpTo("register") { inclusive = true } }
                            },
                            onError = { errorMessage = it },
                            onLoading = { isLoading = it }
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Register")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Error Message
            if (errorMessage.isNotEmpty()) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Login Link
            Text(
                text = "Already have an account? Login here.",
                style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.primary),
                modifier = Modifier.clickable { navController.navigate("login") }
            )
        }
    }
}

// Function to register a user and send a welcome notification
fun registerUser(
    email: String,
    password: String,
    firebaseAuth: FirebaseAuth,
    firestore: FirebaseFirestore,
    onSuccess: () -> Unit,
    onError: (String) -> Unit,
    onLoading: (Boolean) -> Unit
) {
    onLoading(true)

    firebaseAuth.createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = firebaseAuth.currentUser

                // Save user to Firestore
                user?.let {
                    val userData = hashMapOf(
                        "uid" to it.uid,
                        "email" to email,
                        "createdAt" to System.currentTimeMillis()
                    )

                    firestore.collection("users")
                        .document(it.uid)
                        .set(userData)
                        .addOnSuccessListener {
                            // Send welcome notification
                            CoroutineScope(Dispatchers.IO).launch {
                                sendWelcomeNotification(email)
                            }
                            onSuccess()
                        }
                        .addOnFailureListener { e -> onError(e.localizedMessage ?: "Error saving user to Firestore") }
                }
            } else {
                onError(task.exception?.localizedMessage ?: "Registration failed.")
            }
            onLoading(false)
        }
        .addOnFailureListener { e ->
            onError(e.localizedMessage ?: "An error occurred.")
            onLoading(false)
        }
}

// Function to send a welcome push notification
suspend fun sendWelcomeNotification(email: String) {
    val client = OkHttpClient()
    val jsonObject = JSONObject().apply {
        put("app_id", "05e7aef1-a5a7-493c-bc65-f092d7253bff") // Replace with your OneSignal App ID
        put("include_external_user_ids", arrayOf(email)) // Target user by email
        put("headings", JSONObject().put("en", "Welcome to Evently!"))
        put("contents", JSONObject().put("en", "Thank you for joining us. Explore exciting events!"))
    }

    val body = jsonObject.toString().toRequestBody("application/json".toMediaTypeOrNull())
    val request = Request.Builder()
        .url("https://onesignal.com/api/v1/notifications")
        .post(body)
        .addHeader("Authorization", "zex7lc52vu5vu25yv72qkuq6s")
        .addHeader("Content-Type", "application/json")
        .build()

    withContext(Dispatchers.IO) {
        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    println("Failed to send notification: ${response.body?.string()}")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
