package myjfxprojects.sciFiDigitalClock.business;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.image.Image;
import javafx.util.Duration;
import myjfxprojects.sciFiDigitalClock.common.ApplicationLogger;
import myjfxprojects.sciFiDigitalClock.common.DataBean;
import myjfxprojects.sciFiDigitalClock.common.OwnDateTimeFormatter;
import myjfxprojects.sciFiDigitalClock.customTooltip.CustomTempTooltip;
import myjfxprojects.sciFiDigitalClock.customTooltip.CustomWeatherToolTip;
import myjfxprojects.sciFiDigitalClock.location.LocationObject;
import myjfxprojects.sciFiDigitalClock.request.WeatherData;
import myjfxprojects.sciFiDigitalClock.request.jsonObjects.weather.CommonWeather;
import myjfxprojects.sciFiDigitalClock.weatherConditions.WeatherCondition;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class WeatherDataHandling {

	// thread safe instance field
	private static WeatherDataHandling instance = null;

	// logger instance
	private final static Logger LOGGER = ApplicationLogger.getAppLogger();

	// thread safe instance of data bean
	private DataBean dataBean = DataBean.getInstance();

	// thread safe instance of weather conditions class
	private WeatherCondition weatherCondition = null;

	// Timeline to fetch periodically weather data
	private Timeline timelineWeatherData = null;

	// weather data currently not available
	private final String dataNotAvailableText = "No weather data currently available.\nTry again later.";


	// customize tool tips
	private CustomWeatherToolTip weatherIconToolTip = null;
	private CustomTempTooltip tempIconToolTip = null;

	/**
	 * private Constructor initialize members
	 */
	private WeatherDataHandling() {

		this.weatherCondition = WeatherCondition.getInstance();
		this.timelineWeatherData = new Timeline();
		this.weatherIconToolTip = new CustomWeatherToolTip();
		this.tempIconToolTip = new CustomTempTooltip();

		// Assign custom tool tips to nodes from FXML
		this.dataBean.getDigitalClockFXMLcontroller().getLblHelperTooltip().setTooltip(this.weatherIconToolTip);
		this.dataBean.getDigitalClockFXMLcontroller().getLblCurrentTemp().setTooltip(this.tempIconToolTip);

		/*
		 * add ChangeListener to the BooleanProperty value which says if Internet
		 * connection is up or down if this value changed from false to true -> than
		 * start once a new timeline to fetch weather data again IMPORTANT because if
		 * the application started only the periodically, timeline and these is fetching
		 * the weather data every minute -> this means the weather data can not updated
		 * one minute long
		 */
		DataBean.isInternetUp.addListener((observable, oldValue, newValue) -> {

			if (! oldValue) {

				// the default value from variable "isInternetUp" is false, IMPORTANT if
				// Application starts and the scheduled service to check the
				// Internet connectivity will execute -> than will this value changed to true if
				// Internet is available and fetching weather data begins or
				// value stays at false -> fetching weather data will not execute and so on
				this.startOnceTimelineToFetchWeatherData();
			}
		});
	}

	/**
	 * GETTER for a Thread-Safe Singleton of this class
	 * 
	 * @return	Instance of Weather
	 */
	public static WeatherDataHandling getInstance() {
		if (instance == null) {
			synchronized (WeatherDataHandling.class) {
				if (instance == null) {
					instance = new WeatherDataHandling();
				}
			}
		}

		return instance;
	}

	/**
	 * Method starts a timeline who periodically fetch the current weather data
	 * (dependent on the duration value).
	 */
	public void startPeriodicallyFetchWeatherData() {
		this.timelineWeatherData.setCycleCount(Timeline.INDEFINITE);

		// define weather data timeline duration to four minutes
		// IMPORTANT: because only 1000 API calls per day are allowed
		double durationWeatherTimeline = 4.0;
		this.timelineWeatherData.getKeyFrames()
				.add(new KeyFrame(Duration.minutes(durationWeatherTimeline), event -> {

					this.handleWeatherData();

				}));

		this.timelineWeatherData.play();
	}

	/**
	 * Method execute a new timeline once to fetch weather data.
	 */
	public void startOnceTimelineToFetchWeatherData() {
		Timeline timeline = new Timeline();
		// timeline executes once
		timeline.setCycleCount(1);
		timeline.getKeyFrames().add(new KeyFrame(Duration.millis(100), event -> {

			this.handleWeatherData();

		}));

		timeline.play();
	}

	/**
	 * Method fetch weather data once and save the current time zone for the given
	 * location object in database. Farther the tool tip from digital clock will be
	 * updated with new time zone info and the date info's will be updated. Last but
	 * not least, the current time shift in hours will be add to the digital clock
	 * tool tip.
	 * 
	 * @param location -> [LocationObject] the current location
	 */
	public void fetchWeatherDataAndSetTimeZone(LocationObject location) {
		Timeline timeline = new Timeline();
		// timeline executes only one time
		timeline.setCycleCount(1);
		timeline.getKeyFrames().add(new KeyFrame(Duration.millis(100), event -> {

			this.handleWeatherData();

		}));

		// event handler for the timeline finished event
		timeline.setOnFinished(event -> {
			// if timeline finished, then save time zone for the given location object in
			// database
			dataBean.getJooqDbApi().setTimeZoneForThisLocation(location, DataBean.currentLocationTimeZone);
			// set new date info's dependent on the current active time zone
			dataBean.getDigitalClockFXMLcontroller()
					.setLblWeekDay(ControlDateInfos.getInstance().getCurrentDayOfWeek());
			dataBean.getDigitalClockFXMLcontroller().setLblMonth(ControlDateInfos.getInstance().getCurrentMonth());
			dataBean.getDigitalClockFXMLcontroller()
					.setLblMonthDay(ControlDateInfos.getInstance().getCurrentDayOfMonth());

			// assigned the current time zone to the right label in the custom tool tip of
			// the digital clock
			dataBean.getTimeZoneTooltip().setTimeZoneName(DataBean.currentLocationTimeZone);

			// assigned the current time shift value to the right label in the custom tool
			// tip of the digital clock
			dataBean.getTimeZoneTooltip().setTimeShiftValue(DigitalClock.getInstance().getCurrentTimeShiftInHours());
		});

		timeline.play();
	}

	/**
	 * Method handle the current received weather data.
	 */
	private void handleWeatherData() {

		Gson gson = new Gson();
		String weatherDataAsJson;
		CommonWeather commonWeatherData;

		// fields for weather data
		String currentTemp;
		String lowestDailyTemp;
		String highestDailyTemp;
		String currentSunrise;
		String currentSunset;
		String weatherDescription;
		String weatherConIconPath;

		try {
			// execute the API call to fetch weather data
			weatherDataAsJson = WeatherData.getWeatherData();

		} catch (IOException ex) {
			LOGGER.error("Failed to established a connection to:\n'" + this.dataBean.getAPI_WEATHER_URL() + "'\n", ex);
			this.fetchingWeatherDataFails();

			return;
		}

		try {
			// build common weather data object from JSON string
			commonWeatherData = gson.fromJson(weatherDataAsJson, CommonWeather.class);

		} catch (JsonSyntaxException ex) {

			LOGGER.error("Failed to extract weather data from JSON:\n", ex);
			this.fetchingWeatherDataFails();

			return;
		}

		// handle with the weather data only if these also available
		if(commonWeatherData.getCurrent() != null && commonWeatherData.getDaily() != null) {

			// boolean to handle the icon's or the case if the no internet available or fetching weather
			// data fails
			DataBean.isWaitingSymbolShowing = false;
			DataBean.isErrorSymbolShowing = false;

			// save periodically the required weather data
			DataBean.currentLocationTimeZone = commonWeatherData.getTimezone();
			// get rounded current temp
			int roundedCurrentTemp = this.roundTemp(commonWeatherData.getCurrent().getTemp());
			currentTemp = Integer.toString(roundedCurrentTemp);

			int roundedLowestDailyTemp = this.roundTemp(commonWeatherData.getDaily().get(0).getTemp().getMin());
			lowestDailyTemp = Integer.toString(roundedLowestDailyTemp);

			int roundedHighestDailyTemp = this.roundTemp(commonWeatherData.getDaily().get(0).getTemp().getMax());
			highestDailyTemp = Integer.toString(roundedHighestDailyTemp);

			// convert given sunrise date time value with the current timezone
			currentSunrise = OwnDateTimeFormatter.formatTime(
					TimeUnit.SECONDS.toMillis(commonWeatherData.getCurrent().getSunrise()),
					DataBean.currentLocationTimeZone);

			currentSunset = OwnDateTimeFormatter.formatTime(
					TimeUnit.SECONDS.toMillis(commonWeatherData.getCurrent().getSunset()),
					DataBean.currentLocationTimeZone);

			// save periodically current sunset and sunrise date time in dataBean
			DataBean.dateTimeSunrise = currentSunrise;
			DataBean.dateTimeSunset = currentSunset;

			// save periodically necessary UNIX time stamps in seconds
			DataBean.currentUnixTimestampInSeconds = commonWeatherData.getCurrent().getDt();
			DataBean.currentDaySunriseUnixTimestampInSeconds = commonWeatherData.getCurrent().getSunrise();
			DataBean.currentDaySunsetUnixTimestampInSeconds = commonWeatherData.getCurrent().getSunset();

			weatherDescription = commonWeatherData.getCurrent().getWeather().get(0).getDescription();

			// get matching weather icon
			weatherConIconPath = this.weatherCondition.getWeatherIcon(
					commonWeatherData.getCurrent().getWeather().get(0).getId());

			// set correct weather data in each assigned UI field
			this.dataBean.getDigitalClockFXMLcontroller().getLblCurrentTemp().setText(currentTemp);
			this.dataBean.getDigitalClockFXMLcontroller().getTextTempUnit()
					.setText(this.dataBean.getCurrentTempUnit().getTempUnit());

			// check if path to weather icon not empty
			if (! weatherConIconPath.isEmpty()) {

				// if weather icon valid -> than change image view with current weather icon
				this.dataBean.getDigitalClockFXMLcontroller()
						.setImageViewWeather(new Image(weatherConIconPath));
				// set tool tips for weather icon
				this.weatherIconToolTip.setWeatherText(weatherDescription, currentSunrise, currentSunset);

			} else {
				// if no path to icon available -> show waiting icon
				this.dataBean.getDigitalClockFXMLcontroller().setImageViewWeather(new Image("images/waiting.png"));
				this.weatherIconToolTip.setWaitingText(this.dataNotAvailableText);
			}

			// set values for current highest and lowest temperature tool tip
			this.tempIconToolTip.setMinMaxTempValue(lowestDailyTemp, highestDailyTemp);
		}
		else {
			// if no weather data available
			this.noWeatherDataAvailable();
			// make log entry
			LOGGER.warn("Current and daily weather data are not available.");
		}
	}

	/**
	 * Method hide the text elements for temperatureCurrent and temp unit
	 */
	private void hideWeatherData() {
		this.dataBean.getDigitalClockFXMLcontroller().getLblCurrentTemp().setVisible(false);
		this.dataBean.getDigitalClockFXMLcontroller().getTextTempUnit().setVisible(false);
	}

	/**
	 * Method rounds the given temperature to the closest integer.
	 * @param temp	->	The temperature as float
	 * @return	rounded temp as integer
	 */
	private int roundTemp(float temp) {
		return Math.round(temp);
	}

	/**
	 * Method will be used if no weather data at current query interval available.
	 * Shows the waiting Symbol instead of the weather icon and changed the tool
	 * tip.
	 */
	private void noWeatherDataAvailable() {

		DataBean.isWaitingSymbolShowing = true;
		// hide weather data in application and shows waiting symbol
		this.hideWeatherData();
		// hide progress indicator
		this.dataBean.getDigitalClockFXMLcontroller().getProgressIndicator().setVisible(false);

		// show imageView and label with tooltip
		this.dataBean.getDigitalClockFXMLcontroller().getImageViewWeather().setVisible(true);
		this.dataBean.getDigitalClockFXMLcontroller().getLblHelperTooltip().setVisible(true);
		// show waiting symbol instead of weather icon
		this.dataBean.getDigitalClockFXMLcontroller().setImageViewWeather(new Image("images/waiting.png"));
		this.weatherIconToolTip.setWaitingText(this.dataNotAvailableText);
	}

	/**
	 * Method is used when an exception will thrown by querying the current weather
	 * data from OpenWeather API. Shows the error Symbol instead of the weather icon
	 * and changed the tool tip.
	 */
	private void fetchingWeatherDataFails() {

		DataBean.isErrorSymbolShowing = true;
		// hide weather data in application and shows error symbol
		this.hideWeatherData();
		// hide progress indicator
		this.dataBean.getDigitalClockFXMLcontroller().getProgressIndicator().setVisible(false);

		// show imageView and label with tooltip
		this.dataBean.getDigitalClockFXMLcontroller().getImageViewWeather().setVisible(true);
		this.dataBean.getDigitalClockFXMLcontroller().getLblHelperTooltip().setVisible(true);
		// show waiting symbol instead of weather icon
		this.dataBean.getDigitalClockFXMLcontroller().setImageViewWeather(new Image("images/error.png"));
		// error by query the current weather data from server
		String errorByFetchingWeatherDataText = "Error getting current weather data.\nNo response from server.";
		this.weatherIconToolTip.setWaitingText(errorByFetchingWeatherDataText);
	}
}
