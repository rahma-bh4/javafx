package application;

import java.math.BigDecimal;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;
import java.util.ResourceBundle;

import application.Client;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.util.StringConverter;
import javafx.util.converter.IntegerStringConverter;

public class OrdreReparationController implements Initializable {

    // Champs client
    @FXML private TextField txtRechercheClient;
    @FXML private TextField txtNomClient;
    @FXML private TextField txtAdresseClient;
    @FXML private TextField txtTelephoneClient;
    @FXML private Button btnRechercherClient;
    
    // Champs appareil
    @FXML private TextArea txtDescriptionAppareil;
    @FXML private TextField txtMarqueAppareil;
    @FXML private ComboBox<Categorie> cmbCategorieAppareil;
    
    // Champs réparation
    @FXML private Spinner<Integer> spinnerHeuresMO;
    @FXML private TextArea txtInfosSupplementaires;
    
    // Pièces détachées
    @FXML private TextField txtRecherchePiece;
    @FXML private TableView<PieceDetachee> piecesDisponiblesTable;
    @FXML private TableColumn<PieceDetachee, String> colNomPieceDispo;
    @FXML private TableColumn<PieceDetachee, String> colReferencePieceDispo;
    @FXML private TableColumn<PieceDetachee, BigDecimal> colPrixHtPieceDispo;
    
    @FXML private TableView<PieceOrdre> piecesTable;
    @FXML private TableColumn<PieceOrdre, String> colNomPiece;
    @FXML private TableColumn<PieceOrdre, BigDecimal> colPrixHT;
    @FXML private TableColumn<PieceOrdre, Integer> colQuantite;
    
    @FXML private Label lblTotalPiecesHT;
    
    // Boutons actions
    @FXML private Button btnEnregistrer;
    @FXML private Button btnAnnuler;
    @FXML private Button btnAjouterPiece;
    @FXML private Button btnSupprimerPiece;
    @FXML private Button btnAjouterNouvellePiece; // Nouveau bouton pour ajouter une nouvelle pièce
    
    // Listes de données
    private ObservableList<Categorie> categories = FXCollections.observableArrayList();
    private ObservableList<PieceDetachee> piecesDisponibles = FXCollections.observableArrayList();
    private ObservableList<PieceOrdre> piecesSélectionnées = FXCollections.observableArrayList();
    
    // Client actuel
    private Client clientActuel = null;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            System.out.println("Initializing controller...");
            
            // Initialiser les composants
            initSpinner();
            initCategories();
            
            // Vérifier si les éléments TableView sont correctement injectés
            if (piecesDisponiblesTable == null) {
                System.err.println("piecesDisponiblesTable est null!");
            } else {
                System.out.println("piecesDisponiblesTable OK");
                initPiecesDetacheesTableau();
            }
            
            if (piecesTable == null) {
                System.err.println("piecesTable est null!");
            } else {
                System.out.println("piecesTable OK");
                initPiecesSelectionneesTableau();
            }
            
            // Configurer les événements
            configurerRecherchePieces();
            configurerEcouteursPieces();
            
            // Associer les actions aux boutons
            if (btnEnregistrer != null) btnEnregistrer.setOnAction(e -> enregistrerOrdreReparation());
            if (btnAnnuler != null) btnAnnuler.setOnAction(e -> annuler());
            if (btnAjouterPiece != null) btnAjouterPiece.setOnAction(e -> ajouterPieceSelectionnee());
            if (btnSupprimerPiece != null) btnSupprimerPiece.setOnAction(e -> supprimerPieceSelectionnee());
            if (btnAjouterNouvellePiece != null) btnAjouterNouvellePiece.setOnAction(e -> ajouterNouvellePiece());
            
            // Calcul du total initial
            calculerTotalPiecesHT();
            
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur d'initialisation", "Une erreur est survenue lors de l'initialisation: " + e.getMessage());
        }
    }

    private void initSpinner() {
        if (spinnerHeuresMO != null) {
            SpinnerValueFactory<Integer> valueFactory = 
                    new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100, 1);
            spinnerHeuresMO.setValueFactory(valueFactory);
        } else {
            System.err.println("spinnerHeuresMO est null!");
        }
    }
    
    private void initCategories() {
        if (cmbCategorieAppareil == null) {
            System.err.println("cmbCategorieAppareil est null!");
            return;
        }
        
        // Charger les catégories depuis la base de données
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id_categorie, nom_categorie FROM categorie")) {
            
            while (rs.next()) {
                int id = rs.getInt("id_categorie");
                String nom = rs.getString("nom_categorie");
                categories.add(new Categorie(id, nom));
            }
            
            // Définir le convertisseur pour afficher le nom de la catégorie
            cmbCategorieAppareil.setConverter(new StringConverter<Categorie>() {
                @Override
                public String toString(Categorie categorie) {
                    return categorie == null ? null : categorie.getNomCategorie();
                }
                
                @Override
                public Categorie fromString(String string) {
                    return categories.stream()
                           .filter(c -> c.getNomCategorie().equals(string))
                           .findFirst().orElse(null);
                }
            });
            
            // Ajouter les catégories à la combobox
            cmbCategorieAppareil.setItems(categories);
            
            System.out.println("Categories loaded: " + categories.size());
            
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur lors du chargement des catégories", e.getMessage());
        }
    }
    
    private void initPiecesDetacheesTableau() {
        // Debug to check column names
        System.out.println("Initializing parts table columns...");
        System.out.println("colNomPieceDispo: " + (colNomPieceDispo != null ? "OK" : "NULL"));
        System.out.println("colReferencePieceDispo: " + (colReferencePieceDispo != null ? "OK" : "NULL"));
        System.out.println("colPrixHtPieceDispo: " + (colPrixHtPieceDispo != null ? "OK" : "NULL"));
        
        if (colNomPieceDispo == null || colReferencePieceDispo == null || colPrixHtPieceDispo == null) {
            System.err.println("Une ou plusieurs colonnes de piecesDisponiblesTable sont nulles!");
            return;
        }
        
        // Configurer les colonnes du tableau des pièces disponibles
        colNomPieceDispo.setCellValueFactory(new PropertyValueFactory<>("nomPiece"));
        colReferencePieceDispo.setCellValueFactory(new PropertyValueFactory<>("reference"));
        colPrixHtPieceDispo.setCellValueFactory(new PropertyValueFactory<>("prixHT"));
        
        // Charger toutes les pièces détachées
        chargerPiecesDetachees("");
    }
    
    private void initPiecesSelectionneesTableau() {
        System.out.println("Initializing selected parts table columns...");
        System.out.println("colNomPiece: " + (colNomPiece != null ? "OK" : "NULL"));
        System.out.println("colPrixHT: " + (colPrixHT != null ? "OK" : "NULL"));
        System.out.println("colQuantite: " + (colQuantite != null ? "OK" : "NULL"));
        
        if (colNomPiece == null || colPrixHT == null || colQuantite == null) {
            System.err.println("Une ou plusieurs colonnes de piecesTable sont nulles!");
            return;
        }
        
        // Configurer les colonnes du tableau des pièces sélectionnées
        // Utiliser PropertyValueFactory pour une compatibilité plus large
        colNomPiece.setCellValueFactory(new PropertyValueFactory<>("nomPiece"));
        colPrixHT.setCellValueFactory(new PropertyValueFactory<>("prixHT"));
        colQuantite.setCellValueFactory(new PropertyValueFactory<>("quantite"));
        
        // Rendre la colonne quantité éditable
        piecesTable.setEditable(true);
        colQuantite.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        colQuantite.setOnEditCommit(event -> {
            PieceOrdre pieceOrdre = event.getRowValue();
            pieceOrdre.setQuantite(event.getNewValue());
            calculerTotalPiecesHT();
        });
        
        // Associer le tableau à la liste observable
        piecesTable.setItems(piecesSélectionnées);
    }
    
    private void chargerPiecesDetachees(String recherche) {
        if (piecesDisponiblesTable == null) {
            System.err.println("piecesDisponiblesTable est null lors du chargement des pièces!");
            return;
        }
        
        piecesDisponibles.clear();
        
        // Verify the table and column names match your database schema
        String requete = "SELECT id_piece, nom_piece, reference, prix_ht FROM piece_detachee";
        if (!recherche.isEmpty()) {
            requete += " WHERE nom_piece LIKE ? OR reference LIKE ?";
        }
        
        System.out.println("Executing query: " + requete);
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(requete)) {
            
            if (!recherche.isEmpty()) {
                String searchPattern = "%" + recherche + "%";
                pstmt.setString(1, searchPattern);
                pstmt.setString(2, searchPattern);
            }
            
            ResultSet rs = pstmt.executeQuery();
            
            int count = 0;
            while (rs.next()) {
                count++;
                int id = rs.getInt("id_piece");
                String nom = rs.getString("nom_piece");
                String reference = rs.getString("reference");
                BigDecimal prixHt = rs.getBigDecimal("prix_ht");
                
                System.out.println("Found part: " + nom);
                piecesDisponibles.add(new PieceDetachee(id, nom, reference, prixHt));
            }
            
            System.out.println("Total parts found: " + count);
            
            if (count == 0) {
                // If no parts were found, check database connection and table structure
                System.err.println("No parts found in database! Check table structure and connection.");
                
                // Print table structure for debugging
                try (Statement stmt = conn.createStatement();
                     ResultSet tablesRS = stmt.executeQuery("SHOW TABLES")) {
                    
                    System.out.println("Available tables:");
                    while (tablesRS.next()) {
                        String tableName = tablesRS.getString(1);
                        System.out.println(" - " + tableName);
                        
                        if (tableName.equals("piece_detachee")) {
                            // Print columns for this table
                            try (ResultSet columnsRS = stmt.executeQuery("DESCRIBE " + tableName)) {
                                System.out.println("   Columns in piece_detachee:");
                                while (columnsRS.next()) {
                                    System.out.println("   - " + columnsRS.getString("Field") + 
                                                       " (" + columnsRS.getString("Type") + ")");
                                }
                            }
                        }
                    }
                } catch (SQLException e) {
                    System.err.println("Error checking database structure: " + e.getMessage());
                }
            }
            
            piecesDisponiblesTable.setItems(piecesDisponibles);
            
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("SQL Error: " + e.getMessage());
            showError("Erreur lors du chargement des pièces détachées", e.getMessage());
        }
    }
    
    private void configurerRecherchePieces() {
        if (txtRecherchePiece == null) {
            System.err.println("txtRecherchePiece est null!");
            return;
        }
        
        // Configurer la recherche en temps réel pour les pièces détachées
        txtRecherchePiece.textProperty().addListener((observable, oldValue, newValue) -> {
            chargerPiecesDetachees(newValue);
        });
    }
    
    private void configurerEcouteursPieces() {
        // Ajouter des écouteurs pour les boutons d'ajout/suppression de pièces
        
        // Action pour le bouton de recherche de client
        if (btnRechercherClient != null) {
            btnRechercherClient.setOnAction(this::rechercherClient);
        } else {
            System.err.println("btnRechercherClient est null!");
        }
        
        // Double-clic sur une pièce disponible pour l'ajouter
        if (piecesDisponiblesTable != null) {
            piecesDisponiblesTable.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2) {
                    ajouterPieceSelectionnee();
                }
            });
        }
        
        // Double-clic sur une pièce sélectionnée pour la supprimer
        if (piecesTable != null) {
            piecesTable.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2) {
                    supprimerPieceSelectionnee();
                }
            });
        }
    }
    
    @FXML
    private void ajouterPieceSelectionnee() {
        if (piecesDisponiblesTable == null) {
            System.err.println("piecesDisponiblesTable est null lors de l'ajout d'une pièce!");
            return;
        }
        
        PieceDetachee pieceSelectionnee = piecesDisponiblesTable.getSelectionModel().getSelectedItem();
        
        if (pieceSelectionnee != null) {
            // Vérifier si la pièce est déjà dans la liste
            boolean pieceDejaPresente = false;
            
            for (PieceOrdre pieceOrdre : piecesSélectionnées) {
                if (pieceOrdre.getIdPiece() == pieceSelectionnee.getIdPiece()) {
                    // Incrémenter la quantité si déjà présente
                    pieceOrdre.setQuantite(pieceOrdre.getQuantite() + 1);
                    pieceDejaPresente = true;
                    break;
                }
            }
            
            // Ajouter la pièce si elle n'est pas déjà présente
            if (!pieceDejaPresente) {
                PieceOrdre nouvellePiece = new PieceOrdre(
                    pieceSelectionnee.getIdPiece(),
                    pieceSelectionnee.getNomPiece(),
                    pieceSelectionnee.getPrixHT(),
                    1
                );
                piecesSélectionnées.add(nouvellePiece);
            }
            
            // Rafraîchir la table et recalculer le total
            if (piecesTable != null) {
                piecesTable.refresh();
            }
            calculerTotalPiecesHT();
        } else {
            showInfo("Aucune pièce sélectionnée", "Veuillez sélectionner une pièce dans la liste des pièces disponibles.");
        }
    }
    
    @FXML
    private void supprimerPieceSelectionnee() {
        if (piecesTable == null) {
            System.err.println("piecesTable est null lors de la suppression d'une pièce!");
            return;
        }
        
        PieceOrdre pieceSelectionnee = piecesTable.getSelectionModel().getSelectedItem();
        
        if (pieceSelectionnee != null) {
            piecesSélectionnées.remove(pieceSelectionnee);
            calculerTotalPiecesHT();
        } else {
            showInfo("Aucune pièce sélectionnée", "Veuillez sélectionner une pièce dans la liste des pièces sélectionnées.");
        }
    }
    
    @FXML
    private void ajouterNouvellePiece() {
        // Create dialog to add a new part
        Dialog<PieceDetachee> dialog = new Dialog<>();
        dialog.setTitle("Ajouter une nouvelle pièce");
        dialog.setHeaderText("Entrez les détails de la nouvelle pièce");
        
        // Set the button types
        ButtonType ajouterButtonType = new ButtonType("Ajouter", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(ajouterButtonType, ButtonType.CANCEL);
        
        // Create the fields and labels
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        TextField nomField = new TextField();
        nomField.setPromptText("Nom de la pièce");
        TextField referenceField = new TextField();
        referenceField.setPromptText("Référence");
        TextField prixField = new TextField();
        prixField.setPromptText("Prix HT");
        
        grid.add(new Label("Nom:"), 0, 0);
        grid.add(nomField, 1, 0);
        grid.add(new Label("Référence:"), 0, 1);
        grid.add(referenceField, 1, 1);
        grid.add(new Label("Prix HT:"), 0, 2);
        grid.add(prixField, 1, 2);
        
        dialog.getDialogPane().setContent(grid);
        
        // Convert the result to a PieceDetachee when the add button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ajouterButtonType) {
                try {
                    String nom = nomField.getText();
                    String reference = referenceField.getText();
                    BigDecimal prix = new BigDecimal(prixField.getText().replace(',', '.'));
                    
                    // Validate input
                    if (nom.isEmpty() || reference.isEmpty()) {
                        throw new IllegalArgumentException("Tous les champs sont obligatoires.");
                    }
                    
                    // Save to database
                    int id = sauvegarderNouvellePiece(nom, reference, prix);
                    if (id > 0) {
                        return new PieceDetachee(id, nom, reference, prix);
                    }
                } catch (NumberFormatException e) {
                    showError("Erreur de format", "Le prix doit être un nombre valide.");
                } catch (IllegalArgumentException e) {
                    showError("Erreur de validation", e.getMessage());
                } catch (SQLException e) {
                    showError("Erreur de base de données", e.getMessage());
                }
            }
            return null;
        });
        
        // Show the dialog and process the result
        Optional<PieceDetachee> result = dialog.showAndWait();
        
        result.ifPresent(nouvellePiece -> {
            // Add to the list and refresh the table
            piecesDisponibles.add(nouvellePiece);
            piecesDisponiblesTable.refresh();
            
            // Automatically select and add the new part
            piecesDisponiblesTable.getSelectionModel().select(nouvellePiece);
            ajouterPieceSelectionnee();
        });
    }
    
    private int sauvegarderNouvellePiece(String nom, String reference, BigDecimal prix) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                 "INSERT INTO piece_detachee (nom_piece, reference, prix_ht) VALUES (?, ?, ?)",
                 Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, nom);
            pstmt.setString(2, reference);
            pstmt.setBigDecimal(3, prix);
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("La création de la pièce a échoué, aucune ligne modifiée.");
            }
            
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("La création de la pièce a échoué, aucun ID obtenu.");
                }
            }
        }
    }
    
    private void calculerTotalPiecesHT() {
        if (lblTotalPiecesHT == null) {
            System.err.println("lblTotalPiecesHT est null!");
            return;
        }
        
        BigDecimal total = BigDecimal.ZERO;
        
        for (PieceOrdre piece : piecesSélectionnées) {
            BigDecimal prixTotal = piece.getPrixHT().multiply(new BigDecimal(piece.getQuantite()));
            total = total.add(prixTotal);
        }
        
        // Afficher le total formaté
        lblTotalPiecesHT.setText(String.format("%.2f €", total));
    }
    
    @FXML
    private void rechercherClient(ActionEvent event) {
        if (txtRechercheClient == null) {
            System.err.println("txtRechercheClient est null!");
            return;
        }
        
        String recherche = txtRechercheClient.getText().trim();
        
        if (recherche.isEmpty()) {
            showInfo("Recherche vide", "Veuillez entrer un nom, prénom ou numéro de client.");
            return;
        }
        
        System.out.println("Searching for client: " + recherche);
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            // First, check the client table structure to ensure we're using the correct column names
            try (Statement stmt = conn.createStatement();
                 ResultSet columnsRS = stmt.executeQuery("DESCRIBE client")) {
                
                System.out.println("Client table structure:");
                while (columnsRS.next()) {
                    System.out.println(" - " + columnsRS.getString("Field") + 
                                       " (" + columnsRS.getString("Type") + ")");
                }
            } catch (SQLException e) {
                System.err.println("Error checking client table: " + e.getMessage());
            }
            
            // Now, perform the search with the corrected column names
            String query = "SELECT * FROM client WHERE id = ? OR name LIKE ? OR phoneNumber LIKE ?";
            System.out.println("Executing query: " + query);
            
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                
                // Essayer de convertir en ID si c'est un nombre
                int id = 0;
                try {
                    id = Integer.parseInt(recherche);
                } catch (NumberFormatException e) {
                    // Ce n'est pas un nombre, on laisse id à 0
                }
                
                pstmt.setInt(1, id);
                pstmt.setString(2, "%" + recherche + "%");
                pstmt.setString(3, "%" + recherche + "%");
                
                ResultSet rs = pstmt.executeQuery();
                
                if (rs.next()) {
                    // Client trouvé, remplir les champs
                    clientActuel = new Client(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("address"),
                        rs.getString("phoneNumber")
                    );
                    
                    System.out.println("Client found: " + clientActuel.getName());
                    afficherInfoClient(clientActuel);
                } else {
                    // Client non trouvé, vider les champs
                    System.out.println("No client found matching: " + recherche);
                    showInfo("Client non trouvé", 
                        "Aucun client correspondant à '" + recherche + "' trouvé dans la base de données.");
                    clientActuel = null;
                    viderChampsClient();
                }
                
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur lors de la recherche client", e.getMessage());
        }
    }
    
    private void afficherInfoClient(Client client) {
        if (txtNomClient == null || txtAdresseClient == null || txtTelephoneClient == null) {
            System.err.println("Un ou plusieurs champs client sont null!");
            return;
        }
        
        txtNomClient.setText(client.getName());
        txtAdresseClient.setText(client.getAddress());
        txtTelephoneClient.setText(client.getPhoneNumber());
    }
    
    private void viderChampsClient() {
        if (txtNomClient != null) txtNomClient.setText("");
        if (txtAdresseClient != null) txtAdresseClient.setText("");
        if (txtTelephoneClient != null) txtTelephoneClient.setText("");
    }
    
    @FXML
    private void enregistrerOrdreReparation() {
        // Vérification des champs obligatoires
        if (txtNomClient == null || txtNomClient.getText().trim().isEmpty()) {
            showError("Champ manquant", "Le nom du client est obligatoire.");
            return;
        }
        
        if (txtDescriptionAppareil == null || txtDescriptionAppareil.getText().trim().isEmpty()) {
            showError("Champ manquant", "La description de l'appareil est obligatoire.");
            return;
        }
        
        if (cmbCategorieAppareil == null || cmbCategorieAppareil.getSelectionModel().getSelectedItem() == null) {
            showError("Champ manquant", "Veuillez sélectionner une catégorie d'appareil.");
            return;
        }
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Désactiver le commit automatique pour la transaction
            conn.setAutoCommit(false);
            
            try {
                // 1. Insérer ou mettre à jour le client
                int id = enregistrerClient(conn);
                
                // 2. Insérer l'appareil
                int idAppareil = enregistrerAppareil(conn);
                
                // 3. Créer l'ordre de réparation
                int idOrdre = enregistrerOrdre(conn, id, idAppareil);
                
                // 4. Associer les pièces détachées à l'ordre
                enregistrerPiecesOrdre(conn, idOrdre);
                
                // Tout s'est bien passé, valider la transaction
                conn.commit();
                
                showInfo("Succès", "L'ordre de réparation a été enregistré avec succès.");
                viderFormulaire();
                
            } catch (SQLException e) {
                // En cas d'erreur, annuler toutes les opérations
                conn.rollback();
                throw e;
            } finally {
                // Réactiver le commit automatique
                conn.setAutoCommit(true);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur d'enregistrement", e.getMessage());
        }
    }
    
    private int enregistrerClient(Connection conn) throws SQLException {
        // Si le client existe déjà, le mettre à jour
        if (clientActuel != null) {
            try (PreparedStatement pstmt = conn.prepareStatement(
                "UPDATE client SET name = ?, address = ?, phoneNumber = ? WHERE id = ?")) {
                
                pstmt.setString(1, txtNomClient.getText().trim());
                pstmt.setString(2, txtAdresseClient.getText().trim());
                pstmt.setString(3, txtTelephoneClient.getText().trim());
                pstmt.setInt(4, clientActuel.getId());
                
                pstmt.executeUpdate();
                return clientActuel.getId();
            }
        } else {
          
            		
            		
            		// Sinon, créer un nouveau client
                    try (PreparedStatement pstmt = conn.prepareStatement(
                        "INSERT INTO client (name, address, phoneNumber) VALUES (?, ?, ?)",
                        Statement.RETURN_GENERATED_KEYS)) {
                        
                        pstmt.setString(1, txtNomClient.getText().trim());
                        pstmt.setString(2, txtAdresseClient.getText().trim());
                        pstmt.setString(3, txtTelephoneClient.getText().trim());
                        
                        pstmt.executeUpdate();
                        
                        try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                            if (generatedKeys.next()) {
                                return generatedKeys.getInt(1);
                            } else {
                                throw new SQLException("Échec de la création du client, aucun ID obtenu.");
                            }
                        }
                    }
                }
            }
            
            private int enregistrerAppareil(Connection conn) throws SQLException {
                try (PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT INTO appareil (description, marque, id_categorie) VALUES (?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                    
                    pstmt.setString(1, txtDescriptionAppareil.getText().trim());
                    pstmt.setString(2, txtMarqueAppareil.getText().trim());
                    pstmt.setInt(3, cmbCategorieAppareil.getValue().getIdCategorie());
                    
                    pstmt.executeUpdate();
                    
                    try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            return generatedKeys.getInt(1);
                        } else {
                            throw new SQLException("Échec de la création de l'appareil, aucun ID obtenu.");
                        }
                    }
                }
            }
            
            private int enregistrerOrdre(Connection conn, int id, int idAppareil) throws SQLException {
                try (PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT INTO ordre_reparation (nb_heures_mo, informations_supp, id, id_appareil) VALUES (?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                    
                    pstmt.setInt(1, spinnerHeuresMO.getValue());
                    pstmt.setString(2, txtInfosSupplementaires.getText().trim());
                    pstmt.setInt(3, id);
                    pstmt.setInt(4, idAppareil);
                    
                    pstmt.executeUpdate();
                    
                    try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            return generatedKeys.getInt(1);
                        } else {
                            throw new SQLException("Échec de la création de l'ordre, aucun ID obtenu.");
                        }
                    }
                }
            }
            
            private void enregistrerPiecesOrdre(Connection conn, int idOrdre) throws SQLException {
                if (piecesSélectionnées.isEmpty()) {
                    return; // Pas de pièces à enregistrer
                }
                
                try (PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT INTO ordre_piece (id_ordre, id_piece, quantite) VALUES (?, ?, ?)")) {
                    
                    for (PieceOrdre piece : piecesSélectionnées) {
                        pstmt.setInt(1, idOrdre);
                        pstmt.setInt(2, piece.getIdPiece());
                        pstmt.setInt(3, piece.getQuantite());
                        pstmt.addBatch();
                    }
                    
                    pstmt.executeBatch();
                }
            }
            
            private void viderFormulaire() {
                // Vider les champs client
                clientActuel = null;
                if (txtRechercheClient != null) txtRechercheClient.clear();
                viderChampsClient();
                
                // Vider les champs appareil
                if (txtDescriptionAppareil != null) txtDescriptionAppareil.clear();
                if (txtMarqueAppareil != null) txtMarqueAppareil.clear();
                if (cmbCategorieAppareil != null) cmbCategorieAppareil.getSelectionModel().clearSelection();
                
                // Vider les champs réparation
                if (spinnerHeuresMO != null) spinnerHeuresMO.getValueFactory().setValue(1);
                if (txtInfosSupplementaires != null) txtInfosSupplementaires.clear();
                
                // Vider les pièces sélectionnées
                piecesSélectionnées.clear();
                calculerTotalPiecesHT();
            }
            
            @FXML
            private void annuler() {
                viderFormulaire();
            }
            
            // Méthodes d'affichage des messages
            private void showInfo(String titre, String message) {
                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setTitle(titre);
                alert.setHeaderText(null);
                alert.setContentText(message);
                alert.showAndWait();
            }
            
            private void showError(String titre, String message) {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle(titre);
                alert.setHeaderText(null);
                alert.setContentText(message);
                alert.showAndWait();
            }
        }