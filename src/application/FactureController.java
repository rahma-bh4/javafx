package application;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class FactureController implements Initializable {
    
    // Constante pour le tarif horaire de la main d'œuvre
    private final double TARIF_HORAIRE_MO = 45.0;
    
    // Variables pour stocker les objets sélectionnés
    private Client clientSelectionne;
    private Map<String, Object> appareilSelectionne;
    private Map<String, Object> ordreSelectionne;
    private double tarifCategorie;
    private List<PieceDetachee> piecesDetachees;
    
    // Injection des composants d'interface définis dans le FXML
    @FXML private TextField txtRechercheClient;
    @FXML private Button btnRechercher;
    
    @FXML private HBox hboxAppareils;
    @FXML private ComboBox<String> cmbAppareils;
    
    @FXML private HBox hboxOrdres;
    @FXML private ComboBox<String> cmbOrdresReparation;
    
    @FXML private HBox hboxBoutonAfficher;
    @FXML private Button btnAfficherFacture;
    
    @FXML private ScrollPane scrollFacture;
    @FXML private VBox vboxFacture;
    @FXML private HBox hboxBoutonsAction;
    
    @FXML private Label lblNumeroFacture;
    @FXML private Label lblDateFacture;
    
    @FXML private Label lblNomClient;
    @FXML private Label lblAdresseClient;
    @FXML private Label lblTelephoneClient;
    
    @FXML private Label lblDescriptionAppareil;
    @FXML private Label lblMarqueAppareil;
    @FXML private Label lblCategorieAppareil;
    @FXML private Label lblTarifCategorie;
    
    @FXML private Label lblHeuresMO;
    @FXML private Label lblCoutMO;
    @FXML private Label lblInfosSupplementaires;
    
    @FXML private TableView<PieceDetachee> tablePieces;
    @FXML private TableColumn<PieceDetachee, String> colNomPiece;
    @FXML private TableColumn<PieceDetachee, BigDecimal> colPrixPiece;
    @FXML private Label lblTotalPieces;
    
    @FXML private Label lblRecapTarifCategorie;
    @FXML private Label lblRecapCoutMO;
    @FXML private Label lblRecapTotalPieces;
    @FXML private Label lblCoutTotal;
    
    private ClientDAO clientDAO;
    private FactureDAO factureDAO;
    
    // Maps to store loaded data
    private List<Map<String, Object>> appareils;
    private List<Map<String, Object>> ordres;
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        clientDAO = new ClientDAO();
        factureDAO = new FactureDAO();
        
        // Configuration des colonnes du tableau
        colNomPiece.setCellValueFactory(new PropertyValueFactory<>("nomPiece"));
        colPrixPiece.setCellValueFactory(new PropertyValueFactory<>("prixHT"));
        
        // Formater la colonne de prix pour afficher 2 décimales
        colPrixPiece.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal prix, boolean empty) {
                super.updateItem(prix, empty);
                if (empty || prix == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f €", prix));
                }
            }
        });
        
        // Initialiser les vues
        scrollFacture.setVisible(false);
        hboxBoutonsAction.setVisible(false);
        hboxAppareils.setVisible(false);
        hboxOrdres.setVisible(false);
        hboxBoutonAfficher.setVisible(false);
        
        // Configuration des événements
        cmbAppareils.setOnAction(event -> {
            String selected = cmbAppareils.getSelectionModel().getSelectedItem();
            if (selected != null) {
                // Extract appareil ID from the displayed string
                String idStr = selected.split(" - ")[0].trim();
                int appareilId = Integer.parseInt(idStr);
                
                // Find the selected appareil in our loaded list
                for (Map<String, Object> appareil : appareils) {
                    if ((int)appareil.get("id") == appareilId) {
                        appareilSelectionne = appareil;
                        break;
                    }
                }
                
                chargerOrdresReparation(appareilId);
            }
        });
        
        cmbOrdresReparation.setOnAction(event -> {
            String selected = cmbOrdresReparation.getSelectionModel().getSelectedItem();
            if (selected != null) {
                // Extract ordre ID from the displayed string
                String idStr = selected.split(" - ")[0].trim();
                int ordreId = Integer.parseInt(idStr);
                
                // Find the selected ordre in our loaded list
                for (Map<String, Object> ordre : ordres) {
                    if ((int)ordre.get("id") == ordreId) {
                        ordreSelectionne = ordre;
                        break;
                    }
                }
                
                hboxBoutonAfficher.setVisible(true);
            }
        });
    }
    
    @FXML
    private void rechercherClient() {
        String nomRecherche = txtRechercheClient.getText().trim();
        if (nomRecherche.isEmpty()) {
            afficherErreur("Veuillez entrer un nom de client à rechercher.");
            return;
        }
        
        List<Client> clients = clientDAO.searchClientsByName(nomRecherche);
        if (clients.isEmpty()) {
            afficherInfo("Aucun client trouvé avec ce nom.");
            return;
        }
        
        // Pour simplifier, on prend le premier client trouvé
        clientSelectionne = clients.get(0);
        System.out.println("Client trouvé: " + clientSelectionne.getName() + " (ID: " + clientSelectionne.getId() + ")");
        
        // Charger les appareils du client
        chargerAppareils(clientSelectionne.getId());
    }
    
    private void chargerAppareils(int clientId) {
        appareils = factureDAO.getAppareilsForClient(clientId);
        
        if (appareils.isEmpty()) {
            afficherInfo("Ce client n'a pas d'appareils enregistrés.");
            return;
        }
        
        ObservableList<String> appareilsStrings = FXCollections.observableArrayList();
        for (Map<String, Object> appareil : appareils) {
            appareilsStrings.add(
                appareil.get("id") + " - " + 
                appareil.get("marque") + " " + 
                appareil.get("description")
            );
        }
        
        cmbAppareils.setItems(appareilsStrings);
        hboxAppareils.setVisible(true);
        hboxOrdres.setVisible(false);
        hboxBoutonAfficher.setVisible(false);
    }
    
    private void chargerOrdresReparation(int appareilId) {
        ordres = factureDAO.getOrdresForAppareil(appareilId);
        
        if (ordres.isEmpty()) {
            afficherInfo("Cet appareil n'a pas d'ordres de réparation.");
            return;
        }
        
        ObservableList<String> ordresStrings = FXCollections.observableArrayList();
        for (Map<String, Object> ordre : ordres) {
            ordresStrings.add(
                ordre.get("id") + " - " + 
                ordre.get("heures") + " heures de réparation"
            );
        }
        
        cmbOrdresReparation.setItems(ordresStrings);
        hboxOrdres.setVisible(true);
    }
    
    @FXML
    private void afficherFacture() {
        if (clientSelectionne == null || appareilSelectionne == null || ordreSelectionne == null) {
            afficherErreur("Veuillez sélectionner un client, un appareil et un ordre de réparation.");
            return;
        }
        
        // Récupérer le tarif de la catégorie
        int categorieId = (int)appareilSelectionne.get("categorieId");
        tarifCategorie = factureDAO.getCategorieRate(categorieId);
        
        // Récupérer les pièces détachées pour cet ordre
        piecesDetachees = factureDAO.getPiecesForOrdre((int)ordreSelectionne.get("id"));
        
        // Afficher les informations dans l'interface
        afficherInformationsFacture();
        
        // Rendre visible les sections de la facture
        scrollFacture.setVisible(true);
        vboxFacture.setVisible(true);
        hboxBoutonsAction.setVisible(true);
    }
    
    private void afficherInformationsFacture() {
        // Numéro de facture et date
        lblNumeroFacture.setText("Facture N° F-" + ordreSelectionne.get("id") + "-" + LocalDate.now().getYear());
        lblDateFacture.setText("Date: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        
        // Informations client
        lblNomClient.setText(clientSelectionne.getName());
        lblAdresseClient.setText(clientSelectionne.getAddress());
        lblTelephoneClient.setText(clientSelectionne.getPhoneNumber());
        
        // Informations appareil
        lblDescriptionAppareil.setText((String)appareilSelectionne.get("description"));
        lblMarqueAppareil.setText((String)appareilSelectionne.get("marque"));
        lblCategorieAppareil.setText((String)appareilSelectionne.get("categorie"));
        lblTarifCategorie.setText(String.format("%.2f €", tarifCategorie));
        
        // Informations réparation
        int heuresMO = (int)ordreSelectionne.get("heures");
        lblHeuresMO.setText(String.valueOf(heuresMO));
        double coutMO = heuresMO * TARIF_HORAIRE_MO;
        lblCoutMO.setText(String.format("%.2f € (%.2f €/h)", coutMO, TARIF_HORAIRE_MO));
        lblInfosSupplementaires.setText((String)ordreSelectionne.get("infos"));
        
        // Tableau des pièces
        tablePieces.setItems(FXCollections.observableArrayList(piecesDetachees));
        
        // Calcul des totaux
        BigDecimal totalPieces = piecesDetachees.stream()
            .map(PieceDetachee::getPrixHT)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
            
        lblTotalPieces.setText(String.format("%.2f €", totalPieces));
        
        // Récapitulatif
        lblRecapTarifCategorie.setText(String.format("%.2f €", tarifCategorie));
        lblRecapCoutMO.setText(String.format("%.2f €", coutMO));
        lblRecapTotalPieces.setText(String.format("%.2f €", totalPieces));
        
        double coutTotal = tarifCategorie + coutMO + totalPieces.doubleValue();
        lblCoutTotal.setText(String.format("%.2f €", coutTotal));
    }
    
    @FXML
    private void imprimerFacture() {
        try {
            // We'll print the facture node
            FacturePrintService.printNode(vboxFacture);
        } catch (Exception e) {
            e.printStackTrace();
            afficherErreur("Erreur d'impression");
        }
    }

    @FXML
    private void exporterPDF() {
        try {
            // Calculate the total price of parts
            BigDecimal totalPieces = piecesDetachees.stream()
                .map(PieceDetachee::getPrixHT)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
                
            // Export to PDF
            FacturePrintService.exportToPDF(
                (int)ordreSelectionne.get("id"),
                clientSelectionne,
                (String)appareilSelectionne.get("description"),
                (String)appareilSelectionne.get("marque"),
                (String)appareilSelectionne.get("categorie"),
                tarifCategorie,
                (int)ordreSelectionne.get("heures"),
                TARIF_HORAIRE_MO,
                (String)ordreSelectionne.get("infos"),
                piecesDetachees
            );
        } catch (Exception e) {
            e.printStackTrace();
            afficherErreur("Erreur d'export PDF");
        }
    }
    
    @FXML
    private void retour() {
        scrollFacture.setVisible(false);
        vboxFacture.setVisible(false);
        hboxBoutonsAction.setVisible(false);
    }
    
    private void afficherErreur(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void afficherInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    
    
}