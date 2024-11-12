package com.example.eventplanner.events

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun Register(navController: NavController) {
    val context = LocalContext.current

    // UI state variables
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // Firebase instances
    val firebaseAuth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()

    // UI Layout
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Register", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(16.dp))

        // Email input field
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Password input field
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Confirm Password input field
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Register Button
        Button(
            onClick = {
                if (password != confirmPassword) {
                    errorMessage = "Passwords do not match!"
                } else if (email.isBlank() || password.isBlank()) {
                    errorMessage = "Email and Password cannot be empty!"
                } else {
                    registerUser(
                        email,
                        password,
                        firebaseAuth,
                        firestore,
                        onSuccess = {
                            Toast.makeText(
                                context,
                                "Registration Successful! Check your email for a welcome message.",
                                Toast.LENGTH_LONG
                            ).show()
                            navController.navigate("login") {
                                popUpTo("register") { inclusive = true }
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
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text("Register")
            }
        }

        // Display error message
        if (errorMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(errorMessage, color = MaterialTheme.colorScheme.error)
        }
    }
}

// Registration Function
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
                            // Send welcome email
                            user.sendEmailVerification()
                                .addOnCompleteListener { emailTask ->
                                    if (emailTask.isSuccessful) {
                                        onSuccess()
                                    } else {
                                        onError(emailTask.exception?.localizedMessage ?: "Email error")
                                    }
                                }
                        }
                        .addOnFailureListener { e ->
                            onError(e.localizedMessage ?: "Error saving user to Firestore")
                        }
                }
            } else {
                onError(task.exception?.localizedMessage ?: "Registration failed")
            }
            onLoading(false)
        }
        .addOnFailureListener { e ->
            onError(e.localizedMessage ?: "An error occurred")
            onLoading(false)
        }
}

