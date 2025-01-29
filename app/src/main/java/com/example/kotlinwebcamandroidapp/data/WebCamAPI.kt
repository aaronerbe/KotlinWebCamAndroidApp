package com.example.kotlinwebcamandroidapp.data

// Import necessary libraries
import com.example.kotlinwebcamandroidapp.model.WebCamResponse
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

/**
 * Handles API requests for fetching webcam data from the Windy API.
 */
object WebCamAPI {
    private const val API_KEY = "hxK1iiN4GCkjymbhFO76k67rzmfQ60M1"

    // JSON configuration for parsing API responses
    private val json = Json {
        prettyPrint = true
        isLenient = true
        ignoreUnknownKeys = true
    }

    /**
     * Fetches webcam data from the Windy API.
     * @param lat Latitude for the request
     * @param lon Longitude for the request
     * @return WebCamResponse? (Null if API call fails)
     */
    suspend fun fetchWebCamData(lat: Double, lon: Double): WebCamResponse? {
        val url = buildBaseURL(lat, lon)
        println("DEBUG API URL: $url")

        val client = HttpClient(CIO) {
            install(ContentNegotiation) {
                json(json)
            }
        }

        return try {
            val response: HttpResponse = client.get(url) {
                expectSuccess = true
                headers {
                    append("Content-Type", "application/json")
                    append("x-windy-api-key", API_KEY)
                }
            }

            if (response.status.value in 200..299) {
                val responseBody = response.bodyAsText()
                json.decodeFromString<WebCamResponse>(responseBody)
            } else {
                println("DEBUG API Error: ${response.status}")
                null
            }
        } catch (e: Exception) {
            println("DEBUG API Exception: ${e.message}")
            null
        } finally {
            client.close()
        }
    }

    /**
     * Builds the Windy API URL based on given latitude and longitude.
     */
    private fun buildBaseURL(lat: Double, lon: Double): String {
        return "https://api.windy.com/webcams/api/v3/webcams?offset=0&categoryOperation=or&nearby=${lat},${lon},250&include=categories,images,location,player,urls&categories=water,island,beach,harbor,bay,coast,underwater,mountain,park,sportarea"
    }
}
