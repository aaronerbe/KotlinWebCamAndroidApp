// Defines classes to represent the structure of the JSON response.
// Each section of the JSON data is mapped to its own class.
// Tools used: https://json2kotlin.com/ + ChatGPT for setup and organization.

package com.example.kotlinwebcamandroidapp.model
import kotlinx.serialization.Serializable

// Marks the class as serializable, enabling the Kotlinx Serialization library to:
// - ENCODE: Convert an instance of the class into a specific data format (e.g., JSON).
// - DECODE: Convert data from a specific format back into an instance of the class.
@Serializable
data class WebCamResponse(      // Represents the top-level JSON object. Contains metadata and a list of webcams.
    val total: Int,             // Total number of webcams returned in the response.
    val webcams: List<WebCam>   // List of WebCam objects, representing individual webcams.
)

@Serializable
data class WebCam(              // Represents an individual webcam and its top-level details.
    val title: String,          // The title or name of the webcam.
    val viewCount: Int,         // The number of views for this webcam.
    val webcamId: Long,         // Unique identifier for the webcam.
    val status: String,         // Status of the webcam (e.g., active or inactive).
    val lastUpdatedOn: String,  // Last update timestamp for the webcam.
    val categories: List<Category>, //List of objects of class Category
    val images: Images,         //Nested object contains data of class Images
    val location: Location,     //Nested object contains data of class Location
    val urls: Urls              //Nested object contains data of class URLs
)

@Serializable
data class Category(            // Represents a category associated with a webcam.
    val id: String,             // Unique identifier for the category.
    val name: String            // Name of the category.
)

@Serializable
data class Images(              // Represents image data for a webcam.
    val current: ImageSet,      //Nested object contains data of class ImageSet
    val sizes: ImageSizes,      //Nested object contains data of class ImageSizes
    val daylight: ImageSet      // Nested object for daylight-specific images.
)

@Serializable
data class ImageSet(            // Represents a set of image URLs for different formats.
    val icon: String,           // URL for the icon-sized image.
    val thumbnail: String,      // URL for the thumbnail-sized image.
    val preview: String         // URL for the preview-sized image.
)

@Serializable
data class ImageSizes(          // Represents dimensions for different image formats.
    val icon: Size,             //Nested object contains data of class Size
    val thumbnail: Size,        //Nested object contains data of class Size
    val preview: Size           //Nested object contains data of class Size
)

@Serializable
data class Size(                // Represents the width and height of an image.
    val width: Int,             // Width of the image in pixels.
    val height: Int             // Height of the image in pixels.
)

@Serializable
data class Location(            // Represents the geographical location of a webcam.
    val city: String,           // Name of the city where the webcam is located.
    val region: String,         // Name of the region or state.
    val region_code: String,    // Geographical code for the region.
    val country: String,        // Name of the country.
    val country_code: String,   // Geographical code for the country.
    val continent: String,      // Name of the continent.
    val continent_code: String, // Geographical code for the continent.
    val latitude: Double,       // Latitude coordinate of the webcam.
    val longitude: Double       // Longitude coordinate of the webcam.
)

@Serializable
data class Urls(                // Represents URLs associated with a webcam.
    val detail: String,         // URL to the webcam's detail page.
    val edit: String,           // URL to edit the webcam's information.
    val provider: String        // URL of the webcam's data provider.
)
