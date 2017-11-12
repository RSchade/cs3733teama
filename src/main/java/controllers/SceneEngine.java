package controllers;

import boundaries.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import sun.misc.Request;

import java.io.IOException;

public final class SceneEngine{

    static private Stage primaryStage, popOutStage;

    public static void setStages(Stage primaryStage) {
        SceneEngine.primaryStage = primaryStage;
        popOutStage=new Stage();
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void closePopOut(){
        popOutStage.close();
    }
    public static void displayMainScreen(){
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainScreenController.class.getResource("../MainScreen.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.show();

        }catch(IOException e){
            System.out.println("couldn't load screen");
        }
    }
    //TODO add the login screen
    public static void displayLoginScreen(){

    }
    public static void displayMapEditor(){
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MapEditorController.class.getResource("../MapEditor.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.show();

        }catch(IOException e){
            e.printStackTrace();
        }
    }
    public static void displayRequestScreen(){
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(RequestScreenController.class.getResource("../RequestScreen.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.show();

        }catch(IOException e) {
            e.printStackTrace();
        }
    }
    public static void displayFulfillRequest(){
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(FulfillReqController.class.getResource("../FulfillRequest.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            popOutStage.setScene(scene);
            popOutStage.show();

        }catch(IOException e){
            e.printStackTrace();
        }
    }
    public static void displayDirectionsScreen(){
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(DirectionsController.class.getResource("../DirectionsScreen.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.show();

        }catch(IOException e){
            e.printStackTrace();
        }
    }

}