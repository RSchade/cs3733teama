package com.teama.controllers_refactor2;

import com.jfoenix.controls.*;
import com.teama.ProgramSettings;
import com.teama.controllers.NodeInfoPopUpController;
import com.teama.controllers.PathfindingController;
import com.teama.controllers.SearchBarController;
import com.teama.controllers_refactor.PopOutFactory;
import com.teama.controllers_refactor.PopOutType;
import com.teama.mapdrawingsubsystem.ClickedListener;
import com.teama.mapdrawingsubsystem.MapDisplay;
import com.teama.mapdrawingsubsystem.MapDrawingSubsystem;
import com.teama.mapsubsystem.MapSubsystem;
import com.teama.mapsubsystem.data.Floor;
import com.teama.mapsubsystem.data.Location;
import com.teama.mapsubsystem.data.MapNode;
import com.teama.translator.Translator;
import javafx.beans.Observable;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class MainMapScreen implements Initializable {

    @FXML
    private AnchorPane areaPane;

    @FXML
    private ScrollPane mapScroll;

    @FXML
    private Canvas mapCanvas;

    @FXML
    private JFXDrawer drawer;

    @FXML private Pane searchPane;

    @FXML
    private JFXComboBox<String> searchBar;

    @FXML
    private JFXHamburger hmbDrawerOpener;

    @FXML
    private ImageView searchButton;

    @FXML private ImageView loginButton;

    @FXML private ImageView translateButton;

    @FXML
    private JFXSlider zoomSlider;

    @FXML
    private GridPane floorButtonBox;

    @FXML
    private ImageView directionsButton;



    private MapDisplay map;
    private HamburgerController curController;
    private boolean drawerExtended = false;

    private double maxZoom = 3.0;
    private double minZoom = 1.1;

    private MapSubsystem mapSubsystem;

    private MapDrawingSubsystem mapDrawing;

    private PathfindingController pathfinding;
    private SimpleBooleanProperty isLoggedIn = new SimpleBooleanProperty(false);
    private SimpleBooleanProperty showLoginButton = new SimpleBooleanProperty(true);
    private EventHandler<MouseEvent> originalMouseClick;

    private PopOutFactory popOutFactory = new PopOutFactory();

    private long popUpID;

    // Contains all of the event handlers for the buttons on the sidebar
    // Useful for when we need to open something on the sidebar based on another event
    //get rid of this
    private Map<PopOutType, EventHandler<MouseEvent>> mainSidebarMap = new HashMap<>();


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        mapSubsystem = MapSubsystem.getInstance();
        pathfinding = new PathfindingController(mainSidebarMap);
        //set up buttonBox IDs
        for(int i=0; i<Floor.values().length; i++) {
            floorButtonBox.getChildren().get(i).setId(Floor.values()[floorButtonBox.getChildren().size()-1-i].toString());
        }
        mapDrawing = MapDrawingSubsystem.getInstance();
        //have the button box be the gridPane here
        System.out.println(mapScroll);
        mapDrawing.initialize(mapCanvas, mapScroll, floorButtonBox, areaPane);

        mapDrawing.setGrow(true);
        mapDrawing.setZoomFactor(1.7); // Initial zoom

        // Add a listener that displays the correct nodes for the floor
        mapDrawing.clearMap();
        for (MapNode n : mapSubsystem.getVisibleFloorNodes(mapDrawing.getCurrentFloor()).values()) {
            mapDrawing.drawNode(n, 0, null);
        }
        mapDrawing.attachFloorChangeListener((a, b, c) -> {
            mapDrawing.clearMap();
            for (MapNode n : mapSubsystem.getVisibleFloorNodes(mapDrawing.getCurrentFloor()).values()) {
                mapDrawing.drawNode(n, 0, null);
            }
        });
        // Make a pop up on the user's mouse cursor every time a node is clicked
        popUpID = mapDrawing.attachClickedListener(event -> generateNodePopUp(event), ClickedListener.LOCCLICKED);
        //will need to change this to create the child in the drawer and adjust the drawer

        // Pop up goes away on a floor switch
        mapDrawing.attachFloorChangeListener((a, b, c) -> removeCurrentPopUp());

        // Make the node pop up disappear every time the window is resized
        ChangeListener<Number> removePopUpWhenResized = (ObservableValue<? extends Number> obsVal, Number oldVal, Number newVal) -> removeCurrentPopUp();
        areaPane.heightProperty().addListener(removePopUpWhenResized);
        areaPane.widthProperty().addListener(removePopUpWhenResized);

        // Remove pop up on pan
        mapScroll.onDragDetectedProperty().set((event -> removeCurrentPopUp()));

        // Zoom in and out using plus and minus keys
        mapScroll.onKeyTypedProperty().set((KeyEvent event) -> {
            switch (event.getCharacter()) {
                case "=":
                    // zoom in
                    if (mapDrawing.getZoomFactor() < maxZoom) {
                        mapDrawing.setZoomFactor(mapDrawing.getZoomFactor() + 0.1);
                    }
                    break;
                case "-":
                    // zoom out
                    if (mapDrawing.getZoomFactor() > minZoom) {
                        mapDrawing.setZoomFactor(mapDrawing.getZoomFactor() - 0.1);
                    }
                    break;
            }
            // Remove the pop up
            removeCurrentPopUp();

            // make the zoom slider reflect the current zoom level
            zoomSlider.setValue(mapDrawing.getZoomFactor());
        });

        // Set the zoom slider max and min to the zoom max and min
        zoomSlider.setMin(minZoom);
        zoomSlider.setMax(maxZoom);
        // Set to default
        zoomSlider.setValue(mapDrawing.getZoomFactor());
        // When the zoom slider is moved, change the zoom factor on the screen
        zoomSlider.valueProperty().addListener((a, b, after) -> {
            mapDrawing.setZoomFactor(after.doubleValue());
            removeCurrentPopUp();
        });

        // Populate and create the search bar
        SearchBarController mainSearch = new SearchBarController(searchBar, false);

        // When the search button is pressed then generate a new path with that as the destination
        searchButton.pressedProperty().addListener((Observable a) -> {
            pathfinding.genPath(mainSearch.getSelectedNode());
        });

        // Have the search bar listen to the beginning of the path and update accordingly
        ProgramSettings.getInstance().getPathEndNodeProp().addListener((a) -> {
            searchBar.getEditor().setText(ProgramSettings.getInstance().getPathEndNodeProp().getValue().getLongDescription());
        });
        // Hide stuff until someone is logged in
        ProgramSettings.getInstance().getIsLoggedInProp().addListener((a) -> {
            System.out.println("logging in");
            setLoginVisibility();
        });
        setLoginVisibility();
    }

    private void setLoginVisibility() {
        if (ProgramSettings.getInstance().getIsLoggedInProp().get()) {
            System.out.println("here");
            //inserting animation here
            Image logOut = new Image(getClass().getResourceAsStream("/materialicons/mainscreenicons/LogOut.png"));
            loginButton.setImage(logOut);
        } else {
            System.out.println("I guess not");
            // mapEditorButton.setY(startPoint);
            Image logIn = new Image(getClass().getResourceAsStream("/materialicons/mainscreenicons/LogIn.png"));
            loginButton.setImage(logIn);
        }
    }


    private Parent nodeInfo;
    //TODO update this stuff to create and contain the search info
    @FXML public void onSearchClick(MouseEvent e){

    }
    //TODO update this to create and contain the pathfinding stuff
    @FXML public void onDirectionsClick(MouseEvent e){
        if(drawerExtended){
            //do nothing
        }
        else{
            System.out.println("We need to implement this");
        }
    }
    @FXML public void onOpenerClick(MouseEvent e){
        //TODO fix double click breaking this guy
        try {
            disableSearchPane();
            drawer.setVisible(true);
            FXMLLoader openerLoader = new FXMLLoader();
            curController = new AdminPaneController();
            System.out.println(getClass().getResource(curController.getFXMLPath()));
            openerLoader.setLocation(getClass().getResource(curController.getFXMLPath()));
            openerLoader.setController(curController);
            openerLoader.load();
            curController.getParentPane().prefHeightProperty().bind(drawer.heightProperty());
            curController.onOpen();
            System.out.println("in the main "+curController);
            System.out.println(curController.getParentPane().getPrefWidth());
            drawer.setDefaultDrawerSize(curController.getParentPane().getPrefWidth());
            drawer.setSidePane(curController.getParentPane());
            drawer.open();
            curController.getClosing().addListener((a, oldVal, newVal) -> {
                if (newVal) {
                    curController.onClose();
                    drawer.close();
                    drawer.setVisible(false);
                   enableSearchPane();
                }
            });
        }
        catch(IOException error){
            error.printStackTrace();
        }
    }
    private void enableSearchPane(){
       hmbDrawerOpener.setDisable(false);
        searchPane.getStyleClass().clear();
        searchPane.getStyleClass().add("searchPane");
    }
    private void disableSearchPane() {
        hmbDrawerOpener.setDisable(true);
        searchPane.getStyleClass().clear();
        searchPane.getStyleClass().add("searchPane-disabled");
    }

    /**
     * Generates a node pop up if able from the given mouse event
     *
     * @param event
     */
    private void generateNodePopUp(MouseEvent event) {
        removeCurrentPopUp(); // only pop up allowed at a time
        System.out.println("CLICK ON NODE BUTTON");
        // Get the node clicked on (if any)
        MapNode nodeAt = mapDrawing.nodeAt(new Location(event, mapDrawing.getCurrentFloor()));

        if (nodeAt != null) {
            System.out.println("CLICKED ON " + nodeAt.getId());

            // Load the screen in and display it on the cursor
            FXMLLoader loader = new FXMLLoader();
            loader.setResources(Translator.getInstance().getNewBundle());
            loader.setLocation(getClass().getResource("/NodeInfoPopUp.fxml"));
            NodeInfoPopUpController nodePopUp = new NodeInfoPopUpController();
            loader.setController(nodePopUp);
            try {
                nodeInfo = loader.load();
            } catch (IOException e) {
                e.printStackTrace();
            }
            areaPane.getChildren().add(nodeInfo);
            System.out.println(areaPane.getChildren().getClass());
            nodePopUp.setInfo(nodeAt, pathfinding, event);
        } else {
            System.out.println("Clicked on a random location.");
            removeCurrentPopUp();
        }
    }

    //this needs to be edited to close the drawer instead
    private void removeCurrentPopUp() {
        if (nodeInfo != null && areaPane.getChildren().contains(nodeInfo)) {
            areaPane.getChildren().remove(nodeInfo);
            nodeInfo = null;
        }

    }//add methods for login click and translate click

    //CREATES THE ABOUT PAGE POP UP
    //TODO attach this method to the about button
    @FXML
    private void onAboutClick(ActionEvent e) {
        Stage aboutPopUp = new Stage();
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/AboutPage.fxml"));
        try {
            Parent root = (Parent) loader.load();
            Scene aboutScene = new Scene(root);
            aboutPopUp.setScene(aboutScene);
            aboutPopUp.resizableProperty().set(false);
            aboutPopUp.initModality(Modality.WINDOW_MODAL);
            aboutPopUp.showAndWait();

        } catch (IOException exception) {
            exception.printStackTrace();
        }

    }
    //END OF ABOUT PAGE POP UP

    //create the help page pop up
    @FXML
    private void onHelpClick(ActionEvent e) {
        Stage helpPopUp = new Stage();
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/HelpPage.fxml"));
        try {
            Parent root = (Parent) loader.load();
            Scene helpScene = new Scene(root);
            helpPopUp.setScene(helpScene);
            helpPopUp.resizableProperty().set(false);
            helpPopUp.initModality(Modality.WINDOW_MODAL);
            helpPopUp.showAndWait();
        } catch (IOException exception) {
            exception.printStackTrace();
        }

    }
}

