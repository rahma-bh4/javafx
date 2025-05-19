package application;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ClientDAO {
    
    private final DatabaseConnection dbConnection;
    
    public ClientDAO() {
        dbConnection = new DatabaseConnection();
    }
    
    public List<Client> searchClientsByName(String name) {
        List<Client> clients = new ArrayList<>();
        String query = "SELECT * FROM clients WHERE name LIKE ? ORDER BY name";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, "%" + name + "%");
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                int id = rs.getInt("id");
                String clientName = rs.getString("name");
                String address = rs.getString("address");
                String phoneNumber = rs.getString("phone_number");
                
                Client client = new Client(id, clientName, address, phoneNumber);
                clients.add(client);
            }
            
        } catch (SQLException e) {
            System.err.println("Error searching for clients: " + e.getMessage());
            e.printStackTrace();
        }
        
        return clients;
    }
    
    public Client getClientById(int id) {
        String query = "SELECT * FROM clients WHERE id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                String name = rs.getString("name");
                String address = rs.getString("address");
                String phoneNumber = rs.getString("phone_number");
                
                return new Client(id, name, address, phoneNumber);
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting client by ID: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    public List<Client> getAllClients() {
        List<Client> clients = new ArrayList<>();
        String query = "SELECT * FROM clients ORDER BY name";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String address = rs.getString("address");
                String phoneNumber = rs.getString("phone_number");
                
                Client client = new Client(id, name, address, phoneNumber);
                clients.add(client);
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting all clients: " + e.getMessage());
            e.printStackTrace();
        }
        
        return clients;
    }
}