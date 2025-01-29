package com.example.kotlinwebcamandroidapp.data

// Import necessary libraries
import com.example.kotlinwebcamandroidapp.model.WebCam
import com.example.kotlinwebcamandroidapp.model.WebCamResponse

/**
 * WebCams class for managing webcam data.
 * Calls WebCamAPI to fetch and store webcam data.
 */
class WebCams(private val lat: Double = 0.0, private val lon: Double = 0.0) {
    private var data: WebCamResponse? = null // Stores API response

    /**
     * Initializes the WebCams object by fetching and parsing webcam data.
     */
    suspend fun init() {
        println("DEBUG API INIT: Fetching data for $lat, $lon")
        data = WebCamAPI.fetchWebCamData(lat, lon)
        println("DEBUG API Response: $data")
    }

    /**
     * Returns the list of webcams or an empty list if no data is available.
     */
    fun getWebcams(): List<WebCam> {
        return data?.webcams ?: emptyList()
    }

    /**
     * Below methods are from CLI version.  Not used in Android App
     */
//
//    /**
//     * Checks if valid webcam data exists.
//     */
//    fun hasValidData(): Boolean {
//        return data?.webcams?.isNotEmpty() ?: false
//    }
//
//    /**
//     * Checks if a given webcam ID exists.
//     */
//    fun isValidID(webCamID: Long): Boolean {
//        return data?.webcams?.any { it.webcamId == webCamID } ?: false
//    }
//
//    /**
//     * Prints the titles and webcam IDs of all fetched webcams.
//     */
//    fun printTitles() {
//        println()
//        if (data != null) {
//            println("Total webcams: ${data!!.total}")
//            data!!.webcams.forEach {
//                println("Title: ${it.title} - WebCam ID: ${it.webcamId}")
//            }
//        } else {
//            println("No data available.")
//        }
//    }
//
//    /**
//     * Prints detailed information about a specific webcam by ID.
//     */
//    fun printDetailsByWebCamID(webCamID: Long) {
//        println()
//        val webcam = data?.webcams?.find { it.webcamId == webCamID }
//        if (webcam != null) {
//            println("Details for ${webcam.title} (ID: $webCamID)")
//            println("${webcam.location.city}, ${webcam.location.country}")
//            println("Windy URL: ${webcam.urls.detail}")
//            println("Provider URL: ${webcam.urls.provider}")
//            println("Image Preview URL: ${webcam.images.current.preview}")
//        } else {
//            println("No webcam found with ID: $webCamID")
//        }
//    }
}
