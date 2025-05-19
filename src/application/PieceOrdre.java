package application;

import java.math.BigDecimal;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class PieceOrdre {
    private final IntegerProperty idPiece;
    private final StringProperty nomPiece;
    private final ObjectProperty<BigDecimal> prixHT;
    private final IntegerProperty quantite;
    
    public PieceOrdre(int idPiece, String nomPiece, BigDecimal prixHT, int quantite) {
        this.idPiece = new SimpleIntegerProperty(idPiece);
        this.nomPiece = new SimpleStringProperty(nomPiece);
        this.prixHT = new SimpleObjectProperty<>(prixHT);
        this.quantite = new SimpleIntegerProperty(quantite);
    }
    
    // Getters et Setters
    public int getIdPiece() {
        return idPiece.get();
    }
    
    public void setIdPiece(int idPiece) {
        this.idPiece.set(idPiece);
    }
    
    public IntegerProperty idPieceProperty() {
        return idPiece;
    }
    
    public String getNomPiece() {
        return nomPiece.get();
    }
    
    public void setNomPiece(String nomPiece) {
        this.nomPiece.set(nomPiece);
    }
    
    public StringProperty nomPieceProperty() {
        return nomPiece;
    }
    
    public BigDecimal getPrixHT() {
        return prixHT.get();
    }
    
    public void setPrixHT(BigDecimal prixHT) {
        this.prixHT.set(prixHT);
    }
    
    public ObjectProperty<BigDecimal> prixHTProperty() {
        return prixHT;
    }
    
    public int getQuantite() {
        return quantite.get();
    }
    
    public void setQuantite(int quantite) {
        this.quantite.set(quantite);
    }
    
    public IntegerProperty quantiteProperty() {
        return quantite;
    }
    
    // Méthode pour calculer le prix total
    public BigDecimal getPrixTotal() {
        return prixHT.get().multiply(new BigDecimal(quantite.get()));
    }
}
