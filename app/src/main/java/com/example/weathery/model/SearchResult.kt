package com.example.weathery.model

data class SearchResult(
    val name: String,
    val region: String,
    val country: String,
    val lat: Double,
    val lon: Double
)
