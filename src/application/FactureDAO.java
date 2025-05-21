package application;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FactureDAO {
    
    private final DatabaseConnection dbConnection;
    
    public FactureDAO() {
        dbConnection = new DatabaseConnection();
    }
    
    public List<Map<String, Object>> getAppareilsForClient(int clientId) {
        List<Map<String, Object>> result = new ArrayList<>();
        
        // Based on your schema, this is the correct SQL
        String sql = "SELECT a.* FROM appareil a " +
                     "JOIN ordre_reparation o ON a.id_appareil = o.id_appareil " +
                     "WHERE o.id = ?";
            
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, clientId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Map<String, Object> appareil = new HashMap<>();
                appareil.put("id", rs.getInt("id_appareil"));
                appareil.put("description", rs.getString("description"));
                appareil.put("marque", rs.getString("marque"));
                appareil.put("categorieId", rs.getInt("id_categorie"));
                
                // Get category name from category table
                try (PreparedStatement catStmt = conn.prepareStatement(
                         "SELECT nom_categorie FROM categorie WHERE id_categorie = ?")) {
                    catStmt.setInt(1, rs.getInt("id_categorie"));
                    ResultSet catRs = catStmt.executeQuery();
                    if (catRs.next()) {
                        appareil.put("categorie", catRs.getString("nom_categorie"));
                    } else {
                        appareil.put("categorie", "Unknown");
                    }
                }
                
                result.add(appareil);
            }
            
        } catch (SQLException e) {
            System.err.println("Error loading client devices: " + e.getMessage());
            e.printStackTrace();
        }
        
        return result;
    }
    
    public List<Map<String, Object>> getOrdresForAppareil(int appareilId) {
        List<Map<String, Object>> result = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT * FROM ordre_reparation WHERE id_appareil = ?")) {
            
            stmt.setInt(1, appareilId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Map<String, Object> ordre = new HashMap<>();
                ordre.put("id", rs.getInt("id_ordre"));
                ordre.put("heures", rs.getInt("nb_heures_mo"));
                ordre.put("infos", rs.getString("informations_supp"));
                
                result.add(ordre);
            }
            
        } catch (SQLException e) {
            System.err.println("Error loading repair orders: " + e.getMessage());
            e.printStackTrace();
        }
        
        return result;
    }
    
    public List<PieceDetachee> getPiecesForOrdre(int ordreId) {
        List<PieceDetachee> result = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT p.id_piece, p.nom_piece, p.reference, p.prix_ht " +
                 "FROM piece_detachee p " +
                 "JOIN ordre_piece op ON p.id_piece = op.id_piece " +
                 "WHERE op.id_ordre = ?")) {
            
            stmt.setInt(1, ordreId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                int id = rs.getInt("id_piece");
                String nom = rs.getString("nom_piece");
                String ref = rs.getString("reference");
                BigDecimal prix = rs.getBigDecimal("prix_ht");
                
                PieceDetachee piece = new PieceDetachee(id, nom, ref, prix);
                result.add(piece);
            }
            
        } catch (SQLException e) {
            System.err.println("Error loading parts: " + e.getMessage());
            e.printStackTrace();
        }
        
        return result;
    }
    
    public double getCategorieRate(int categorieId) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT tarif FROM categorie WHERE id_categorie = ?")) {
            
            stmt.setInt(1, categorieId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getDouble("tarif");
            }
            
        } catch (SQLException e) {
            System.err.println("Error loading category rate: " + e.getMessage());
            e.printStackTrace();
        }
        
        return 0.0; // Default if not found
    }
    
    // Add a debug method to help with troubleshooting
    public String debugTable(String tableName) {
        StringBuilder result = new StringBuilder();
        result.append("Table: ").append(tableName).append("\n");
        result.append("----------------------------------------\n");
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM " + tableName + " LIMIT 5")) {
            
            ResultSet rs = stmt.executeQuery();
            int colCount = rs.getMetaData().getColumnCount();
            
            // Print column names
            for (int i = 1; i <= colCount; i++) {
                result.append(rs.getMetaData().getColumnName(i)).append("\t");
            }
            result.append("\n");
            
            // Print data
            while (rs.next()) {
                for (int i = 1; i <= colCount; i++) {
                    String val = rs.getString(i);
                    result.append(val != null ? val : "NULL").append("\t");
                }
                result.append("\n");
            }
            
        } catch (SQLException e) {
            result.append("Error: ").append(e.getMessage());
        }
        
        return result.toString();
    }
}