package application;



import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableView;
import java.net.URL;
import java.util.ResourceBundle;

public class ClientAppliancesController implements Initializable {
    
    @FXML
    private TableView<?> appliancesTableView;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialisation du contrôleur
    }
    
    public void loadClientAppliances(int clientId) {
        // Charger les appareils du client avec l'ID spécifié
        // Cette méthode sera complétée plus tard
        System.out.println("Loading appliances for client ID: " + clientId);
    }
}