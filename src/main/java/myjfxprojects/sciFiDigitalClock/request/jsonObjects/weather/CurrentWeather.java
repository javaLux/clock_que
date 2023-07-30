package myjfxprojects.sciFiDigitalClock.request.jsonObjects.weather;

import java.util.List;

public class CurrentWeather {

    private long dt;
    private long sunrise;
    private long sunset;
    private float temp;
    private final List<CurrentWeatherDetails> weather = null;


    public CurrentWeather() {}

    public long getDt() {return dt;}

    public long getSunrise() {return sunrise;}

    public long getSunset() {return sunset;}

    public float getTemp() {return temp;}

    public List<CurrentWeatherDetails> getWeather() {return weather;}

    @Override
    public String toString() {
        return "CurrentWeather{" +
                " dt = " + dt +
                ", sunrise = " + sunrise +
                ", sunset = " + sunset +
                ", temp = " + temp +
                ", weather = " + weather +
                '}';
    }
}
