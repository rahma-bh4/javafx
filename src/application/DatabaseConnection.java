package application;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    
    // Configuration de la base de données
    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/reparation?serverTimezone=UTC";
    private static final String JDBC_USER = "root";
    private static final String JDBC_PASSWORD = ""; // À modifier selon votre configuration
    
    /**
     * Établit et renvoie une connexion à la base de données
     * @return Objet Connection
     * @throws SQLException En cas d'erreur de connexion
     */
    public static Connection getConnection() throws SQLException {
        try {
            // Charger le pilote JDBC
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // Établir la connexion
            return DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD);
            
        } catch (ClassNotFoundException e) {
            throw new SQLException("Pilote MySQL introuvable", e);
        }
    }
    
    /**
     * Ferme une connexion à la base de données
     * @param connection La connexion à fermer
     */
    public static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                System.err.println("Erreur lors de la fermeture de la connexion : " + e.getMessage());
            }
        }
    }
    
    /**
     * Teste la connexion à la base de données
     * @return true si la connexion a réussi, false sinon
     */
    public static boolean testConnection() {
        Connection conn = null;
        try {
            conn = getConnection();
            return conn != null;
        } catch (SQLException e) {
            System.err.println("Erreur de connexion à la base de données : " + e.getMessage());
            return false;
        } finally {
            closeConnection(conn);
        }
    }
}