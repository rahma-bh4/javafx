package application;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ClientSearchController implements Initializable {
    
    @FXML
    private TextField searchField;
    
    @FXML
    private Button searchButton;
    
    @FXML
    private TableView<Client> clientTableView;
    
    @FXML
    private TableColumn<Client, Integer> idColumn;
    
    @FXML
    private TableColumn<Client, String> nameColumn;
    
    @FXML
    private TableColumn<Client, String> addressColumn;
    
    @FXML
    private TableColumn<Client, String> phoneColumn;
    
    @FXML
    private Button viewDetailsButton;
    
    @FXML
    private Button viewAppliancesButton;
    
    @FXML
    private Button closeButton;
    
    private ObservableList<Client> clientsList;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        clientsList = FXCollections.observableArrayList();
        
        // Initialize table columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        addressColumn.setCellValueFactory(new PropertyValueFactory<>("address"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        
        // Load all clients on initialization
        loadAllClients();
        
        // Set buttons to be disabled initially until a client is selected
        viewDetailsButton.setDisable(true);
        viewAppliancesButton.setDisable(true);
        
        // Add a listener to enable buttons when a client is selected
        clientTableView.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                boolean disableButtons = (newValue == null);
                viewDetailsButton.setDisable(disableButtons);
                viewAppliancesButton.setDisable(disableButtons);
            }
        );
        
        // Add listener for Enter key on search field
        searchField.setOnKeyPressed(event -> {
            if (event.getCode().toString().equals("ENTER")) {
                searchClient(new ActionEvent());
            }
        });
    }
    
    private void loadAllClients() {
        clientsList.clear();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM client ORDER BY name");
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String address = rs.getString("address");
                String phoneNumber = rs.getString("phoneNumber");
                
                Client client = new Client(id, name, address, phoneNumber);
                clientsList.add(client);
            }
            
            clientTableView.setItems(clientsList);
            
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", 
                      "Error loading clients", e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void searchClient(ActionEvent event) {
        String searchText = searchField.getText().trim();
        
        if (searchText.isEmpty()) {
            loadAllClients();
            return;
        }
        
        clientsList.clear();
        
        // Search by name, id, or phone number
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                "SELECT * FROM client WHERE name LIKE ? OR id = ? OR phoneNumber LIKE ? ORDER BY name")) {
            
            // Try to parse ID if the search is numeric
            int id = 0;
            try {
                id = Integer.parseInt(searchText);
            } catch (NumberFormatException e) {
                // Not a number, id remains 0
            }
            
            stmt.setString(1, "%" + searchText + "%");
            stmt.setInt(2, id);
            stmt.setString(3, "%" + searchText + "%");
            
            ResultSet rs = stmt.executeQuery();
            
            boolean found = false;
            while (rs.next()) {
                found = true;
                int clientId = rs.getInt("id");
                String name = rs.getString("name");
                String address = rs.getString("address");
                String phoneNumber = rs.getString("phoneNumber");
                
                Client client = new Client(clientId, name, address, phoneNumber);
                clientsList.add(client);
            }
            
            clientTableView.setItems(clientsList);
            
            if (!found) {
                showAlert(Alert.AlertType.INFORMATION, "No Results", 
                          "No clients found", "No clients matching '" + searchText + "' were found.");
            }
            
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Search Error", 
                      "Error searching for clients", e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void viewClientDetails(ActionEvent event) {
        Client selectedClient = clientTableView.getSelectionModel().getSelectedItem();
        
        if (selectedClient != null) {
            showAlert(Alert.AlertType.INFORMATION, "Client Details", 
                      "Client: " + selectedClient.getName(),
                      "ID: " + selectedClient.getId() + "\n" +
                      "Name: " + selectedClient.getName() + "\n" +
                      "Address: " + selectedClient.getAddress() + "\n" +
                      "Phone: " + selectedClient.getPhoneNumber());
        }
    }
    
    @FXML
    private void viewClientAppliances(ActionEvent event) {
        Client selectedClient = clientTableView.getSelectionModel().getSelectedItem();
        
        if (selectedClient != null) {
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                    "SELECT a.id_appareil, a.description, a.marque, c.nom_categorie " +
                    "FROM appareil a " +
                    "JOIN ordre_reparation o ON a.id_appareil = o.id_appareil " +
                    "JOIN categorie c ON a.id_categorie = c.id_categorie " +
                    "WHERE o.id = ?")) {
                
                stmt.setInt(1, selectedClient.getId());
                ResultSet rs = stmt.executeQuery();
                
                StringBuilder devices = new StringBuilder();
                boolean hasDevices = false;
                
                while (rs.next()) {
                    hasDevices = true;
                    devices.append("ID: ").append(rs.getInt("id_appareil"))
                           .append("\nDescription: ").append(rs.getString("description"))
                           .append("\nBrand: ").append(rs.getString("marque"))
                           .append("\nCategory: ").append(rs.getString("nom_categorie"))
                           .append("\n\n");
                }
                
                if (hasDevices) {
                    showAlert(Alert.AlertType.INFORMATION, "Client Devices", 
                              "Devices for " + selectedClient.getName(), 
                              devices.toString());
                } else {
                    showAlert(Alert.AlertType.INFORMATION, "No Devices", 
                              "No devices found", 
                              "No devices found for client " + selectedClient.getName());
                }
                
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Error", 
                          "Cannot load client devices", e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    @FXML
    private void closeWindow(ActionEvent event) {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
    
    private void showAlert(Alert.AlertType alertType, String title, String header, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}