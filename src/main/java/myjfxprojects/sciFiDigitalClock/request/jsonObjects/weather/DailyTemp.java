package myjfxprojects.sciFiDigitalClock.request.jsonObjects.weather;

public class DailyTemp {

    private float min;
    private float max;

    public DailyTemp() {}

    public float getMin() {return min;}

    public float getMax() {return max;}

    @Override
    public String toString() {
        return "DailyTemp{" +
                " min = " + min +
                ", max = " + max +
                '}';
    }
}
