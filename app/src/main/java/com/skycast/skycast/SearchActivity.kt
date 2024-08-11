package com.skycast.skycast

import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import com.skycast.skycast.databinding.ActivitySearchBinding

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SearchActivity : AppCompatActivity() {

    // Enabling the Activity Binding
    private val binding: ActivitySearchBinding by lazy {
        ActivitySearchBinding.inflate(layoutInflater)
    }

    interface ApiSuggestionsInterface {
        @GET("endpoint_for_city_suggestions")
        fun getCitySuggestions(): Call<List<String>>
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        //hide the action bar
        supportActionBar?.hide()
        // Make the activity full-screen
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )


        searchCity()
    }

    private fun searchCity() {
        val searchBar = binding.searchBar

        searchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // Handle the query when the user submits it
                if (!query.isNullOrBlank()) {
                    val capitalizedQuery = query.capitalize(Locale.ROOT)
                    fetchWeatherData(capitalizedQuery)
                }

                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Handle the query text as it changes
                // newText contains the current query text
                newText?.let { query ->
                    val capitalizedQuery = query.capitalize(Locale.ROOT)
                    fetchWeatherData(capitalizedQuery)
                }
                return true
            }
        })
    }



    private fun fetchWeatherData(CityName:String) {

        LocationFile.getCoordinatesByName(applicationContext,CityName)


        val retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .build()
            .create(ApiInterface::class.java)

        val response = retrofit.getWeatherDataByCoordinates("${LocationFile.getLatitude()}","${LocationFile.getLongitude()}","f61a79812e0a6f33d40b3f5aeef0171f","metric")
        response.enqueue(object : Callback<WeatherApp> {
            override fun onResponse(call: Call<WeatherApp>, response: Response<WeatherApp>) {
                val responseBody = response.body()
                if (response.isSuccessful && responseBody != null) {
                    val temperature = responseBody.main.temp.toString().trim()
                    Log.d("TAG", "Temperature: $temperature")
                    binding.temprature.text = "$temperature°C"

                    val condition = responseBody.weather.firstOrNull()?.main ?: "Unknown"
                    binding.condition.text = condition
                    binding.weatherCondition.text = condition

                    val humidity = responseBody.main.humidity
                    binding.humidityTxt.text = "$humidity%"

                    val maxTemp = responseBody.main.temp_max
                    val minTemp = responseBody.main.temp_min

                    if (maxTemp != minTemp) {
                        binding.maximumTemp.text = "Max.Temp: $maxTemp°C"
                        binding.minimumTemp.text = "Min.Temp: $minTemp°C"
                    } else {
                        // Handle the case where maxTemp and minTemp are the same
                        binding.maximumTemp.text = "Max.Temp: $maxTemp°C"
                        binding.minimumTemp.text = "Error!!" // Or provide a default value or message
                    }

                    val windSpeed = responseBody.wind.speed
                    binding.windSpeed.text = "$windSpeed m/s"

                    val sunRise = responseBody.sys.sunrise * 1000L // Convert seconds to milliseconds
                    binding.sunRiseTime.text = formatTime(sunRise)

                    val sunSet = responseBody.sys.sunset * 1000L // Convert seconds to milliseconds
                    binding.sunSetTime.text = formatTime(sunSet)

                    val sea = responseBody.main.pressure
                    binding.seaTxt.text = "$sea hPa"


                    binding.currentLocation.text="$CityName"

                    binding.dayName.text = dayName(System.currentTimeMillis())
                    binding.date.text =datedDate(System.currentTimeMillis())

                    updateWeatherAnimation(condition)
                }
            }

            override fun onFailure(call: Call<WeatherApp>, t: Throwable) {
                Log.e("TAG", "Error fetching weather data", t)
            }
        })
    }

    private fun formatTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    private fun updateWeatherAnimation(weatherCondition: String) {
        val weatherAnimationView = findViewById<LottieAnimationView>(R.id.lottieAnimationView)

        when (weatherCondition) {

            "Foggy","Mist","Haze","Cloudy","Clouds","Overcast","Partly Clouds" -> {
                weatherAnimationView.setAnimation(R.raw.cloudy_night)
                binding.root.setBackgroundResource(R.drawable.cloud_background)
            }

            "Smoke" -> {
                weatherAnimationView.setAnimation(R.raw.smoke)
                binding.root.setBackgroundResource(R.drawable.smoke_background)
            }


            "Light Rain", "Drizzle", "Moderate Rain","Shower", "Heavy Rain","Rain" -> {
                weatherAnimationView.setAnimation(R.raw.rainy)
                binding.root.setBackgroundResource(R.drawable.rain_background)
            }

            "Sunny","Clear Sky","Clear" -> {
                weatherAnimationView.setAnimation(R.raw.sunny)
                binding.root.setBackgroundResource(R.drawable.sunshine)
            }

            "Light snow","Moderate Snow", "Heavy snow","Blizzard"->{
                weatherAnimationView.setAnimation(R.raw.snow)
                binding.root.setBackgroundResource(R.drawable.snow_background)
            }


            "Storm" ->{
                weatherAnimationView.setAnimation(R.raw.storm)
                binding.root.setBackgroundResource(R.drawable.storm_bg)
            }

            "Windy" -> {
                weatherAnimationView.setAnimation(R.raw.sunny)
                binding.root.setBackgroundResource(R.drawable.fields)
            }

            else -> weatherAnimationView.setAnimation(R.raw.sunny)
        }

        weatherAnimationView.playAnimation()
    }

    private fun datedSunTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("hh:mm:ss a", Locale.getDefault())
        return sdf.format(timestamp)
    }

    private fun datedDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd-MMMM-yyyy", Locale.getDefault())
        return sdf.format((Date()))
    }

    private fun dayName(timestamp:Long): String {
        val sdf = SimpleDateFormat("EEEE", Locale.getDefault())
        return sdf.format((Date()))
    }
}