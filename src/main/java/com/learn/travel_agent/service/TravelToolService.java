package com.learn.travel_agent.service;

import com.google.adk.tools.Annotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

//@Service
public class TravelToolService {

//    private static final Logger logger = LoggerFactory.getLogger(TravelToolService.class);
//
//    public static Map<String, Object> getWeather(
//
//            @Annotations.Schema(name = "city", description = "The name of the city (e.g., 'New York', 'Los Angeles', 'Kolkata', 'London')")
//            String city) {
//
//        logger.info("Getting weather for {}---", city);
//        String cityNormalized = city.toLowerCase().replace(" ", "");
//
//        // Mock weather data
//        Map<String, Map<String, Object>> mockWeatherDb = new HashMap<>();
//        mockWeatherDb.put("newyork", Map.<String, Object>of(
//                "status", "success",
//                "report", "The weather in New York is sunny with a temperature of 25°C."
//        ));
//        mockWeatherDb.put("london", Map.<String, Object>of(
//                "status", "success",
//                "report", "It's cloudy in London with a temperature of 15°C."
//        ));
//        mockWeatherDb.put("tokyo", Map.<String, Object>of(
//                "status", "success",
//                "report", "Tokyo is experiencing light rain and a temperature of 18°C."
//        ));
//        mockWeatherDb.put("paris", Map.<String, Object>of(
//                "status", "success",
//                "report", "The weather in Paris is sunny with a temperature of 22°C."
//        ));
//
//        Map<String, Object> result;
//        if (mockWeatherDb.containsKey(cityNormalized)) {
//            result = new HashMap<>(mockWeatherDb.get(cityNormalized));
//        } else {
//            Map<String, Object> error = new HashMap<>();
//            error.put("status", "error");
//            error.put("error_message", "Sorry, I don't have weather information for '" + city + "'.");
//            result = error;
//        }
//        logger.info("Returning weather for {}: {}", city, result);
//        return result;
//    }
//
//    public static Map<String, Object> getCurrentTime(
//            @Annotations.Schema(name = "city", description = "The name of the city for which to retrieve the current time")
//            String city) {
//
//        logger.info("Getting time for {}---", city);
//        String tzIdentifier;
//
//        if (city.equalsIgnoreCase("new york")) {
//            tzIdentifier = "America/New_York";
//        } else if (city.equalsIgnoreCase("london")) {
//            tzIdentifier = "Europe/London";
//        } else if (city.equalsIgnoreCase("tokyo")) {
//            tzIdentifier = "Asia/Tokyo";
//        } else if (city.equalsIgnoreCase("paris")) {
//            tzIdentifier = "Europe/Paris";
//        } else {
//            Map<String, Object> error = new HashMap<>();
//            error.put("status", "error");
//            error.put("error_message", "Sorry, I don't have timezone information for " + city + ".");
//            logger.warn("Timezone not found for {}", city);
//            return error;
//        }
//
//        ZoneId tz = ZoneId.of(tzIdentifier);
//        ZonedDateTime now = ZonedDateTime.now(tz);
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");
//        String report = "The current time in " + city + " is " + now.format(formatter);
//
//        Map<String, Object> success = new HashMap<>();
//        success.put("status", "success");
//        success.put("report", report);
//        logger.info("Returning time for {}: {}", city, success);
//        return success;
//    }

}