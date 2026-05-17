package com.example.mykreedapreranascout.ui

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import java.io.File

fun createTempUri(context: Context): Uri {
    val imageFolder = java.io.File(context.cacheDir, "images")
    imageFolder.mkdirs() // Crucial: Creates the approved folder

    val file = java.io.File(imageFolder, "camera_photo_${System.currentTimeMillis()}.jpg")

    // Try using ".fileprovider" here instead of ".provider"
    return androidx.core.content.FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAthleteScreen(
    viewModel: AthleteViewModel,
    onSaveClick: () -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var rollNumber by remember { mutableStateOf("") } // <-- Roll Number State
    var age by remember { mutableStateOf("") }
    var studentClass by remember { mutableStateOf("") }
    var school by remember { mutableStateOf("") }
    var sport by remember { mutableStateOf("") }

    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }
    var showDialog by remember { mutableStateOf(false) }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            try {
                // 1. Open the temporary gallery image
                val inputStream = context.contentResolver.openInputStream(uri)
                // 2. Create a permanent file inside your app's secure internal storage
                val file = java.io.File(context.filesDir, "profile_${System.currentTimeMillis()}.jpg")
                val outputStream = java.io.FileOutputStream(file)
                // 3. Copy the pixels over
                inputStream?.copyTo(outputStream)
                inputStream?.close()
                outputStream.close()

                // 4. Save this permanent link to your variable!
                photoUri = android.net.Uri.fromFile(file)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) photoUri = tempCameraUri
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Register New Athlete") }) }
    ) { paddingValues ->
        Column(
            modifier = Modifier.padding(paddingValues).padding(16.dp).fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = { Text("Add Profile Photo") },
                    text = { Text("Choose where to get the picture from.") },
                    confirmButton = {
                        TextButton(onClick = {
                            showDialog = false
                            tempCameraUri = createTempUri(context)
                            cameraLauncher.launch(tempCameraUri!!)
                        }) { Text("Take Photo") }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            showDialog = false
                            galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        }) { Text("Gallery") }
                    }
                )
            }

            Box(
                modifier = Modifier.size(120.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { showDialog = true },
                contentAlignment = Alignment.Center
            ) {
                if (photoUri != null) {
                    AsyncImage(model = photoUri, contentDescription = "Profile Photo", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.Person, contentDescription = "Add Photo", modifier = Modifier.size(40.dp))
                        Text("Add Photo", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full Name") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))

            // THE NEW ROLL NUMBER FIELD!
            OutlinedTextField(value = rollNumber, onValueChange = { rollNumber = it }, label = { Text("Roll Number") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(value = age, onValueChange = { age = it }, label = { Text("Age") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = studentClass, onValueChange = { studentClass = it }, label = { Text("Class / Standard") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = school, onValueChange = { school = it }, label = { Text("School Name") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = sport, onValueChange = { sport = it }, label = { Text("Primary Sport") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    viewModel.addAthlete(name, rollNumber, age.toIntOrNull() ?: 0, studentClass, school, sport, photoUri?.toString())
                    onSaveClick()
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = name.isNotBlank() && age.isNotBlank() && studentClass.isNotBlank() && sport.isNotBlank()
            ) {
                Text("Save Profile")
            }
        }
    }
}