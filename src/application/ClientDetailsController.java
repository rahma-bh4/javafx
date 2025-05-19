package application;


import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import java.net.URL;
import java.util.ResourceBundle;

public class ClientDetailsController implements Initializable {
    
    @FXML
    private Label idLabel;
    
    @FXML
    private Label nameLabel;
    
    @FXML
    private Label addressLabel;
    
    @FXML
    private Label phoneLabel;
    
    private Client client;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialisation du contrôleur
    }
    
    public void setClient(Client client) {
        this.client = client;
        
        // Mettre à jour les labels avec les informations du client
        idLabel.setText(String.valueOf(client.getId()));
        nameLabel.setText(client.getName());
        addressLabel.setText(client.getAddress());
        phoneLabel.setText(client.getPhoneNumber());
    }
}