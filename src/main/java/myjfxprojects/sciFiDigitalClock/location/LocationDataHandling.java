package myjfxprojects.sciFiDigitalClock.location;

import com.google.gson.Gson;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import myjfxprojects.sciFiDigitalClock.common.ApplicationLogger;
import myjfxprojects.sciFiDigitalClock.common.DataBean;
import myjfxprojects.sciFiDigitalClock.customTooltip.NoResultTooltip;
import myjfxprojects.sciFiDigitalClock.request.LocationData;
import myjfxprojects.sciFiDigitalClock.request.jsonObjects.location.LocaleNames;
import myjfxprojects.sciFiDigitalClock.request.jsonObjects.location.MatchedLocation;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.*;

public class LocationDataHandling {

    // logger instance
    private final static Logger LOGGER = ApplicationLogger.getAppLogger();

    private static LocationDataHandling instance;

    // simple boolean property to managed showing of the noResultTooltip
    private SimpleBooleanProperty noResultBoolean = new SimpleBooleanProperty(false);

    private Map<String, MatchedLocation> completeLocationData = new HashMap<>();

    /**
     * Private constructor initialize the change listener for the boolean property to monitor,
     * if the noResultTooltip must be showing or not.
     */
    private LocationDataHandling() {

        this.noResultBoolean.addListener((observable, oldValue, newValue) -> {
            // if new value set to true -> show noResultTooltip
            if(newValue) {
                NoResultTooltip.getInstance().showNoResultTooltip();
            }
        });
    }

    /**
     * GETTER for a Thread-Safe Singleton of this class
     * @return	->	LocationDataHandling instance
     */
    public static LocationDataHandling getInstance() {
        if(instance == null) {
            synchronized (LocationDataHandling.class) {
                if(instance == null) {
                    instance = new LocationDataHandling();
                }
            }
        }

        return instance;
    }

    public void searchForLocation(String searchedLocation) {

        Gson gson = new Gson();

        // set always the noResultTooltip, on false because if user search new location the tool tip have to hide
        this.noResultBoolean.set(false);

        // before new API call will be executed, clear old location data
        this.completeLocationData.clear();

        try {
            String jsonResponse = LocationData.getMatchedLocation(searchedLocation);
            MatchedLocation[] matchedLocations = gson.fromJson(jsonResponse, MatchedLocation[].class);

            if(matchedLocations != null) {
                // if matched locations found
                if(matchedLocations.length > 0) {

                    Arrays.stream(matchedLocations).toList().forEach(matchedLocation -> {
                        System.out.println(matchedLocation);

                        String locationDisplayName = this.buildListViewDisplayName(matchedLocation);

                        // add display name and location data to map
                        this.completeLocationData.put(locationDisplayName, matchedLocation);
                    });

                    // build observable list for the list view UI and put all location display names in
                    ObservableList<String> locationDisplayNames = FXCollections.observableArrayList();

                    locationDisplayNames.addAll(this.completeLocationData.keySet());

                    // add display names to list view
                    DataBean.getInstance().getLocationListViewController().getListViewOfLocations().setItems(locationDisplayNames);
                    // show the location list view stage with possible locations
                    DataBean.getInstance().getLocationListViewApp().show();

                } else {
                    // show NoResultTooltip
                    this.noResultBoolean.set(true);
                }
            }

        } catch (IOException ex) {
            LOGGER.error("Can not get geo data for: ' " + searchedLocation + " '\n" + "Complete URL: " +
                    DataBean.getInstance().getAPI_LOCATION_URL(searchedLocation), ex);
        }
    }

    /**
     * Method to build a readable location item for the ListView.
     * E.g. New York (New York) [Vereinigte Staaten]
     * @param matchedLocation   ->  The Location object with required info's
     * @return  ->  String The readable location name Format: City name (State) [Country]
     */
    private String buildListViewDisplayName(MatchedLocation matchedLocation) {

        String cityName = "";
        String stateName = "";
        String countryName = "";
        LocaleNames localeNames = matchedLocation.getLocal_names();

        // handle city name
        if (localeNames != null) {
            cityName = localeNames.getDe() != null ? localeNames.getDe() : matchedLocation.getName();
        } else {
            cityName = matchedLocation.getName();
        }

        stateName = matchedLocation.getState() != null ? matchedLocation.getState() : "";

        // get common country code
        String countryCode = matchedLocation.getCountry() != null ? matchedLocation.getCountry() : "";

        // convert country code to readable country name
        // can no country code resolved to a country name -> use country code instead
        countryName = DataBean.getInstance().getCountryMap().get(countryCode) != null
                ? DataBean.getInstance().getCountryMap().get(countryCode)
                : countryCode;

        if(stateName.isEmpty()) {
            return String.format("%s [%s]", cityName, countryName);
        }

        return String.format("%s (%s) [%s]", cityName, stateName, countryName);
    }

    public Map<String, MatchedLocation> getCompleteLocationData() {return completeLocationData;}
}
