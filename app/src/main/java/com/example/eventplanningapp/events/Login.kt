package com.example.eventplanningapp.events

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

@Composable
fun Login(navController: NavController) {
    val context = LocalContext.current
    val firebaseAuth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val scope = rememberCoroutineScope()

    // UI state variables
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Login", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                isLoading = true
                scope.launch {
                    loginUser(
                        email,
                        password,
                        firebaseAuth,
                        firestore,
                        onSuccess = { role ->
                            // Navigate based on role
                            if (role == "admin") {
                                navController.navigate("adminPage") {
                                    popUpTo("landing") { inclusive = true }
                                }
                            } else {
                                navController.navigate("home") {
                                    popUpTo("landing") { inclusive = true }
                                }
                            }
                        },
                        onError = { error ->
                            errorMessage = error
                        },
                        onLoading = { isLoading = it }
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
            } else {
                Text("Login")
            }
        }

        if (errorMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(errorMessage, color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = { navController.navigate("register") }
        ) {
            Text("Don't have an account? Register here.")
        }
    }
}

// **Login Function**: Authenticate and check user's role in Firestore
suspend fun loginUser(
    email: String,
    password: String,
    firebaseAuth: FirebaseAuth,
    firestore: FirebaseFirestore,
    onSuccess: (String?) -> Unit, // Role can be null for normal users
    onError: (String) -> Unit,
    onLoading: (Boolean) -> Unit
) {
    onLoading(true)

    try {
        val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
        val userId = authResult.user?.uid

        if (userId != null) {
            // Fetch the user document from Firestore
            val userDoc = firestore.collection("users").document(userId).get().await()
            val role = userDoc.getString("role") // Check for the "role" field in Firestore

            onSuccess(role) // Pass the role (admin or null) to determine navigation
        } else {
            onError("User ID not found.")
        }
    } catch (e: Exception) {
        onError(e.localizedMessage ?: "Login failed.")
    } finally {
        onLoading(false)
    }
}
