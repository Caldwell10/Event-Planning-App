package com.example.eventplanningapp.admin

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

@Composable
fun AdminPage(navController: NavController) {
    val firestore = FirebaseFirestore.getInstance()
    val storage = FirebaseStorage.getInstance()

    var eventName by remember { mutableStateOf("") }
    var eventLocation by remember { mutableStateOf("") }
    var eventPrice by remember { mutableStateOf("") }
    var eventImageUri by remember { mutableStateOf<String?>(null) }
    var selectedImageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var isUploading by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Create Event",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = eventName,
            onValueChange = { eventName = it },
            label = { Text("Event Name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = eventLocation,
            onValueChange = { eventLocation = it },
            label = { Text("Event Location") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = eventPrice,
            onValueChange = { eventPrice = it },
            label = { Text("Event Price") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (selectedImageBitmap != null) {
            Image(
                bitmap = selectedImageBitmap!!,
                contentDescription = "Event Image",
                modifier = Modifier
                    .size(200.dp)
                    .padding(16.dp)
            )
        }

        val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                eventImageUri = it.toString()
                selectedImageBitmap = loadBitmapFromUri(context, it)
            }
        }

        Button(onClick = { imagePickerLauncher.launch("image/*") }) {
            Text("Select Image")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (eventName.isBlank() || eventLocation.isBlank() || eventPrice.isBlank() || eventImageUri == null) {
                    Toast.makeText(context, "All fields are required", Toast.LENGTH_LONG).show()
                } else {
                    isUploading = true
                    scope.launch {
                        try {
                            val imageUrl = uploadImageToStorage(storage, eventImageUri!!)
                            firestore.collection("events").add(
                                mapOf(
                                    "name" to eventName,
                                    "location" to eventLocation,
                                    "price" to eventPrice.toDouble(),
                                    "image" to imageUrl
                                )
                            )
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "Event Created Successfully!", Toast.LENGTH_LONG).show()
                                navController.navigate("home")
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                        } finally {
                            isUploading = false
                        }
                    }
                }
            },
            enabled = !isUploading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isUploading) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text("Create Event")
            }
        }
    }
}

suspend fun uploadImageToStorage(storage: FirebaseStorage, uri: String): String {
    val storageRef = storage.reference.child("event_images/${System.currentTimeMillis()}.jpg")
    storageRef.putFile(Uri.parse(uri)).await()
    return storageRef.downloadUrl.await().toString()
}

fun loadBitmapFromUri(context: Context, uri: Uri): ImageBitmap? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        bitmap?.asImageBitmap()
    } catch (e: Exception) {
        null
    }
}
