package com.example.weathery.data

import com.example.weathery.Constants
import com.example.weathery.model.WeatherResponse
import com.example.weathery.model.WeatherTileData

class WeatherRepository {
    private val api = RetrofitInstance.api

    suspend fun getCurrentWeather(q: String): WeatherResponse =
        api.getCurrentWeather(Constants.API_KEY, q)

    suspend fun getForecastWeather(q: String): WeatherResponse =
        api.getForecastWeather(Constants.API_KEY, q, days = 1)

    suspend fun getMultipleCitiesWeather(cities: List<String>): List<WeatherTileData> {
        return cities.mapNotNull { city ->
            try {
                val response = getCurrentWeather(city)
                WeatherTileData(
                    city = response.location.name,
                    tempC = response.current.temp_c.toInt(),
                    iconUrl = response.current.condition.icon,
                    condition = response.current.condition.text,
                    time = response.location.localtime.substringAfter(" ")
                )
            } catch (e: Exception) {
                null
            }
        }
    }
}
