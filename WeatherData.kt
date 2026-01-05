package com.example.weather

data class WeatherData(
    val temperature: Int,      // °C
    val humidity: Int,         // %
    val pressure: Int,         // hPa
    val condition: String,     // weather
    val windSpeed: Double      // m/s
)

data class CityWeather(
    val cityName: String,
    val data: WeatherData
)

object PredefinedCities {
    // 8 total cities
    val cities = listOf(
        CityWeather("北京", WeatherData(22, 60, 1013, "晴", 3.2)),
        CityWeather("上海", WeatherData(25, 75, 1010, "多云", 2.8)),
        CityWeather("广州", WeatherData(28, 80, 1008, "雨", 1.5)),
        CityWeather("成都", WeatherData(20, 70, 995, "雾", 1.0)),
        CityWeather("深圳", WeatherData(27, 85, 1009, "雷暴", 4.1)),
        CityWeather("杭州", WeatherData(24, 68, 1011, "少云", 2.3)),
        CityWeather("西安", WeatherData(19, 55, 998, "风", 5.0)),
        CityWeather("重庆", WeatherData(26, 90, 1002, "阴天", 1.2))
    )

    // ✅ Must be ALL city names (8 items)
    val allCities = cities.map { it.cityName }.toSet()
}