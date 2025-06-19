package com.example.weathery.viewmodel

import androidx.lifecycle.*
import com.example.weathery.Constants
import com.example.weathery.data.WeatherRepository
import com.example.weathery.model.WeatherTileData
import com.example.weathery.util.Resource
import kotlinx.coroutines.launch

class SearchViewModel : ViewModel() {
    private val repo = WeatherRepository()
    private val _weatherTiles = MutableLiveData<Resource<List<WeatherTileData>>>()
    val weatherTiles: LiveData<Resource<List<WeatherTileData>>> = _weatherTiles

    private val _singleResult = MutableLiveData<Resource<WeatherTileData?>>()
    val singleResult: LiveData<Resource<WeatherTileData?>> = _singleResult

    init {
        loadTopCities()
    }

    fun loadTopCities() {
        viewModelScope.launch {
            _weatherTiles.value = Resource.Loading
            try {
                val data = repo.getMultipleCitiesWeather(Constants.TOP_INDIAN_CITIES)
                _weatherTiles.value = Resource.Success(data)
            } catch (e: Exception) {
                _weatherTiles.value = Resource.Error("Could not load city data: ${e.localizedMessage}")
            }
        }
    }

    fun search(query: String) {
        viewModelScope.launch {
            _singleResult.value = Resource.Loading
            try {
                val response = repo.getCurrentWeather(query)
                _singleResult.value = Resource.Success(
                    WeatherTileData(
                        city = response.location.name,
                        tempC = response.current.temp_c.toInt(),
                        iconUrl = response.current.condition.icon,
                        condition = response.current.condition.text,
                        time = response.location.localtime.substringAfter(" ")
                    )
                )
            } catch (e: Exception) {
                _singleResult.value = Resource.Error("No result found.")
            }
        }
    }
}
