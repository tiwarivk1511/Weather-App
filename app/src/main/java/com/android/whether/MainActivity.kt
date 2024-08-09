package com.android.whether

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.airbnb.lottie.LottieAnimationView
import com.android.whether.databinding.ActivityMainBinding
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private val LOCATION_PERMISSION_REQUEST_CODE = 140
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        //hide the action statusbar
        supportActionBar?.hide()
        // Make the activity full-screen
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }else{
            getLocation()
        }
        getLocation()




        binding.searchBar.setOnClickListener {
            val intent = Intent(this, SearchActivity::class.java)
            startActivity(intent)

        }

        binding.developerBtn.setOnClickListener {
            val intent = Intent(this, DeveloperActivity::class.java)
            startActivity(intent)
        }
    }

    private fun getLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            val areaName = LocationFile.getCity(this)
            binding.currentLocation.text = areaName ?: "Unknown Location"



            if (areaName != null) {
                LocationFile.getCoordinatesByName(this, areaName)
                //get the coordinates
                val latitude = LocationFile.getLatitude().toDoubleOrNull() ?: 0.0
                val longitude = LocationFile.getLongitude().toDoubleOrNull() ?: 0.0

                fetchWeatherData(latitude, longitude)
            }

        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                getLocation()
            } else {
                Log.e("Permission", "Location permission denied")
            }
        }
    }



    private fun updateAqiStatus(aqiStatus: String) {
        runOnUiThread {
            //binding.aqiStatus.text = "AQI: $aqiStatus"
        }
    }

    private fun fetchWeatherData(latitude: Double, longitude: Double) {
        val retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .build()
            .create(ApiInterface::class.java)

        val response = retrofit.getWeatherDataByCoordinates(
            (latitude).toString(),
            (longitude).toString(),
            "f61a79812e0a6f33d40b3f5aeef0171f",
            "metric"
        )

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
                        binding.maximumTemp.text = "Max.Temp: $maxTemp°C"
                        binding.minimumTemp.text = "Error!!"
                    }

                    val windSpeed = responseBody.wind.speed
                    binding.windSpeed.text = "$windSpeed m/s"

                    val sunRise = responseBody.sys.sunrise * 1000L
                    binding.sunRiseTime.text = formatTime(sunRise)

                    val sunSet = responseBody.sys.sunset * 1000L
                    binding.sunSetTime.text = formatTime(sunSet)

                    val sea = responseBody.main.pressure
                    binding.seaTxt.text = "$sea hPa"

                    val areaName = LocationFile.getCity(this@MainActivity)
                    binding.currentLocation.text = areaName ?: "Unknown Location"

                    binding.dayName.text = dayName(System.currentTimeMillis())
                    binding.date.text = datedDate(System.currentTimeMillis())

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

    private fun datedDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd-MMMM-yyyy", Locale.getDefault())
        return sdf.format((Date()))
    }

    private fun dayName(timestamp: Long): String {
        val sdf = SimpleDateFormat("EEEE", Locale.getDefault())
        return sdf.format((Date()))
    }

    private fun showToast(message: String) {
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateWeatherAnimation(weatherCondition: String) {
        val weatherAnimationView = findViewById<LottieAnimationView>(R.id.lottieAnimationView)
        binding.currentLocation.text = LocationFile.getCity(this)
        when (weatherCondition) {
            "Foggy", "Mist", "Haze", "Cloudy", "Clouds", "Overcast", "Partly Clouds" -> {
                weatherAnimationView.setAnimation(R.raw.cloudy_night)
                binding.root.setBackgroundResource(R.drawable.cloud_background)
            }
            "Smoke" -> {
                weatherAnimationView.setAnimation(R.raw.smoke)
                binding.root.setBackgroundResource(R.drawable.smoke_background)
            }
            "Light Rain", "Drizzle", "Moderate Rain", "Shower", "Heavy Rain", "Rain" -> {
                weatherAnimationView.setAnimation(R.raw.rainy)
                binding.root.setBackgroundResource(R.drawable.rain_background)
            }
            "Sunny", "Clear Sky", "Clear" -> {
                weatherAnimationView.setAnimation(R.raw.sunny)
                binding.root.setBackgroundResource(R.drawable.sunshine)
            }
            "Light snow", "Moderate Snow", "Heavy snow", "Blizzard" -> {
                weatherAnimationView.setAnimation(R.raw.snow)
                binding.root.setBackgroundResource(R.drawable.snow_bg)
            }
            "Thunderstorm" -> {
                weatherAnimationView.setAnimation(R.raw.rainy)
                binding.root.setBackgroundResource(R.drawable.rain_background)
            }
            "Storm" -> {
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
}