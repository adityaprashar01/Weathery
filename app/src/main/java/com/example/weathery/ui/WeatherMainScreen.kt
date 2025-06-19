package com.example.weathery.ui

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.weathery.model.WeatherTileData
import com.example.weathery.viewmodel.SearchViewModel
import com.example.weathery.util.Resource
import com.google.android.gms.location.LocationServices

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherMainScreen(
    onCityClick: (String) -> Unit,
    onLocationResult: (String) -> Unit,
    vm: SearchViewModel = viewModel()
) {
    // Location permission logic
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val locationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        val fine = perms[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarse = perms[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        if (fine || coarse) {
            if (
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
            ) {
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { loc ->
                        if (loc != null) {
                            onLocationResult("${loc.latitude},${loc.longitude}")
                        } else {
                            Toast.makeText(context, "Location unavailable", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        } else {
            Toast.makeText(context, "Location permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    var query by remember { mutableStateOf("") }
    val weatherTiles by vm.weatherTiles.observeAsState(Resource.Loading)
    val singleResult by vm.singleResult.observeAsState(Resource.Success(null))

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    locationLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                },
                containerColor = Color.White
            ) {
                Icon(Icons.Filled.LocationOn, contentDescription = "Get Current Location")
            }
        },
        containerColor = Color(0xFF181B23)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            // Search bar
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 12.dp)
                    .height(54.dp)
                    .background(Color(0xFF23253A), RoundedCornerShape(14.dp)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = query,
                    onValueChange = {
                        query = it
                        if (query.isEmpty()) vm.loadTopCities()
                    },
                    placeholder = { Text("Search", color = Color(0xFFB0B8C1)) },
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = Color.White,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                    ),
                    textStyle = LocalTextStyle.current.copy(color = Color.White),
                    shape = RoundedCornerShape(14.dp)
                )

                // "Cancel" text/button
                if (query.isNotEmpty()) {
                    TextButton(
                        onClick = {
                            query = ""
                            vm.loadTopCities()
                        },
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("Cancel", color = Color(0xFFB0B8C1))
                    }
                }
            }


            Spacer(Modifier.height(8.dp))

            if (query.isEmpty()) {
                when (weatherTiles) {
                    is Resource.Loading -> Box(
                        Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) { CircularProgressIndicator() }
                    is Resource.Success -> {
                        val data = (weatherTiles as Resource.Success<List<WeatherTileData>>).data
                        LazyColumn {
                            items(data.size) { i ->
                                WeatherTile(
                                    tile = data[i],
                                    onClick = { onCityClick(data[i].city) }
                                )
                            }
                        }
                    }
                    is Resource.Error -> Text(
                        (weatherTiles as Resource.Error).message,
                        color = Color.Red,
                        modifier = Modifier.padding(20.dp)
                    )
                    else -> {}
                }
            } else {
                // Searching
                LaunchedEffect(query) { vm.search(query) }
                when (singleResult) {
                    is Resource.Loading -> Box(
                        Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) { CircularProgressIndicator() }
                    is Resource.Success -> {
                        val tile = (singleResult as Resource.Success<WeatherTileData?>).data
                        if (tile != null) {
                            WeatherTile(tile = tile, onClick = { onCityClick(tile.city) })
                        } else {
                            Text(
                                "No result found.",
                                color = Color.Gray,
                                modifier = Modifier.padding(20.dp)
                            )
                        }
                    }
                    is Resource.Error -> Text(
                        (singleResult as Resource.Error).message,
                        color = Color.Red,
                        modifier = Modifier.padding(20.dp)
                    )
                    else -> {}
                }
            }
        }
    }
}

@Composable
fun WeatherTile(
    tile: WeatherTileData,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF23253A)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp, horizontal = 18.dp)
            .height(78.dp)
    ) {
        Row(
            Modifier
                .fillMaxSize()
                .padding(start = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = "https:${tile.iconUrl}",
                contentDescription = tile.condition,
                modifier = Modifier.size(44.dp)
            )
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(tile.city, color = Color.White, fontWeight = FontWeight.Bold)
                Text(tile.time, color = Color(0xFFB0B8C1), fontWeight = FontWeight.Normal, fontSize = MaterialTheme.typography.bodySmall.fontSize)
            }
            Text(
                "${tile.tempC}°",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = MaterialTheme.typography.headlineLarge.fontSize,
                modifier = Modifier.padding(end = 20.dp)
            )
        }
    }
}
