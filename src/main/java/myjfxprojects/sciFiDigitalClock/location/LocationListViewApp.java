/**
 * 
 */
package myjfxprojects.sciFiDigitalClock.location;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import myjfxprojects.sciFiDigitalClock.common.DataBean;
import myjfxprojects.sciFiDigitalClock.common.FxmlUtil;
import myjfxprojects.sciFiDigitalClock.request.jsonObjects.location.MatchedLocation;

/**
 * @author Christian
 *
 * Class the show new stage for the list view with possible locations.
 */
public class LocationListViewApp {
	
	// Stage for location list view
	private Stage listViewStage = null;
	
	// root layout container
	private Parent root = null;
	
	/**
	 * Constructor load the responsible controller class
	 * and initialize necessary conditions to show and close this stage.
	 */
	public LocationListViewApp() {
		// initialize own FXML loader
		FxmlUtil fxmlUtil = new FxmlUtil();
		
		// load FXML file to root layout container with self-made
    	// FxmlUtil class 
    	this.root = fxmlUtil.loadFxmlFile("/fxml/LocationListView.fxml");
		
    	if(this.root != null) {
    		
    		this.listViewStage = new Stage();
    		
    		// save list view stage in DataBean.getInstance(
    		DataBean.getInstance().setListViewStage(this.listViewStage);
    		
    		Scene scene = new Scene(this.root);
    		
    		// load specific Style sheet file
    		scene.getStylesheets().add(this.getClass().getResource("/css/locationListView.css").toExternalForm());
    		
    		this.listViewStage.setScene(scene);
    		
    		// set the undecorated stage -> this means without frame
    		this.listViewStage.initStyle(StageStyle.TRANSPARENT);
        	scene.setFill(Color.TRANSPARENT);
        	
        	// set icon for dock and main window
        	this.listViewStage.getIcons().add(new Image("images/clock.png"));
        	// set window name
        	this.listViewStage.setTitle("Add location");
        	
        	// add event filter to handle close event for this stage
        	// IMPORTANT: to enable btn search, btn cross and text field if this stage
        	// will be closed in other way as to use the close button
        	this.listViewStage.addEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST, event -> {
				DataBean.getInstance().getDigitalClockFXMLcontroller().getBtnSearchTextfield().setDisable(false);
        		DataBean.getInstance().getDigitalClockFXMLcontroller().getBtnCrossTextfield().setDisable(false);
        		DataBean.getInstance().getDigitalClockFXMLcontroller().getTxtFieldLocation().setDisable(false);
        	});
        	
        	// Add ChangeListener for the list view items -> to get the selected item by user
        	DataBean.getInstance().getLocationListViewController().getListViewOfLocations().
        		getSelectionModel().selectedItemProperty().addListener(new ListViewItemChangeListener());
        	
        	// Event filter to ignore key pressed events on the complete stage
        	this.listViewStage.addEventFilter(KeyEvent.ANY, Event::consume);
        	
        	// bind the disabled property of the arrow button to minimize/maximize VBox middle with
        	// the showing property of the list view stage -> user can not minimize the VBox if list view stage is showing
        	DataBean.getInstance().getDigitalClockFXMLcontroller().getBtnResizeVboxMiddle().disableProperty()
        			.bind(DataBean.getInstance().getListViewStage().showingProperty());
    	}
	}
	
	/**
	 * Show this stage and set the position under the main window.
	 */
	public void show() {
		// set this stage under the primary stage
		this.listViewStage.getScene().getWindow().setX(DataBean.current_X_Pos_MainWindow);
		this.listViewStage.getScene().getWindow().setY(DataBean.current_Y_Pos_MainWindow + DataBean.getInstance().getPrimaryStage().getHeight());
		
    	this.listViewStage.show();       	
    	this.listViewStage.setResizable(false);
    	
    	// disable btn search, btn cross and text field to be sure user choose a new location or close
    	// the list view stage
    	if((this.listViewStage != null) && (this.listViewStage.isShowing())) {
    		
    		DataBean.getInstance().getDigitalClockFXMLcontroller().getBtnSearchTextfield().setDisable(true);
    		DataBean.getInstance().getDigitalClockFXMLcontroller().getBtnCrossTextfield().setDisable(true);
    		DataBean.getInstance().getDigitalClockFXMLcontroller().getTxtFieldLocation().setDisable(true);
    	}
	}
	
	/**
	 * Close this stage and enable btn search, btn cross and the text field.
	 */
	public void close() {
		
		this.listViewStage.close();
		
		if((this.listViewStage != null) && !(this.listViewStage.isShowing())) {
    		
    		DataBean.getInstance().getDigitalClockFXMLcontroller().getBtnSearchTextfield().setDisable(false);
    		DataBean.getInstance().getDigitalClockFXMLcontroller().getBtnCrossTextfield().setDisable(false);
    		DataBean.getInstance().getDigitalClockFXMLcontroller().getTxtFieldLocation().setDisable(false);
    	}
	}
	
	// Event handling on each list view items
	//
	static class ListViewItemChangeListener implements ChangeListener<String> {

		@Override
		public void changed(ObservableValue<? extends String> observable, String oldLocation, String newLocation) {

			// if user select a new location from list view
			// get the assigned Location Data -> handled in class LocationDataHandling
			MatchedLocation newSelectedLocation = LocationDataHandling.getInstance()
					.getCompleteLocationData().get(newLocation);

			if(newSelectedLocation != null) {
				// first:	create new LocationObject with all necessary properties
				LocationObject locationObject = LocationObjectBuilder.getInstance()
						.withFullLocationName(newLocation)
						.withReducedLocationName(HandleLocationName.reduceLocationName(newLocation))
						.withLatitude(Float.toString(newSelectedLocation.getLat()))
						.withLongitude(Float.toString(newSelectedLocation.getLon()))
						.build();

				// second:	save the new Location in database and mark them as currently active location
				DataBean.getInstance().getJooqDbApi().createNewLocation(locationObject, true);

				// third:	mark the current selected location item in database to find out at the next application start
				//			which location was selected at last by user
				//			At first set all location items to false (isLastSelected Column)
				DataBean.getInstance().getJooqDbApi().setAllLocationsAsNotLastSelected();
				//			Then mark the current location item as "isCurrentActive" in database
				DataBean.getInstance().getJooqDbApi().setThisLocationAsLastSelected(locationObject);

				// fourth:	store the new location object in SET
				DataBean.getInstance().getUserLocationsSet().add(locationObject);

				// fifth:	add new location object to ComboBox if this is not present!!!
				if(! DataBean.getInstance().getDigitalClockFXMLcontroller().getComboBoxLocations().getItems().contains(locationObject)) {
					DataBean.getInstance().getDigitalClockFXMLcontroller().getComboBoxLocations().getItems().add(locationObject);
				}

				// sixth:	add the new selected location object to combo box
				//			IMPORTANT: with this step the ChangeListener from Combo Box will trigger and start fetching weather data for the new location!!!
				DataBean.getInstance().getDigitalClockFXMLcontroller().getComboBoxLocations().valueProperty().set(locationObject);

				// seventh:	close location list view, hide text field with buttons and make combo box visible again
				DataBean.getInstance().getLocationListViewApp().close();
				DataBean.getInstance().getDigitalClockFXMLcontroller().getTxtFieldLocation().setVisible(false);
				DataBean.getInstance().getDigitalClockFXMLcontroller().getTxtfieldButtons().setVisible(false);

				if(! DataBean.getInstance().getDigitalClockFXMLcontroller().getComboBoxLocations().isVisible()) {
					DataBean.getInstance().getDigitalClockFXMLcontroller().getComboBoxLocations().setVisible(true);
				}
			}
		}	
	}
}
