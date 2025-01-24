// MainActivity.kt
package com.example.kotlinwebcamandroidapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.kotlinwebcamandroidapp.data.WebCams
import com.example.kotlinwebcamandroidapp.model.WebCam
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WebCamApp()
        }
        checkLocationPermission()
    }
    private fun checkLocationPermission() {
        val fineLocationPermission = Manifest.permission.ACCESS_FINE_LOCATION
        val coarseLocationPermission = Manifest.permission.ACCESS_COARSE_LOCATION

        when {
            // Check if permissions are already granted
            ContextCompat.checkSelfPermission(this, fineLocationPermission) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, coarseLocationPermission) == PackageManager.PERMISSION_GRANTED -> {
                // Permissions already granted
                getUserLocation()
            }

            // Request permissions
            else -> {
                requestPermissionsLauncher.launch(arrayOf(fineLocationPermission, coarseLocationPermission))
            }
        }
    }

    private val requestPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
            // Permissions granted
            getUserLocation()
        } else {
            // Permissions denied
            println("Location permission denied")
        }
    }

    private fun getUserLocation() {
        // Logic to retrieve the user's location
        println("Fetching user location...")
    }
}

fun buildData(lat: Double, lon: Double): WebCams = runBlocking {
    val webCams = WebCams(lat, lon)
    webCams.init() // Fetch and initialize data from the API
    webCams
}

@Composable
fun WebCamApp() {
    var currentScreen by remember { mutableStateOf<WebCamState>(WebCamState.LocationInput) }

    when (val state = currentScreen) {
        is WebCamState.LocationInput -> {
            LocationInputScreen(
                onLocationSubmit = { lat, lon ->
                    CoroutineScope(Dispatchers.IO).launch {
                        val webCams = buildData(lat, lon)
                        val webcamsList = webCams.getWebcams() // Get list of webcams
                        withContext(Dispatchers.Main) {
                            currentScreen = WebCamState.List(webcamsList) // Transition to List state
                        }
                    }
                }
            )
        }
        is WebCamState.List -> {
            WebCamListScreen(
                webcams = state.webcams,
                onWebCamSelected = { id ->
                    // Use an explicitly named parameter for clarity
                    val selectedWebCam = state.webcams.firstOrNull { webcam: WebCam-> webcam.webcamId == id }
                    selectedWebCam?.let {
                        currentScreen = WebCamState.Detail(selectedWebCam, state.webcams)
                    }
                }
            )
        }
        is WebCamState.Detail -> {
            WebCamDetailScreen(
                webcam = state.webcam,
                onBack = {
                    currentScreen = WebCamState.List(state.webcamList)
                }
            )
        }
    }
}

@Composable
fun LocationInputScreen(onLocationSubmit: (Double, Double) -> Unit) {
    var lat by remember { mutableStateOf("") }
    var lon by remember { mutableStateOf("") }
    var isLatError by remember { mutableStateOf(false) }
    var isLonError by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        OutlinedTextField(
            value = lat,
            onValueChange = {
                lat = it
                isLatError = false
            },
            label = { Text("Latitude") },
            isError = isLatError,
            modifier = Modifier.fillMaxWidth()
        )
        if (isLatError) {
            Text("Invalid latitude. Must be between -90 and 90.", color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = lon,
            onValueChange = {
                lon = it
                isLonError = false
            },
            label = { Text("Longitude") },
            isError = isLonError,
            modifier = Modifier.fillMaxWidth()
        )
        if (isLonError) {
            Text("Invalid longitude. Must be between -180 and 180.", color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val latValue = lat.toDoubleOrNull()
                val lonValue = lon.toDoubleOrNull()

                if (latValue == null || latValue !in -90.0..90.0) {
                    isLatError = true
                }

                if (lonValue == null || lonValue !in -180.0..180.0) {
                    isLonError = true
                }

                if (!isLatError && !isLonError && latValue != null && lonValue != null) {
                    onLocationSubmit(latValue, lonValue)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Fetch WebCams")
        }
    }
}

@Composable
fun WebCamListScreen(webcams: List<WebCam>, onWebCamSelected: (Long) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Available WebCams", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        if (webcams.isEmpty()) {
            Text("No webcams found for the given location.", style = MaterialTheme.typography.bodyLarge)
        } else {
            webcams.forEach { webcam ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clickable { onWebCamSelected(webcam.webcamId) },
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(webcam.title)
                    Text("ID: ${webcam.webcamId}")
                }
            }
        }
    }
}

@Composable
fun WebCamDetailScreen(webcam: WebCam, onBack: () -> Unit) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("WebCam Details", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Title: ${webcam.title}")
        Text("Location: ${webcam.location.city}, ${webcam.location.country}")
        Spacer(modifier = Modifier.height(16.dp))
        TextButton(onClick = {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(webcam.urls.detail))
            context.startActivity(intent)
        }) {
            Text("Windy URL")
        }
        TextButton(onClick = {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(webcam.urls.provider))
            context.startActivity(intent)
        }) {
            Text("Provider URL")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onBack) {
            Text("Back to List")
        }
    }
}

sealed class WebCamState {
    data object LocationInput : WebCamState()

    // Use the getter to populate the webcams list
    data class List(val webcams: kotlin.collections.List<WebCam>) : WebCamState()

    // Use the getter to provide a default value for webcamList
    data class Detail(
        val webcam: WebCam,
        val webcamList: kotlin.collections.List<WebCam> = emptyList()
    ) : WebCamState()
}

