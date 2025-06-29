package com.example.weathery.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.weathery.data.WeatherRepository
import com.example.weathery.model.Hour
import com.example.weathery.model.WeatherResponse
import com.example.weathery.util.Resource

@Composable
fun WeatherDetailScreen(
    city: String,
    onBack: () -> Unit
) {
    val repo = remember { WeatherRepository() }
    var state by remember { mutableStateOf<Resource<WeatherResponse>>(Resource.Loading) }
    var hourlyState by remember { mutableStateOf<Resource<List<Hour>>>(Resource.Loading) }

    LaunchedEffect(city) {
        state = Resource.Loading
        hourlyState = Resource.Loading
        try {
            val data = repo.getForecastWeather(city)
            state = Resource.Success(data)

            val hours = data
                .forecast
                ?.forecastday
                ?.firstOrNull()
                ?.hour
                ?: emptyList()
            hourlyState = Resource.Success(hours)

        } catch (e: Exception) {
            state = Resource.Error("Could not fetch weather: ${e.localizedMessage}")
            hourlyState = Resource.Error("Could not fetch hourly forecast")
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        color = Color(0xFF181B23)
    ) {
        when (state) {
            is Resource.Loading -> Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }

            is Resource.Error -> Text(
                (state as Resource.Error).message,
                color = Color.Red,
                modifier = Modifier.padding(32.dp)
            )

            is Resource.Success -> {
                val data = (state as Resource.Success<WeatherResponse>).data
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(18.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(
                            data.location.name,
                            color = Color.White,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    AsyncImage(
                        model = "https:${data.current.condition.icon}",
                        contentDescription = null,
                        modifier = Modifier
                            .size(100.dp)
                            .align(Alignment.CenterHorizontally)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "${data.current.temp_c.toInt()}°",
                        style = MaterialTheme.typography.displayLarge,
                        color = Color.White,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    Text(
                        data.current.condition.text,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    Spacer(Modifier.height(24.dp))

                    when (hourlyState) {
                        is Resource.Loading -> Box(
                            Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) { CircularProgressIndicator() }
                        is Resource.Error -> Text(
                            (hourlyState as Resource.Error).message,
                            color = Color.Red,
                            modifier = Modifier.padding(12.dp)
                        )
                        is Resource.Success -> {
                            val hours = (hourlyState as Resource.Success<List<Hour>>).data
                            if (hours.isNotEmpty()) {
                                Text(
                                    "Today's Hourly Forecast",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                                HourlyForecastRow(hours)
                            }
                        }
                        else -> {}
                    }
                    WeatherDetailGrid(data)
                }
            }
            else -> {}
        }
    }
}

@Composable
fun WeatherDetailGrid(data: WeatherResponse) {
    val infoList = listOf(
        "Humidity" to "${data.current.humidity}%",
        "Wind" to "${data.current.wind_kph} km/h",
        "Pressure" to "${data.current.pressure_mb} hPa",
        "Feels like" to "${data.current.feelslike_c.toInt()}°",
        "UV" to "${data.current.uv}",
        "Visibility" to "${data.current.vis_km} km",
//        "Wind Dir" to data.current.wind_dir,
        "Precipitation" to "${data.current.precip_mm} mm",
        "Heat Index" to "${data.current.heatindex_c}°",
        "Wind Gusts" to "${data.current.gust_kph} km/h",
        "Cloud Cover" to "${data.current.cloud}%"
    )

    Surface(
        color = Color(0xFF23253A),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .statusBarsPadding()
    ) {
        Column(Modifier.padding(18.dp)) {
            for (i in infoList.indices step 2) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    WeatherInfoCard(
                        title = infoList[i].first,
                        value = infoList[i].second,
                        modifier = Modifier
                            .weight(1f)
                            .height(62.dp)
                    )
                    if (i + 1 < infoList.size) {
                        WeatherInfoCard(
                            title = infoList[i + 1].first,
                            value = infoList[i + 1].second,
                            modifier = Modifier
                                .weight(1f)
                                .height(62.dp)
                        )
                    } else {
                        Spacer(Modifier.weight(1f))
                    }
                }
                Spacer(Modifier.height(10.dp))
            }
        }
    }
}

@Composable
fun WeatherInfoCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier
            .background(Color(0xFF181B23), RoundedCornerShape(12.dp))
            .padding(vertical = 8.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(title, color = Color(0xFFB0B8C1), fontSize = MaterialTheme.typography.bodySmall.fontSize)
        Spacer(Modifier.height(5.dp))
        Text(value, color = Color.White, fontWeight = FontWeight.Bold)
    }
}
@Composable
fun HourlyForecastRow(hours: List<Hour>) {
    // show every 2nd hour
    val filtered = hours.filterIndexed { index, _ -> index % 2 == 0 }

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        items(filtered) { hour ->
            Card(
                modifier = Modifier
                    .width(80.dp)
                    .height(140.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF23253A)),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 12.dp, horizontal = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${hour.temp_c.toInt()}°",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                    AsyncImage(
                        model = "https:${hour.condition.icon}",
                        contentDescription = hour.condition.text,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = hour.time.takeLast(5),  // "HH:mm"
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFB0B8C1)
                    )
                }
            }
        }
    }
}




