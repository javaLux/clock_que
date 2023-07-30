package myjfxprojects.sciFiDigitalClock.request;

import myjfxprojects.sciFiDigitalClock.common.DataBean;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class WeatherData {

    private WeatherData() {}


    /**
     * Method fetch the current weather data from <a href="https://home.openweathermap.org/">Open-Weather-Map</a>
     * and returned the weather data for the specify location as JSON string.
     * @return  ->  String  The weather data as JSON
     */
    public static String getWeatherData() throws IOException {

        // StringBuffer to hold API response
        StringBuffer jsonResponseString = new StringBuffer();

        // START ONLY the weather API call if Internet is up
        if (DataBean.isInternetUp.get()) {

            URL url = new URL(DataBean.getInstance().getAPI_WEATHER_URL());

            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

            // store incoming weather data in BufferReader, read this data and
            // use try with resources to close stream automatically after using
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {

                // temp container for reading data
                String line = "";
                // read response line by line
                while ((line = reader.readLine()) != null) {
                    jsonResponseString.append(line);
                }
            }
        }

        return jsonResponseString.toString();
    }
}
