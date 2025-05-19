package application;

import java.math.BigDecimal;
import java.math.BigDecimal;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class PieceDetachee {
    private final IntegerProperty idPiece;
    private final StringProperty nomPiece;
    private final StringProperty reference;
    private final ObjectProperty<BigDecimal> prixHT;
    
    public PieceDetachee(int idPiece, String nomPiece, String reference, BigDecimal prixHT) {
        this.idPiece = new SimpleIntegerProperty(idPiece);
        this.nomPiece = new SimpleStringProperty(nomPiece);
        this.reference = new SimpleStringProperty(reference);
        this.prixHT = new SimpleObjectProperty<>(prixHT);
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
    
    public String getReference() {
        return reference.get();
    }
    
    public void setReference(String reference) {
        this.reference.set(reference);
    }
    
    public StringProperty referenceProperty() {
        return reference;
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
    
    @Override
    public String toString() {
        return nomPiece.get();
    }
}