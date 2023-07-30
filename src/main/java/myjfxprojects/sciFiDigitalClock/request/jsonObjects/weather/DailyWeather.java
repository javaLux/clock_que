package myjfxprojects.sciFiDigitalClock.request.jsonObjects.weather;

import myjfxprojects.sciFiDigitalClock.request.jsonObjects.weather.DailyTemp;

public class DailyWeather {

    private long dt;
    private long sunrise;
    private long sunset;
    private DailyTemp temp;

    public DailyWeather() {}

    public long getDt() {return dt;}

    public long getSunrise() {return sunrise;}

    public long getSunset() {return sunset;}

    public DailyTemp getTemp() {return temp;}

    @Override
    public String toString() {
        return "DailyWeather{" +
                " dt = " + dt +
                ", sunrise = " + sunrise +
                ", sunset = " + sunset +
                ", temp = " + temp +
                '}';
    }
}
