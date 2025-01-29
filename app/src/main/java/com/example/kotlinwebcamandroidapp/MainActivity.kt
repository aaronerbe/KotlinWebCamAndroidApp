// MainActivity.kt
package com.example.kotlinwebcamandroidapp

// Core Android Imports
import android.content.Intent // To open external URLs
import android.net.Uri // For handling URL
import android.os.Bundle // Stores and transfers data between screens (activities)
import androidx.activity.ComponentActivity // Base class for activities (screens) that use Jetpack Compose
import androidx.activity.compose.setContent // Sets the Compose UI as the activity's content

// Jetpack Compose Imports
import androidx.compose.foundation.clickable // Click functionality in Composable UI
import androidx.compose.foundation.layout.* // Layout tools like Column, Row, etc.
import androidx.compose.material3.* // Material Design components
import androidx.compose.runtime.* // State management in Compose
import androidx.compose.ui.Modifier // Modifier allows customization of UI elements (size, padding, etc)
import androidx.compose.ui.platform.LocalContext // Access apps context
import androidx.compose.ui.unit.dp // Dimensions in density-independent pixels (dp)

// App specific imports
import com.example.kotlinwebcamandroidapp.data.WebCams // webcam data retrieval
import com.example.kotlinwebcamandroidapp.model.WebCam // defines webcam data model
import com.example.kotlinwebcamandroidapp.model.Coordinates // import coordinates class


// Coroutines for async operations
import kotlinx.coroutines.CoroutineScope // For managing coroutines
import kotlinx.coroutines.Dispatchers // Specifies coroutine threads
import kotlinx.coroutines.launch // Launches a coroutine
import kotlinx.coroutines.runBlocking // Runs coroutines synchronously (blocking functions)
import kotlinx.coroutines.withContext // Switches coroutine contexts

// Location permissions and compatibility imports
import android.Manifest // For requesting location permissions
import android.content.pm.PackageManager // For checking permission status
import androidx.activity.result.contract.ActivityResultContracts // Manages permission requests
import androidx.core.content.ContextCompat // Provides compatibility methods for permissions
import androidx.core.app.ActivityCompat // For permission handling
import com.google.android.gms.location.LocationServices // Provides location services
import com.google.android.gms.location.FusedLocationProviderClient // Accesses the device's location
import android.location.Location // Represents a location
import android.widget.Toast // Displays short messages to the user

// Android Back Button handling
import androidx.activity.compose.BackHandler //Allows me to customize back button so it doesn't just close the app

// UI utilities
import com.example.kotlinwebcamandroidapp.ui.theme.KotlinWebCamAndroidAppTheme
import androidx.compose.foundation.rememberScrollState // allows scrolling
import androidx.compose.foundation.verticalScroll  //for vertical scrolling
import androidx.compose.foundation.background // set background color
import androidx.compose.ui.draw.clip  // clips ui elements
import androidx.compose.ui.text.style.TextAlign  //align text

// Previewing
import androidx.compose.ui.tooling.preview.Preview  //for previewing composables

// Coroutine Utilities
import kotlin.coroutines.resume  //resume suspended coroutines
import kotlin.coroutines.suspendCoroutine  //suspend execution till resumed


/**
 * MainActivity Class is the entry point of the app.
 * In here, I have code to verify permissions and fetch the user's location.
 */
class MainActivity : ComponentActivity() {
    // FusedLocationProviderClient
    // provides access to location services for retrieving the device's location
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize FusedLocationProviderClient for accessing location services
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Apply the theme to the entire app
        setContent {
            KotlinWebCamAndroidAppTheme {
                WebCamApp(getCoordinates = { getCoordinates() })
            }
        }
    }

    /**
     * Public getter for getting coordinates
     * Handles checking permissions, calling launcher if needed and getting coords
     * Note:  suspend needed since it's an async call to get the coords.  Needed to avoid returning null before it was done actaully getting the coords
     */
    private suspend fun getCoordinates(): Coordinates? {
        return checkLocationPermission()
    }

    /**
     * Checks if location permissions are granted.
     * If granted, fetch the location.
     * If not, requests the necessary permissions using the permissions launcher
     * Input:  onLocationReady
     */
    private suspend fun checkLocationPermission(): Coordinates? {
        val fineLocationPermission = Manifest.permission.ACCESS_FINE_LOCATION
        val coarseLocationPermission = Manifest.permission.ACCESS_COARSE_LOCATION

        return when {
            // Permissions are already granted
            ContextCompat.checkSelfPermission(this, fineLocationPermission) == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, coarseLocationPermission) == PackageManager.PERMISSION_GRANTED -> {
                fetchUserLocation() // Fetch and return the location
            }

            // Request permissions if not granted
            else -> {
                requestPermissionsLauncher.launch(
                    arrayOf(fineLocationPermission, coarseLocationPermission)
                )
                null // Permissions are being requested, return null for now
            }
        }
    }

    /**
     * Registers a permission request launcher to handle the result of permission requests.
     * This will handle whether the user grants or denies the location permissions.
     */
    private val requestPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // Check if either FINE or COARSE location permission was granted
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        ) {
            println("DEBUG LOCATION INPUT: Permissions granted")
        } else {
            // Notify the user that permissions were denied
            Toast.makeText(this, "Location permissions denied", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Fetches the user's last known location using FusedLocationProviderClient.
     * This function is a suspend function that can only be called within a coroutine.
     *
     * @return Coordinates? The user's last known coordinates or null if unavailable or permissions are missing.
     */
    private suspend fun fetchUserLocation(): Coordinates? {
        // Double Check if the app has the necessary permissions for accessing location
        // This is necessary to make kotlin happy even though we already check for permissions.  It doesn't see that the fetch is already dependent on the check
        //TODO look for way to clean this up and remove redundant check.
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION // Fine-grained location
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION // Coarse-grained location
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // If permissions are not granted, return null
            return null
        }

        // Suspend the coroutine while waiting for the result from the FusedLocationProviderClient
        return suspendCoroutine { continuation ->
            // Request the last known location from the FusedLocationProviderClient
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        // If a location is successfully retrieved, extract latitude and longitude
                        val userLat = location.latitude
                        val userLon = location.longitude
                        println("DEBUG LOCATION INPUT: Latitude: $userLat, Longitude: $userLon")
                        // Resume the coroutine with the fetched coordinates
                        continuation.resume(Coordinates(userLat, userLon))
                    } else {
                        // If no location is available, log a message and resume with null
                        println("DEBUG LOCATION INPUT: Unable to fetch location.")
                        continuation.resume(null)
                    }
                }
                .addOnFailureListener { exception ->
                    // If there was an error retrieving the location, log the error and resume with null
                    println("DEBUG LOCATION INPUT: Error retrieving location: ${exception.message}")
                    continuation.resume(null)
                }
        }
    }
}

/**
 * Builds a WebCams object using the given latitude and longitude.
 * This is a blocking operation for simplicity in this learning context.
 */
fun buildData(lat: Double, lon: Double): WebCams = runBlocking {
    val webCams = WebCams(lat, lon) // Create a WebCams object with the given coordinates
    webCams.init() // Fetch data from the API and initialize the object
    webCams // Return the initialized object
}

/**
 * Main Composable function for the app.
 * Handles screen transitions and passes the getUserLocation function to the appropriate screens.
 */
@Composable
fun WebCamApp(getCoordinates: suspend () -> Coordinates?) {
    var currentScreen by remember { mutableStateOf<WebCamState>(WebCamState.LocationInput) }

    // Handle the device back button so it goes back instead of closing app
    BackHandler {
        when (currentScreen) {
            is WebCamState.List -> currentScreen = WebCamState.LocationInput // Back from List to LocationInput
            is WebCamState.Detail -> currentScreen = WebCamState.List((currentScreen as WebCamState.Detail).webcamList) // Back from Detail to List
            else -> {} // No action for LocationInput (let the system close the app)
        }
    }

    when (val state = currentScreen) {
        is WebCamState.LocationInput -> {
            LocationInputScreen(
                getCoordinates = getCoordinates,
                onLocationSubmit = { lat, lon ->
                    CoroutineScope(Dispatchers.IO).launch {
                        val webCams = buildData(lat, lon)
                        val webcamsList = webCams.getWebcams()
                        withContext(Dispatchers.Main) {
                            currentScreen = WebCamState.List(webcamsList)
                        }
                    }
                }
            )
        }
        is WebCamState.List -> {
            WebCamListScreen(
                webcams = state.webcams,
                onWebCamSelected = { id ->
                    val selectedWebCam = state.webcams.firstOrNull { webcam: WebCam -> webcam.webcamId == id }
                    selectedWebCam?.let {
                        currentScreen = WebCamState.Detail(selectedWebCam, state.webcams)
                    }
                },
                onBack = {
                    currentScreen = WebCamState.LocationInput
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
fun LocationInputScreen(
    getCoordinates: suspend () -> Coordinates?, // Passed in Function from MainActivity to get user-provided coordinates
    onLocationSubmit: (Double, Double) -> Unit // Function to submit user-provided latitude and longitude
) {
    // State variables for latitude and longitude input fields
    var lat by remember { mutableStateOf("") } // User provided latitude as a string
    var lon by remember { mutableStateOf("") } // User provided longitude as a string

    // State variables  for tracking validation of data
    var isLatError by remember { mutableStateOf(false) } // Flag for invalid latitude input
    var isLonError by remember { mutableStateOf(false) } // Flag for invalid longitude input

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
        ) {

        // Layout container for input fields and buttons
        Column(
            modifier = Modifier
                .fillMaxSize() // Fill the available screen space
                .padding(16.dp), // Add padding around the content
            verticalArrangement = Arrangement.Center // Center content vertically
        ) {
            Text(
                "Enter Latitude and Longitude",
                style = MaterialTheme.typography.headlineMedium, // Apply headline style
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center, // Center the text
                modifier = Modifier.fillMaxWidth() // Make the text fill the available width
                .padding(bottom = 32.dp) // Add padding below the text
            )
            // Button to fetch and use the device's current location
            Button(
                onClick = {
                    CoroutineScope(Dispatchers.IO).launch {
                        val coords = getCoordinates() // Suspend function call
                        withContext(Dispatchers.Main) {
                            if (coords != null) {
                                lat = coords.latitude.toString()
                                lon = coords.longitude.toString()
                                println("DEBUG LOCATION INPUT: $lat, $lon")
                                onLocationSubmit(coords.latitude, coords.longitude)
                            } else {
                                println("DEBUG LOCATION INPUT: Coordinates are null or permissions denied.")
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = MaterialTheme.shapes.large, // Use the medium shape from the theme
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary, // Use secondary color for this button
                    contentColor = MaterialTheme.colorScheme.onSecondary // Use contrast color
                )
            ) {
                Text("Use Current Location")
            }

            // Input field for latitude
            OutlinedTextField(
                value = lat, // Current value of the latitude input
                onValueChange = {
                    lat = it // Update the latitude value
                    isLatError = false // Reset the error flag
                },
                label = { Text("Latitude - e.g. 43.5") }, // Placeholder text
                isError = isLatError, // Highlight the field if there's an error
                modifier = Modifier.fillMaxWidth(), // Make the input field full-width
                shape = MaterialTheme.shapes.small // Small rounded corners

            )
            // Show an error message if the latitude is invalid
            if (isLatError) {
                Text(
                    "Invalid latitude. Must be between -90 and 90.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(16.dp)) // Add spacing between fields

            // Input field for longitude
            OutlinedTextField(
                value = lon, // Current value of the longitude input
                onValueChange = {
                    lon = it // Update the longitude value
                    isLonError = false // Reset the error flag
                },
                label = { Text("Longitude - e.g. 116") }, // Placeholder text
                isError = isLonError, // Highlight the field if there's an error
                modifier = Modifier.fillMaxWidth(), // Make the input field full-width
                shape = MaterialTheme.shapes.small
            )
            // Show an error message if the longitude is invalid
            if (isLonError) {
                Text(
                    "Invalid longitude. Must be between -180 and 180.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(16.dp)) // Add spacing between fields

            // Button to submit the latitude and longitude
            Button(
                onClick = {
                    val latValue = lat.toDoubleOrNull() // Convert latitude to Double
                    val lonValue = lon.toDoubleOrNull() // Convert longitude to Double

                    // Validate the latitude value
                    if (latValue == null || latValue !in -90.0..90.0) {
                        isLatError = true
                    }

                    // Validate the longitude value
                    if (lonValue == null || lonValue !in -180.0..180.0) {
                        isLonError = true
                    }

                    // Submit the values if both are valid
                    if (!isLatError && !isLonError && latValue != null && lonValue != null) {
                        onLocationSubmit(latValue, lonValue)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary, // Use primary color
                    contentColor = MaterialTheme.colorScheme.onPrimary // Use onPrimary for contrast
                ),
                modifier = Modifier.fillMaxWidth(), // Make the button full-width
                shape = MaterialTheme.shapes.medium
            ) {
                Text("Fetch WebCams") // Button label
            }
        }
    }
}

/**
 * Webcam List Screen
 * Displays a scrollable list of webcams with a back button.
 */
@Composable
fun WebCamListScreen(
    webcams: List<WebCam>, // List of webcams to display
    onWebCamSelected: (Long) -> Unit, // Function to handle when a webcam is selected
    onBack: () -> Unit // Function to handle back navigation
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        // Layout container for the webcam list
        Column(
            modifier = Modifier
                .fillMaxSize() // Fill the available screen space
                .padding(16.dp) // Add padding around the content
        ) {
            Spacer(modifier = Modifier.height(62.dp))

            // Title for the screen
            Text(
                "Available WebCams",
                style = MaterialTheme.typography.headlineMedium, // Apply headline style
                color = MaterialTheme.colorScheme.onBackground,
            )
            // Back button
            Button(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("Back")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Scrollable list of webcams
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                webcams.forEach { webcam ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clip(MaterialTheme.shapes.medium)
                            .background(MaterialTheme.colorScheme.surface) // Surface background color
                            .clickable { onWebCamSelected(webcam.webcamId) }
                            .padding(16.dp) // Inner padding
                    ) {
                        Column {
                            Text(
                                webcam.title,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "ID: ${webcam.webcamId}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * WebCam Detail Screen
 * Displays detailed information about a selected webcam, including title, location, and URLs.
 * Provides navigation back to the list screen.
 */
@Composable
fun WebCamDetailScreen(
    webcam: WebCam, // Webcam details to display
    onBack: () -> Unit // Function to handle back navigation
) {
    val context = LocalContext.current // Provides access to the current context

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        // Layout container for the detail view
        Column(
            modifier = Modifier
                .fillMaxSize() // Fill the available screen space
                .padding(16.dp) // Add padding around the content
        ) {
            // Add spacing before the title
            Spacer(modifier = Modifier.height(62.dp))

            // Title for the screen
            Text(
                "WebCam Details",
                style = MaterialTheme.typography.headlineMedium, // Apply headline style
                color = MaterialTheme.colorScheme.onBackground,
            )

            // Add spacing between title and details
            Spacer(modifier = Modifier.height(16.dp))

            // Display webcam details (title, location)
            Text(
                text = "Title: ${webcam.title}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground // Use appropriate contrast color
            )
            Text(
                text = "Location: ${webcam.location.city}, ${webcam.location.country}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )

            // Add spacing between details and URLs
            Spacer(modifier = Modifier.height(16.dp))

            // Button to open the Windy URL
            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(webcam.urls.detail))
                    context.startActivity(intent) // Open the URL in a browser
                },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium, // Apply consistent rounded corners
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary, // Use secondary color for this button
                    contentColor = MaterialTheme.colorScheme.onSecondary // Use contrast color
                )
            ) {
                Text("Windy URL")
            }

            // Add spacing between buttons
            Spacer(modifier = Modifier.height(16.dp))

            // Button to open the provider URL
            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(webcam.urls.provider))
                    context.startActivity(intent) // Open the URL in a browser
                },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                )
            ) {
                Text("Provider URL")
            }

            // Add spacing between URL buttons and the back button
            Spacer(modifier = Modifier.height(16.dp))

            // Back button to navigate back to the list screen
            Button(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium, // Consistent rounded corners for the back button
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary, // Use primary color for the back button
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("Back to List") // Label for the back button
            }
        }
    }
}

// Sealed class to represent the different states/screens of the app
sealed class WebCamState {
    // State for the location input screen
    data object LocationInput : WebCamState()

    // State for the list screen, with a list of webcams
    data class List(val webcams: kotlin.collections.List<WebCam>) : WebCamState()

    // State for the detail screen, with a selected webcam and the full list of webcams
    data class Detail(
        val webcam: WebCam, // Selected webcam
        val webcamList: kotlin.collections.List<WebCam> = emptyList() // Default to an empty list
    ) : WebCamState()
}


@Preview(showBackground = true)
@Composable
fun PreviewLocationInputScreen() {
    MaterialTheme {
        LocationInputScreen(
            getCoordinates = { null }, // Provide a dummy implementation for preview
            onLocationSubmit = { _, _ -> } // Provide a no-op function for location submission
        )
    }
}



