package application;


import java.io.IOException;
import java.net.URL;
import java.util.List;
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
    
    private ClientDAO clientDAO;
    private ObservableList<Client> clientsList;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        clientDAO = new ClientDAO();
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
        List<Client> allClients = clientDAO.getAllClients();
        clientsList.clear();
        clientsList.addAll(allClients);
        clientTableView.setItems(clientsList);
    }
    
    @FXML
    private void searchClient(ActionEvent event) {
        String searchName = searchField.getText().trim();
        
        if (searchName.isEmpty()) {
            loadAllClients();
            return;
        }
        
        List<Client> foundClients = clientDAO.searchClientsByName(searchName);
        clientsList.clear();
        
        if (foundClients.isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "No Results", 
                    "No clients found", "No clients matching '" + searchName + "' were found.");
        } else {
            clientsList.addAll(foundClients);
        }
        
        clientTableView.setItems(clientsList);
    }
    
    @FXML
    private void viewClientDetails(ActionEvent event) {
        Client selectedClient = clientTableView.getSelectionModel().getSelectedItem();
        
        if (selectedClient != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/repairshop/views/ClientDetailsView.fxml"));
                Parent root = loader.load();
                
                ClientDetailsController controller = loader.getController();
                controller.setClient(selectedClient);
                
                Stage stage = new Stage();
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.setTitle("Client Details");
                stage.setScene(new Scene(root));
                stage.showAndWait();
                
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Error", 
                        "Cannot open client details", "An error occurred while trying to open client details.");
                e.printStackTrace();
            }
        }
    }
    
    @FXML
    private void viewClientAppliances(ActionEvent event) {
        Client selectedClient = clientTableView.getSelectionModel().getSelectedItem();
        
        if (selectedClient != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/repairshop/views/ClientAppliancesView.fxml"));
                Parent root = loader.load();
                
                ClientAppliancesController controller = loader.getController();
                controller.loadClientAppliances(selectedClient.getId());
                
                Stage stage = new Stage();
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.setTitle("Client Appliances");
                stage.setScene(new Scene(root));
                stage.showAndWait();
                
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Error", 
                        "Cannot open appliances", "An error occurred while trying to open client appliances.");
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