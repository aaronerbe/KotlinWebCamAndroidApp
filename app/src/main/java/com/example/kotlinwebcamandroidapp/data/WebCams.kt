package com.example.kotlinwebcamandroidapp.data

//CLIENT IMPORTS
import com.example.kotlinwebcamandroidapp.model.WebCamResponse
import com.example.kotlinwebcamandroidapp.model.WebCam
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
//import io.ktor.client.plugins.logging.*
//REQUEST IMPORTS
import io.ktor.client.request.*
import io.ktor.client.statement.*
//SERIALIZER AND CONTENT NOTIFICATION FOR JSON
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.*


/**
 * Create a Singleton object to hold the reusable Json configuration.
 * Just saves some duplication in the setup below
 */
object JsonConfig {
    val json = Json {
        prettyPrint = true
        isLenient = true
        ignoreUnknownKeys = true // Removes unknown keys (player)
    }
}

/**
 * WebCam Class.
 * Inputs lat/lon
 */
class WebCams(private val lat: Double = 0.0, private val lon: Double = 0.0) {
    private val apiKey: String = "hxK1iiN4GCkjymbhFO76k67rzmfQ60M1"
    private var data: WebCamResponse? = null // Create Nullable data based on WebCamResponse classes to store the parsed data in

    /**
     * Initializes the com.example.kotlinwebcamandroidapp.data.WebCams object by fetching and parsing response into webcam data.
     */
    suspend fun init() {
        println("DEBUG API INIT $lat and $lon")
        data = fetchWebCamData() // Fetch and parse the data
        println("DEBUG API $data")
    }

    /**
     * Fetches the webcam data from the Windy API and deserialize the json response using WebCamResponse
     */
    private suspend fun fetchWebCamData(): WebCamResponse? {
        println("DEBUG API fetchWEbCamData")
        val url = buildBaseURL()
        println("DEBUG API URL:  $url")

        //BUILD THE CLIENT
        val client = HttpClient(CIO) {   //CIO is the 'engine'
            //adds logging plugin
            //install(Logging) {
            //level = LogLevel.HEADERS
            //}
            //add content negotiation for handling json data
            install(ContentNegotiation) {
                //calls the method
                json(JsonConfig.json) // Use the singleton Json instance here
            }
        }
        return try {
            println("DEBUG API entering try sequence")
            // MAKE THE REQUEST
            val response: HttpResponse = client.get(url) {
                expectSuccess = true // Enables default validation of response
                headers {
                    append("Content-Type", "application/json")
                    append("x-windy-api-key", apiKey)
                }
            }
            println("DEBUG API Status ${response.status.value}")
            println("DEBUG API Response: $response") //logging reponse for debug

            if (response.status.value in 200..299) {
                //println("Successful response!")
                // DESERIALIZE the JSON response into WebCamResponse
                val responseBody = response.bodyAsText()

                JsonConfig.json.decodeFromString<WebCamResponse>(responseBody) // Using the singleton Json instance
            } else {
                println("DEBUG API else statement ${response.status}")
                println("Error: ${response.status}")
                null
            }
        } catch (e: Exception) {
            println("Exception during request: ${e.message}")
            null
        } finally {
            client.close() // CLOSE THE CLIENT
        }
    }

    /**
     * Getter to return a List of WebCams for the App
     */
    fun getWebcams(): List<WebCam> {
        return data?.webcams ?: emptyList()
    }

    /**
     * Builds a custom URL for accessing the Windy API based on the provided lat/lon parameters.
     */
    private fun buildBaseURL(): String {
        return "https://api.windy.com/webcams/api/v3/webcams?offset=0&categoryOperation=or&nearby=${lat},${lon},250&include=categories,images,location,player,urls&categories=water,island,beach,harbor,bay,coast,underwater,mountain,park,sportarea"
    }

    /**
     * Check Lat/Lon against the data to see if they return anything valid
     * Return Boolean
     */
    fun checkLatLon(): Boolean{
        return data?.webcams?.isNotEmpty() ?: false //see isValidID
    }

    /**
     * Check if valid webcam ID by checking against data for valid
     * Return Boolean
     */
    fun isValidID(webCamID: Long): Boolean {
        return data?.webcams?.any { it.webcamId == webCamID } ?: false
        //data?.webcams? handles if it comes back null
        //checks if any elements exist in the list for that id
        //?: is an 'Elvis' operator.  gives a fallback if the expression returns null.  so it will return false if webcamId returns a null value
    }

    /**
     * prints out the titles and webcam ids of the webcams
     */
    fun printTitles() {
        println()
        if (data != null) {
            println("Total webcams: ${data!!.total}")
            for (webcam in data!!.webcams) {
                println("Title: ${webcam.title} - WebCam ID:  ${webcam.webcamId}")
            }
        } else {
            println("No data to present.")
        }
    }

    /**
     * Input webCamID
     * Prints out details of the selected webCam
     */
    fun printDetailsByWebCamID(webCamID: Long) {
        println()
        if (data == null){  //if data is empty, print out msg
            println("No data available for webCamId: $webCamID")
            return
        }
        //Find the webcam by ID
        val webcam = data!!.webcams.find { it.webcamId == webCamID}
        if(webcam != null){  //assign all the variables
            val title = webcam.title
            val windyUrl = webcam.urls.detail
            val providerUrl = webcam.urls.provider
            val imagePreview = webcam.images.current.preview
            val city = webcam.location.city
            val country = webcam.location.country
            //Print the Details
            println("Details for $title (ID: $webCamID)")
            println("$city, $country")
            println("Windy URL: $windyUrl")
            println("Provider URL: $providerUrl")
            println("Image Preview URL: $imagePreview")
        } else {
            println("No webcam found with ID: $webCamID")
        }
    }


}
