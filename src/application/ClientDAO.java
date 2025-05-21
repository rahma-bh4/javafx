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
        String query = "SELECT * FROM client WHERE name LIKE ? ORDER BY name";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, "%" + name + "%");
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                int id = rs.getInt("id");
                String clientName = rs.getString("name");
                String address = rs.getString("address");
                // Changed from phone_number to phoneNumber
                String phoneNumber = rs.getString("phoneNumber");
                
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
        String query = "SELECT * FROM client WHERE id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                String name = rs.getString("name");
                String address = rs.getString("address");
                // Changed from phone_number to phoneNumber
                String phoneNumber = rs.getString("phoneNumber");
                
                return new Client(id, name, address, phoneNumber);
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting client by ID: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    public boolean updateClient(Client client) {
        String query = "UPDATE client SET name = ?, address = ?, phoneNumber = ? WHERE id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, client.getName());
            stmt.setString(2, client.getAddress());
            stmt.setString(3, client.getPhoneNumber());
            stmt.setInt(4, client.getId());
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating client: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Deletes a client from the database
     * @param clientId The ID of the client to delete
     * @return true if the deletion was successful, false otherwise
     */
    public boolean deleteClient(int clientId) {
        // First, check if there are any ordre_reparation records for this client
        if (hasRelatedOrders(clientId)) {
            return false; // Cannot delete a client with related orders
        }
        
        // If no related orders, proceed with deletion
        String query = "DELETE FROM client WHERE id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, clientId);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error deleting client: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    private boolean hasRelatedOrders(int clientId) {
        String query = "SELECT COUNT(*) FROM ordre_reparation WHERE id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, clientId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            
        } catch (SQLException e) {
            System.err.println("Error checking related orders: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    public List<Client> getAllClients() {
        List<Client> clients = new ArrayList<>();
        String query = "SELECT * FROM client ORDER BY name";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String address = rs.getString("address");
                // Changed from phone_number to phoneNumber
                String phoneNumber = rs.getString("phoneNumber");
                
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