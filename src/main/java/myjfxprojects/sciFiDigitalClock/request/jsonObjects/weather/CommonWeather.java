package myjfxprojects.sciFiDigitalClock.request.jsonObjects.weather;

import java.util.List;

/**
 * Class represents all necessary weather data.
 * This class will be used to extract required information from the JSON string.
 * The JSON string will be received from the Open Weather API, the request will be
 * handled here -> {@link  myjfxprojects.sciFiDigitalClock.request.WeatherData  data-request}
 */
public class CommonWeather {

    private float lat;
    private float lon;
    private String timezone;
    private CurrentWeather current;
    private final List<DailyWeather> daily = null;

    public CommonWeather() {}

    public float getLat() {return lat;}

    public float getLon() {return lon;}

    public String getTimezone() {return timezone;}

    public CurrentWeather getCurrent() {return current;}

    public List<DailyWeather> getDaily() {return daily;}

    @Override
    public String toString() {
        return "CommonWeather{" +
                " lat = " + lat +
                ", lon = " + lon +
                ", timezone = " + timezone +
                ", current = " + current +
                ", daily = " + daily +
                '}';
    }
}
