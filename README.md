# Overview
This project takes the KotlinWebCam CLI program and builds it into an Android App.  The App is simple and clean.  
## App Usage and Description
### First Screen (LocationInputScreen):
On the first screen, simply enter the coordinates of interest and click the "Fetch WebCams" button.  Alternatively, the user can click the "Use Current Location" button.  
### Second Screen (WebCamListScreen):
The app will fetch all the webcams in the area around the coordinates provided.  On the 2nd screen it will list them out providing the name and WebCam ID.  Reason for the ID is there are sometimes duplicate names.  
User can then select any WebCam of interest to load the details.  Alternatively, the user can click the back button to return to the initial location entry screen.
### Third Screen (WebCamDetailScreen:
On this screen the app will provide some details of the WebCam including the Title, Location and two links.  The first link is the Windy API link which provides thier built in view of the webcam.  The second will take you to the source of the WebCam for a live feed view.  
The user can also return back to the list by clicking the "Back to List" button.  

## Purpose
Purpose of building this app was to continue learning Kotlin language and begin to learn how to build Android Apps.

## Video Demo
[Software Demo Video](https://youtu.be/_zqjLGvF2jE)

# Development Environment

Tools used to develop this app are:
* Android Studio IDE
* Kotlin
* Windy API

Libraries and Dependencies
* Jetpack Compose Libraries
* Coroutine Libraries
* Location and Permissions Libraries
* Ktor Client
* Kotlinx Serialization
* Material Design Components
  
# Useful Websites
* [Android Developer - Build location-aware apps](https://developer.android.com/develop/sensors-and-location/location)
* [Android Developer - Android Basics with Compose](https://developer.android.com/courses/android-basics-compose/course)
  

# Future Work
* Filtering
  * Currently the WebCam categories and radius are fixed.  I want to add filtering capability without complicating the interface.  
* Marina API
    * I want to include Marina API data to provide a list of Marinas in the area
* GIS Mapping
  * I want to list all API results on a map.  Either give user the option to list the results or view it on an interactive map
* Simplify Location Permission Checks
  * Android requires checking for permissions before getting it.  The way to code is written is somewhat redundant in how it checks.  This is due to the way I broke up the different operations.  I want to clean this up and refactor the code to simplify the process some.
* I want to explore more theming such as logos.  
