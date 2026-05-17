package com.example.mykreedapreranascout.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.view.PixelCopy
import android.view.View
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerformanceCardScreen(
    athleteId: Int,
    viewModel: AthleteViewModel,
    onBackClick: () -> Unit // See? No captureAndShareCard parameter here!
) {
    val athlete = viewModel.getAthleteById(athleteId)
    val trials by viewModel.getTrialsForAthlete(athleteId).collectAsState(initial = emptyList())

    val context = LocalContext.current
    val view = LocalView.current
    var cardBounds by remember { mutableStateOf<Rect?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }

    val best100m = trials.filter { it.eventType == "100m Sprint" }.minByOrNull { it.resultValue }?.resultValue
    val bestLongJump = trials.filter { it.eventType == "Long Jump" }.maxByOrNull { it.resultValue }?.resultValue

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Player Profile Card") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                },
                actions = {
                    IconButton(onClick = { showEditDialog = true }) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit Profile")
                    }
                    IconButton(onClick = {
                        if (athlete != null && cardBounds != null) {
                            captureAndShareCard(context, view, athlete.name, cardBounds!!)
                        }
                    }) {
                        Icon(Icons.Filled.Share, contentDescription = "Share Image")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (athlete == null) return@Scaffold

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .onGloballyPositioned { coordinates ->
                        val bounds = coordinates.boundsInWindow()
                        cardBounds = Rect(
                            bounds.left.toInt(),
                            bounds.top.toInt(),
                            bounds.right.toInt(),
                            bounds.bottom.toInt()
                        )
                    },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (athlete.photoUri != null) {
                        AsyncImage(
                            model = athlete.photoUri,
                            contentDescription = "Photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(120.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary)
                        )
                    } else {
                        Box(
                            modifier = Modifier.size(120.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(athlete.name.take(1).uppercase(), style = MaterialTheme.typography.displayLarge, color = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(athlete.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text("${athlete.studentClass} | ${athlete.school}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)

                    Spacer(modifier = Modifier.height(24.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(16.dp))

                    Text("TOP RECORDS", style = MaterialTheme.typography.labelLarge, color = Color.Gray)
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        StatBox("100m Sprint", best100m?.let { "${it}s" } ?: "--")
                        StatBox("Long Jump", bestLongJump?.let { "${it}m" } ?: "--")
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFFFD700))) {
                        Text(
                            text = "🏆 ${athlete.earnedBadge ?: "Rising Star"} 🏆",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 3. The Edit Profile Popup Dialog
        if (showEditDialog) {
            var editName by remember { mutableStateOf(athlete.name) }
            var editRoll by remember { mutableStateOf(athlete.rollNumber) }
            var editAge by remember { mutableStateOf(athlete.age.toString()) }
            var editClass by remember { mutableStateOf(athlete.studentClass) }
            var editSchool by remember { mutableStateOf(athlete.school) }
            var editSport by remember { mutableStateOf(athlete.sport) }

            var editPhotoUri by remember { mutableStateOf(athlete.photoUri) }
            val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
                if (uri != null) {
                    try {
                        val inputStream = context.contentResolver.openInputStream(uri)
                        val file = java.io.File(context.filesDir, "profile_${System.currentTimeMillis()}.jpg")
                        val outputStream = java.io.FileOutputStream(file)
                        inputStream?.copyTo(outputStream)
                        inputStream?.close()
                        outputStream.close()

                        // Save the permanent link (converted to a String for the database)
                        editPhotoUri = android.net.Uri.fromFile(file).toString()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            AlertDialog(
                onDismissRequest = { showEditDialog = false },
                title = { Text("Edit Profile") },
                text = {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {

                        Box(
                            modifier = Modifier.size(100.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
                                .align(Alignment.CenterHorizontally),
                            contentAlignment = Alignment.Center
                        ) {
                            if (editPhotoUri != null) {
                                AsyncImage(model = editPhotoUri, contentDescription = "Edit Photo", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                            } else {
                                Icon(Icons.Filled.Person, contentDescription = "Add Photo", modifier = Modifier.size(40.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(value = editName, onValueChange = { editName = it }, label = { Text("Full Name") })
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(value = editRoll, onValueChange = { editRoll = it }, label = { Text("Roll Number") })
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(value = editAge, onValueChange = { editAge = it }, label = { Text("Age") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(value = editClass, onValueChange = { editClass = it }, label = { Text("Class/Standard") })
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(value = editSchool, onValueChange = { editSchool = it }, label = { Text("School") })
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(value = editSport, onValueChange = { editSport = it }, label = { Text("Primary Sport") })
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        val updatedAthlete = athlete.copy(
                            name = editName,
                            rollNumber = editRoll,
                            age = editAge.toIntOrNull() ?: athlete.age,
                            studentClass = editClass,
                            school = editSchool,
                            sport = editSport,
                            photoUri = editPhotoUri
                        )
                        viewModel.updateAthlete(updatedAthlete)
                        showEditDialog = false
                    }) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEditDialog = false }) { Text("Cancel") }
                }
            )
        }
    }
}

@Composable
fun StatBox(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.bodyMedium)
    }
}

fun captureAndShareCard(context: Context, view: View, athleteName: String, bounds: Rect) {
    val activity = context as? Activity ?: return
    val window = activity.window

    val bitmap = Bitmap.createBitmap(bounds.width(), bounds.height(), Bitmap.Config.ARGB_8888)

    PixelCopy.request(window, bounds, bitmap, { copyResult ->
        if (copyResult == PixelCopy.SUCCESS) {
            val cachePath = File(context.cacheDir, "images")
            cachePath.mkdirs()
            val file = File(cachePath, "athlete_card.png")
            val stream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.close()

            val authority = "${context.packageName}.fileprovider"
            val contentUri = FileProvider.getUriForFile(context, authority, file)

            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                setDataAndType(contentUri, context.contentResolver.getType(contentUri))
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_TEXT, "Check out $athleteName's athletic stats on Kreeda-Prerana Scout!")
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share Performance Card"))
        }
    }, Handler(Looper.getMainLooper()))
}