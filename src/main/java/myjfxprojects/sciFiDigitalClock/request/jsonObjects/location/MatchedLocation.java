package myjfxprojects.sciFiDigitalClock.request.jsonObjects.location;

public class MatchedLocation {

    private String name;
    private LocaleNames local_names;
    private float lat;
    private float lon;
    private String country;
    private String state;

    public MatchedLocation() {}

    public String getName() {return name;}

    public LocaleNames getLocal_names() {return local_names;}

    public float getLat() {return lat;}

    public float getLon() {return lon;}

    public String getCountry() {return country;}

    public String getState() {return state;}

    @Override
    public String toString() {
        return "MatchedLocation{" +
                " name = " + name +
                ", local_names = " + local_names +
                ", lat = " + lat +
                ", lon = " + lon +
                ", country = " + country +
                ", state = " + state +
                '}';
    }
}
