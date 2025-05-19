package application;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Client {
    private final IntegerProperty id;
    private final StringProperty name;
    private final StringProperty address;
    private final StringProperty phoneNumber;
    
    public Client(int id, String name, String address, String phoneNumber) {
        this.id = new SimpleIntegerProperty(id);
        this.name = new SimpleStringProperty(name);
        this.address = new SimpleStringProperty(address);
        this.phoneNumber = new SimpleStringProperty(phoneNumber);
    }
    
    // Getters and Setters for id
    public int getId() {
        return id.get();
    }
    
    public void setId(int id) {
        this.id.set(id);
    }
    
    public IntegerProperty idProperty() {
        return id;
    }
    
    // Getters and Setters for name
    public String getName() {
        return name.get();
    }
    
    public void setName(String name) {
        this.name.set(name);
    }
    
    public StringProperty nameProperty() {
        return name;
    }
    
    // Getters and Setters for address
    public String getAddress() {
        return address.get();
    }
    
    public void setAddress(String address) {
        this.address.set(address);
    }
    
    public StringProperty addressProperty() {
        return address;
    }
    
    // Getters and Setters for phoneNumber
    public String getPhoneNumber() {
        return phoneNumber.get();
    }
    
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber.set(phoneNumber);
    }
    
    public StringProperty phoneNumberProperty() {
        return phoneNumber;
    }
    
    @Override
    public String toString() {
        return name.get();
    }
}
