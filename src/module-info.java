module tp4 {
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.base;
    requires javafx.fxml;
    requires java.sql;
    requires transitive mysql.connector.java;
    
    // Ouvrir le package application à javafx.fxml ET à javafx.base
    opens application to javafx.graphics, javafx.fxml, javafx.base;
}