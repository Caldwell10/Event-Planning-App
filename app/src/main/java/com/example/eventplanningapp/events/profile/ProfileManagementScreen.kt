package com.example.eventplanningapp.events.profile

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun ProfileManagementScreen(navController: NavController) {
    val firebaseAuth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val currentUser = firebaseAuth.currentUser
    val scope = rememberCoroutineScope()

    // State variables for user details
    var name by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var profilePhotoUri by remember { mutableStateOf<Uri?>(null) }
    var email by remember { mutableStateOf(currentUser?.email ?: "") }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf("") }

    // Editable states
    var isNameEditable by remember { mutableStateOf(false) }
    var isPhoneEditable by remember { mutableStateOf(false) }

    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri -> profilePhotoUri = uri }
    )

    // Fetch user details from Firestore
    LaunchedEffect(Unit) {
        if (currentUser != null) {
            try {
                val userDoc = firestore.collection("users").document(currentUser.uid).get().await()
                name = userDoc.getString("name") ?: ""
                phoneNumber = userDoc.getString("phoneNumber") ?: ""
                profilePhotoUri = userDoc.getString("profilePhotoUri")?.let { Uri.parse(it) }
            } catch (e: Exception) {
                errorMessage = "Error fetching profile: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Photo
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .padding(bottom = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                if (profilePhotoUri != null) {
                    Image(
                        painter = rememberAsyncImagePainter(profilePhotoUri),
                        contentDescription = "Profile Photo",
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(Color.Gray)
                    )
                } else {
                    Text(
                        text = "Add Photo",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .wrapContentSize(Alignment.Center)
                    )
                }
            }

            TextButton(onClick = { imagePickerLauncher.launch("image/*") }) {
                Text("Change Profile Photo")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Error Message
            if (errorMessage.isNotEmpty()) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            // Email Display (Non-editable)
            Text(
                text = "Email: $email",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Name Field with Edit Icon
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                readOnly = !isNameEditable,
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = { isNameEditable = !isNameEditable }) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit Name")
                    }
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Phone Number Field with Edit Icon
            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                readOnly = !isPhoneEditable,
                label = { Text("Phone Number") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = { isPhoneEditable = !isPhoneEditable }) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit Phone Number")
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Save Changes Button
            Button(
                onClick = {
                    scope.launch {
                        try {
                            isLoading = true
                            val updates = mutableMapOf(
                                "name" to name,
                                "phoneNumber" to phoneNumber
                            )

                            if (profilePhotoUri != null) {
                                updates["profilePhotoUri"] = profilePhotoUri.toString()
                            }

                            firestore.collection("users").document(currentUser!!.uid).update(updates as Map<String, Any>)
                            Toast.makeText(navController.context, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Toast.makeText(navController.context, "Error updating profile: ${e.message}", Toast.LENGTH_SHORT).show()
                        } finally {
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotEmpty() && phoneNumber.isNotEmpty()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Save Changes")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Logout Button
            Button(
                onClick = {
                    firebaseAuth.signOut()
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Logout")
            }
        }
    }
}
