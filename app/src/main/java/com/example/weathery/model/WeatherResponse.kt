package com.example.weathery.model

data class WeatherResponse(
    val location: Location,
    val current: Current,
    val forecast: Forecast?
)

data class Location(
    val name: String,
    val region: String,
    val country: String,
    val lat: Double,
    val lon: Double,
    val tz_id: String,
    val localtime: String
)

data class Current(
    val temp_c: Double,
    val temp_f: Double,
    val is_day: Int,
    val condition: Condition,
    val wind_kph: Double,
    val humidity: Int,
    val vis_km: Double,
    val uv: Double,
    val feelslike_c: Float,
    val pressure_mb: Float,
    val wind_dir: String,
    val precip_mm: Float,
    val heatindex_c: Float,
    val gust_kph: Float,
    val cloud: Int
)

data class Condition(
    val text: String,
    val icon: String,
    val code: Int
)
data class WeatherTileData(
    val city: String,
    val tempC: Int,
    val iconUrl: String,
    val condition: String,
    val time: String
)
data class Forecast(
    val forecastday: List<ForecastDay>
)

data class ForecastDay(
    val date: String,
    val hour: List<Hour>
)

data class Hour(
    val time: String,
    val temp_c: Double,
    val condition: Condition
)