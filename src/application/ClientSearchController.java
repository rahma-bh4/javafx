package application;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
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
    @FXML private Button updateClientButton;
    @FXML private Button deleteClientButton;
    @FXML private Label resultCountLabel;
    private ClientDAO clientDAO;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        clientsList = FXCollections.observableArrayList();
        clientDAO = new ClientDAO();
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
        updateClientButton.setDisable(true);
        deleteClientButton.setDisable(true);
        
        // Add a listener to enable buttons when a client is selected
        clientTableView.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                boolean disableButtons = (newValue == null);
                viewDetailsButton.setDisable(disableButtons);
                viewAppliancesButton.setDisable(disableButtons);
                updateClientButton.setDisable(disableButtons);
                deleteClientButton.setDisable(disableButtons);
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
    
    @FXML
    private void updateClient(ActionEvent event) {
        Client selectedClient = clientTableView.getSelectionModel().getSelectedItem();
        
        if (selectedClient != null) {
            // Create a dialog for editing client information
            Dialog<Client> dialog = new Dialog<>();
            dialog.setTitle("Modifier Client");
            dialog.setHeaderText("Modifier les informations de " + selectedClient.getName());
            
            // Set the button types
            ButtonType saveButtonType = new ButtonType("Enregistrer", ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
            
            // Create the form fields
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));
            
            TextField nameField = new TextField(selectedClient.getName());
            TextField addressField = new TextField(selectedClient.getAddress());
            TextField phoneField = new TextField(selectedClient.getPhoneNumber());
            
            grid.add(new Label("Nom:"), 0, 0);
            grid.add(nameField, 1, 0);
            grid.add(new Label("Adresse:"), 0, 1);
            grid.add(addressField, 1, 1);
            grid.add(new Label("Téléphone:"), 0, 2);
            grid.add(phoneField, 1, 2);
            
            dialog.getDialogPane().setContent(grid);
            
            // Request focus on the name field by default
            nameField.requestFocus();
            
            // Convert the result to a client when the save button is clicked
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == saveButtonType) {
                    // Validate input
                    if (nameField.getText().trim().isEmpty()) {
                        showAlert(AlertType.ERROR, "Erreur de validation", 
                                 "Le nom du client ne peut pas être vide.", null);
                        return null;
                    }
                    
                    // Update client object with new values
                    Client updatedClient = new Client(
                        selectedClient.getId(),
                        nameField.getText().trim(),
                        addressField.getText().trim(),
                        phoneField.getText().trim()
                    );
                    
                    return updatedClient;
                }
                return null;
            });
            
            // Show the dialog and process the result
            Optional<Client> result = dialog.showAndWait();
            
            result.ifPresent(updatedClient -> {
                // Update the client in the database
                boolean success = clientDAO.updateClient(updatedClient);
                
                if (success) {
                    // Update the client in the table
                    int selectedIndex = clientTableView.getSelectionModel().getSelectedIndex();
                    clientsList.set(selectedIndex, updatedClient);
                    clientTableView.refresh();
                    
                    showAlert(AlertType.INFORMATION, "Client mis à jour", 
                             "Les informations du client ont été mises à jour avec succès.", null);
                } else {
                    showAlert(AlertType.ERROR, "Erreur de mise à jour", 
                             "Une erreur est survenue lors de la mise à jour du client.", null);
                }
            });
        }
    }
    
    /**
     * Deletes the selected client
     */
    @FXML
    private void deleteClient(ActionEvent event) {
        Client selectedClient = clientTableView.getSelectionModel().getSelectedItem();
        
        if (selectedClient != null) {
            // Show confirmation dialog
            Alert alert = new Alert(AlertType.CONFIRMATION);
            alert.setTitle("Confirmation de suppression");
            alert.setHeaderText("Supprimer le client " + selectedClient.getName() + "?");
            alert.setContentText("Cette action ne peut pas être annulée.");
            
            Optional<ButtonType> result = alert.showAndWait();
            
            if (result.isPresent() && result.get() == ButtonType.OK) {
                // Try to delete the client
                boolean success = clientDAO.deleteClient(selectedClient.getId());
                
                if (success) {
                    // Remove the client from the table
                    clientsList.remove(selectedClient);
                    clientTableView.refresh();
                    resultCountLabel.setText(clientsList.size() + " client(s) trouvé(s)");
                    
                    showAlert(AlertType.INFORMATION, "Client supprimé", 
                             "Le client a été supprimé avec succès.", null);
                } else {
                    showAlert(AlertType.ERROR, "Erreur de suppression", 
                             "Impossible de supprimer ce client. Il est peut-être associé à des ordres de réparation.", null);
                }
            }
        }
    }
    
    
    
}