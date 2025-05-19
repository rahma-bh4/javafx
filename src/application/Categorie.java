package application;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Categorie {
    private final IntegerProperty idCategorie;
    private final StringProperty nomCategorie;
    
    public Categorie(int idCategorie, String nomCategorie) {
        this.idCategorie = new SimpleIntegerProperty(idCategorie);
        this.nomCategorie = new SimpleStringProperty(nomCategorie);
    }
    
    // Getters et Setters
    public int getIdCategorie() {
        return idCategorie.get();
    }
    
    public void setIdCategorie(int idCategorie) {
        this.idCategorie.set(idCategorie);
    }
    
    public IntegerProperty idCategorieProperty() {
        return idCategorie;
    }
    
    public String getNomCategorie() {
        return nomCategorie.get();
    }
    
    public void setNomCategorie(String nomCategorie) {
        this.nomCategorie.set(nomCategorie);
    }
    
    public StringProperty nomCategorieProperty() {
        return nomCategorie;
    }
    
    @Override
    public String toString() {
        return nomCategorie.get();
    }
}
