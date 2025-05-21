package application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * Main application class for the Repair Shop Management System
 * Integrates both client search and repair order functionality
 */
public class RepairShopApplication extends Application {
    
	@Override
	public void start(Stage primaryStage) {
	    try {
	        // Test database connection
	        if (!DatabaseConnection.testConnection()) {
	            showDatabaseError();
	            return;
	        }
	        
	        // Create a TabPane to hold all views
	        TabPane tabPane = new TabPane();
	        
	        // Create and load the Client Search tab
	        Tab clientSearchTab = new Tab("Client Search");
	        clientSearchTab.setClosable(false);
	        try {
	            Parent clientSearchView = FXMLLoader.load(getClass().getResource("recherche_client.fxml"));
	            clientSearchTab.setContent(clientSearchView);
	        } catch (Exception e) {
	            e.printStackTrace();
	            showError("Loading Error", "Failed to load client search view: " + e.getMessage());
	        }
	        
	        // Create and load the Repair Order tab
	        Tab repairOrderTab = new Tab("Repair Order");
	        repairOrderTab.setClosable(false);
	        try {
	            Parent repairOrderView = FXMLLoader.load(getClass().getResource("reparation.fxml"));
	            ScrollPane scrollPane = new ScrollPane();
	            scrollPane.setContent(repairOrderView);
	            scrollPane.setFitToWidth(true);
	            scrollPane.setVbarPolicy(ScrollBarPolicy.ALWAYS);
	            repairOrderTab.setContent(scrollPane);
	        } catch (Exception e) {
	            e.printStackTrace();
	            showError("Loading Error", "Failed to load repair order view: " + e.getMessage());
	        }
	        
	        // Create and load the Invoice tab
	        Tab invoiceTab = new Tab("Facture");
	        invoiceTab.setClosable(false);
	        try {
	            Parent invoiceView = FXMLLoader.load(getClass().getResource("facture.fxml"));
	            invoiceTab.setContent(invoiceView);
	        } catch (Exception e) {
	            e.printStackTrace();
	            showError("Loading Error", "Failed to load invoice view: " + e.getMessage());
	        }
	        
	        // Add tabs to the tab pane
	        tabPane.getTabs().addAll(clientSearchTab, repairOrderTab, invoiceTab);
	        
	        // Create a BorderPane as the main layout
	        BorderPane mainLayout = new BorderPane();
	        mainLayout.setCenter(tabPane);
	        
	        // Create the scene and set it on the stage
	        Scene scene = new Scene(mainLayout, 900, 700);
	        
	        // Configure the stage
	        primaryStage.setTitle("Repair Shop Management System");
	        primaryStage.setScene(scene);
	        primaryStage.show();
	        
	    } catch (Exception e) {
	        e.printStackTrace();
	        showError("Application Error", "An error occurred while starting the application: " + e.getMessage());
	    }
	}
    
    private void showDatabaseError() {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Database Connection Error");
        alert.setHeaderText("Could not connect to the database");
        alert.setContentText("Please check that the MySQL server is running and the connection parameters are correct.");
        
        // Add detail about setting up the database
        alert.getDialogPane().setExpandableContent(
            new javafx.scene.control.TextArea(
                "Make sure you have created the 'reparation' database and run the database setup script.\n\n" +
                "Check the DatabaseConnection.java file to verify the connection parameters:\n" +
                "- URL: jdbc:mysql://localhost:3306/reparation?serverTimezone=UTC\n" +
                "- User: root\n" +
                "- Password: (as configured)\n\n" +
                "If you're still having issues, ensure MySQL is installed and running correctly."
            )
        );
        
        alert.showAndWait();
    }
    
    private void showError(String title, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}