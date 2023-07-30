package myjfxprojects.sciFiDigitalClock.request;

import myjfxprojects.sciFiDigitalClock.common.DataBean;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Pattern;

public class LocationData {

    // define bad url characters !*'();:@&=+$,/?#[]
    // these characters not allowed for city or location names, because these are reserved URL characters
    private static final Pattern badUrlCharPattern = Pattern.compile("[!\\*'\\(\\);:@&=\\+\\$,\\/\\?#\\[\\]]+");

    private LocationData() {}

    /**
     * Method search with regular expression in the possible location list for the given location
     * from user input to the text field.
     *
     * @param locationToFind	->	String location which will be search
     * @return  ->  String  The matched location(s) as JSON
     */
    public static String getMatchedLocation(String locationToFind) throws IOException {

        StringBuffer jsonResponseString = new StringBuffer();

        // Start searching for new location ONLY if text field not empty and Internet connection is UP
        if((locationToFind != null) && !(locationToFind.isEmpty()) && (DataBean.isInternetUp.get())) {

            // filtering on bad characters in given location and replace them with an empty string
            String filteredBadCharacters = locationToFind.replaceAll(badUrlCharPattern.toString(), "");

            // only if the sting not is empty, after filtering bad characters from user input call API
            if(! filteredBadCharacters.isEmpty()) {

                String apiUrl = DataBean.getInstance().getAPI_LOCATION_URL(filteredBadCharacters);

                URL url = new URL(apiUrl);
                // connect to GeoCoding API
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                try(BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    // temp container for reading data
                    String line = "";
                    // read response line by line
                    while((line = reader.readLine()) != null) {
                        jsonResponseString.append(line);
                    }
                }
            }
        }

        return jsonResponseString.toString();
    }
}
