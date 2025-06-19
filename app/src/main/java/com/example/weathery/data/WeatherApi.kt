package com.example.weathery.data


import com.example.weathery.model.SearchResult
import com.example.weathery.model.WeatherResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApi {
    @GET("search.json")
    suspend fun searchCities(
        @Query("key") key: String,
        @Query("q")   query: String
    ): List<SearchResult>

    @GET("current.json")
    suspend fun getCurrentWeather(
        @Query("key") key: String,
        @Query("q")   query: String
    ): WeatherResponse
}
