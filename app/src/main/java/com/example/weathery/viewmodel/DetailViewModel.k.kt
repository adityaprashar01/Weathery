package com.example.weathery.viewmodel

import androidx.lifecycle.*
import com.example.weathery.data.WeatherRepository
import com.example.weathery.model.WeatherResponse
import com.example.weathery.util.Resource
import kotlinx.coroutines.launch

class DetailViewModel : ViewModel() {
    private val repo = WeatherRepository()
    private val _weather = MutableLiveData<Resource<WeatherResponse>>()
    val weather: LiveData<Resource<WeatherResponse>> = _weather

    fun load(query: String) {
        viewModelScope.launch {
            _weather.value = Resource.Loading
            try {
                val data = repo.getCurrentWeather(query)
                _weather.value = Resource.Success(data)
            } catch (e: Exception) {
                _weather.value = Resource.Error(e.localizedMessage ?: "Unknown error")
            }
        }
    }
}
