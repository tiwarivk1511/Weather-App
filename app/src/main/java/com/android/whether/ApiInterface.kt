package com.android.whether

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiInterface {
    @GET("weather")
    fun getWeatherDataByCoordinates(
        @Query("lat") latitude: String,
        @Query("lon") longitude: String,
        @Query("appid") apiKey: String,
        @Query("units") units: String
    ): Call<WeatherApp>

    fun getLocationByCityName(cityName: String): Call<List<String>>
}