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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

@Composable
fun ProfileSetupScreen(navController: NavController) {
    val firebaseAuth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val currentUser = firebaseAuth.currentUser
    val scope = rememberCoroutineScope()

    // State variables for user details
    var name by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var profilePhotoUri by remember { mutableStateOf<Uri?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri -> profilePhotoUri = uri }
    )

    // Main UI
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Screen Title
        Text(
            text = "Set Up Your Profile",
            style = MaterialTheme.typography.headlineMedium.copy(fontSize = 24.sp),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Profile Picture Section
        Box(
            modifier = Modifier.size(120.dp),
            contentAlignment = Alignment.Center
        ) {
            if (profilePhotoUri != null) {
                Image(
                    painter = rememberAsyncImagePainter(profilePhotoUri),
                    contentDescription = "Profile Photo",
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                )
            } else {
                Text(
                    text = "Add Photo",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                        .wrapContentSize(Alignment.Center)
                )
            }
        }

        // Change Photo Button
        TextButton(onClick = { imagePickerLauncher.launch("image/*") }) {
            Text("Choose Profile Photo")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Name Input Field
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Phone Number Input Field
        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { phoneNumber = it },
            label = { Text("Phone Number") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Save Profile Button
        Button(
            onClick = {
                scope.launch {
                    try {
                        isLoading = true
                        val updates = mutableMapOf(
                            "name" to name,
                            "phoneNumber" to phoneNumber
                        )

                        // Save profile photo URI to Firestore
                        if (profilePhotoUri != null) {
                            updates["profilePhotoUri"] = profilePhotoUri.toString()
                        }

                        firestore.collection("users").document(currentUser!!.uid).update(updates as Map<String, Any>)
                        Toast.makeText(navController.context, "Profile setup complete!", Toast.LENGTH_SHORT).show()

                        // Navigate to Home Screen
                        navController.navigate("home") {
                            popUpTo("profileSetup") { inclusive = true }
                        }
                    } catch (e: Exception) {
                        Toast.makeText(navController.context, "Error saving profile: ${e.message}", Toast.LENGTH_SHORT).show()
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
                Text("Save and Continue")
            }
        }
    }
}
