package myjfxprojects.sciFiDigitalClock.request.jsonObjects.weather;

public class CurrentWeatherDetails {

    private int id;
    private String description;

    public CurrentWeatherDetails() {}

    public int getId() {return id;}

    public String getDescription() {return description;}

    @Override
    public String toString() {
        return "CurrentWeatherDetails{" +
                " id = " + id +
                ", description = " + description +
                '}';
    }
}
