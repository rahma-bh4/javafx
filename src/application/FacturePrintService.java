package application;

import javafx.print.*;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.transform.Scale;
import javafx.stage.FileChooser;
import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

// Import for PDF export
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import java.math.BigDecimal;
import java.util.List;

public class FacturePrintService {
    
    /**
     * Print the invoice node
     * @param nodeToPrint The JavaFX node to print
     */
    public static void printNode(Node nodeToPrint) {
        PrinterJob job = PrinterJob.createPrinterJob();
        
        if (job != null) {
            // Show printer dialog
            boolean proceeded = job.showPrintDialog(nodeToPrint.getScene().getWindow());
            
            if (proceeded) {
                // Calculate scale to fit paper
                PageLayout pageLayout = job.getJobSettings().getPageLayout();
                double scaleX = pageLayout.getPrintableWidth() / nodeToPrint.getBoundsInParent().getWidth();
                double scaleY = pageLayout.getPrintableHeight() / nodeToPrint.getBoundsInParent().getHeight();
                double scale = Math.min(scaleX, scaleY);
                
                // Apply scaling
                Scale scaling = new Scale(scale, scale);
                nodeToPrint.getTransforms().add(scaling);
                
                // Print
                boolean success = job.printPage(nodeToPrint);
                
                // Remove scaling transform
                nodeToPrint.getTransforms().remove(scaling);
                
                if (success) {
                    job.endJob();
                }
            }
        } else {
            showAlert("No printer found", "No printer was found on the system.");
        }
    }
    
    /**
     * Export invoice data to PDF
     * @param ordreId The order ID
     * @param client The client data
     * @param appareilDesc The device description
     * @param appareilMarque The device brand
     * @param categorieNom The category name
     * @param tarifCategorie The category rate
     * @param heuresMO Hours of labor
     * @param tarifHoraire Hourly rate
     * @param infosSupp Additional information
     * @param pieces Parts list
     */
    public static void exportToPDF(
            int ordreId, 
            Client client, 
            String appareilDesc, 
            String appareilMarque, 
            String categorieNom, 
            double tarifCategorie,
            int heuresMO,
            double tarifHoraire,
            String infosSupp,
            List<PieceDetachee> pieces) {
        
        // Set up file chooser
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save PDF File");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        fileChooser.setInitialFileName("Facture_" + ordreId + "_" + 
            LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".pdf");
        
        // Show save dialog
        File file = fileChooser.showSaveDialog(null);
        
        if (file != null) {
            try {
                // Create document
                Document document = new Document(PageSize.A4);
                PdfWriter.getInstance(document, new FileOutputStream(file));
                document.open();
                
                // Add title with logo (if needed)
                Paragraph title = new Paragraph("FACTURE N° F-" + ordreId + "-" + LocalDate.now().getYear(),
                        FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.BLACK));
                title.setAlignment(Element.ALIGN_CENTER);
                document.add(title);
                
                // Add date
                Paragraph date = new Paragraph("Date: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                        FontFactory.getFont(FontFactory.HELVETICA, 12, BaseColor.BLACK));
                date.setAlignment(Element.ALIGN_RIGHT);
                document.add(date);
                
                document.add(Chunk.NEWLINE);
                
                // Create client information table
                PdfPTable clientTable = new PdfPTable(2);
                clientTable.setWidthPercentage(100);
                
                // Client header
                PdfPCell clientHeader = new PdfPCell(new Phrase("Informations Client", 
                        FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14)));
                clientHeader.setColspan(2);
                clientHeader.setBackgroundColor(new BaseColor(240, 240, 240));
                clientHeader.setBorder(Rectangle.NO_BORDER);
                clientHeader.setPadding(5);
                clientTable.addCell(clientHeader);
                
                // Client data
                addLabelValueRow(clientTable, "Nom:", client.getName());
                addLabelValueRow(clientTable, "Adresse:", client.getAddress());
                addLabelValueRow(clientTable, "Téléphone:", client.getPhoneNumber());
                
                document.add(clientTable);
                document.add(Chunk.NEWLINE);
                
                // Create device information table
                PdfPTable deviceTable = new PdfPTable(2);
                deviceTable.setWidthPercentage(100);
                
                // Device header
                PdfPCell deviceHeader = new PdfPCell(new Phrase("Informations Appareil", 
                        FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14)));
                deviceHeader.setColspan(2);
                deviceHeader.setBackgroundColor(new BaseColor(240, 240, 240));
                deviceHeader.setBorder(Rectangle.NO_BORDER);
                deviceHeader.setPadding(5);
                deviceTable.addCell(deviceHeader);
                
                // Device data
                addLabelValueRow(deviceTable, "Description:", appareilDesc);
                addLabelValueRow(deviceTable, "Marque:", appareilMarque);
                addLabelValueRow(deviceTable, "Catégorie:", categorieNom);
                addLabelValueRow(deviceTable, "Tarif catégorie:", String.format("%.2f €", tarifCategorie));
                
                document.add(deviceTable);
                document.add(Chunk.NEWLINE);
                
                // Create repair information table
                PdfPTable repairTable = new PdfPTable(2);
                repairTable.setWidthPercentage(100);
                
                // Repair header
                PdfPCell repairHeader = new PdfPCell(new Phrase("Détails Réparation", 
                        FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14)));
                repairHeader.setColspan(2);
                repairHeader.setBackgroundColor(new BaseColor(240, 240, 240));
                repairHeader.setBorder(Rectangle.NO_BORDER);
                repairHeader.setPadding(5);
                repairTable.addCell(repairHeader);
                
                // Repair data
                addLabelValueRow(repairTable, "Nombre d'heures:", String.valueOf(heuresMO));
                double coutMO = heuresMO * tarifHoraire;
                addLabelValueRow(repairTable, "Coût main-d'œuvre:", 
                        String.format("%.2f € (%.2f €/h)", coutMO, tarifHoraire));
                addLabelValueRow(repairTable, "Informations:", infosSupp);
                
                document.add(repairTable);
                document.add(Chunk.NEWLINE);
                
                // Parts table
                if (pieces != null && !pieces.isEmpty()) {
                    PdfPTable partsHeaderTable = new PdfPTable(1);
                    partsHeaderTable.setWidthPercentage(100);
                    
                    PdfPCell partsHeaderCell = new PdfPCell(new Phrase("Pièces Détachées", 
                            FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14)));
                    partsHeaderCell.setBackgroundColor(new BaseColor(240, 240, 240));
                    partsHeaderCell.setBorder(Rectangle.NO_BORDER);
                    partsHeaderCell.setPadding(5);
                    partsHeaderTable.addCell(partsHeaderCell);
                    
                    document.add(partsHeaderTable);
                    
                    PdfPTable partsTable = new PdfPTable(2);
                    partsTable.setWidthPercentage(100);
                    float[] columnWidths = {80f, 20f};
                    partsTable.setWidths(columnWidths);
                    
                    // Header row
                    PdfPCell headerPiece = new PdfPCell(new Phrase("Nom de la pièce", 
                            FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));
                    headerPiece.setBackgroundColor(new BaseColor(220, 220, 220));
                    headerPiece.setPadding(5);
                    partsTable.addCell(headerPiece);
                    
                    PdfPCell headerPrix = new PdfPCell(new Phrase("Prix HT (€)", 
                            FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));
                    headerPrix.setBackgroundColor(new BaseColor(220, 220, 220));
                    headerPrix.setPadding(5);
                    partsTable.addCell(headerPrix);
                    
                    // Parts data
                    BigDecimal totalPieces = BigDecimal.ZERO;
                    for (PieceDetachee piece : pieces) {
                        partsTable.addCell(new Phrase(piece.getNomPiece()));
                        partsTable.addCell(new Phrase(String.format("%.2f", piece.getPrixHT())));
                        totalPieces = totalPieces.add(piece.getPrixHT());
                    }
                    
                    document.add(partsTable);
                    
                    // Total parts
                    PdfPTable totalPartsTable = new PdfPTable(2);
                    totalPartsTable.setWidthPercentage(100);
                    totalPartsTable.setWidths(columnWidths);
                    
                    PdfPCell totalLabel = new PdfPCell(new Phrase("Total pièces:", 
                            FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));
                    totalLabel.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    totalLabel.setBorder(Rectangle.NO_BORDER);
                    totalPartsTable.addCell(totalLabel);
                    
                    PdfPCell totalValue = new PdfPCell(new Phrase(String.format("%.2f €", totalPieces),
                            FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));
                    totalValue.setBorder(Rectangle.NO_BORDER);
                    totalPartsTable.addCell(totalValue);
                    
                    document.add(totalPartsTable);
                    document.add(Chunk.NEWLINE);
                    
                    // Summary
                    PdfPTable summaryTable = new PdfPTable(2);
                    summaryTable.setWidthPercentage(100);
                    
                    // Summary header
                    PdfPCell summaryHeader = new PdfPCell(new Phrase("Récapitulatif", 
                            FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14)));
                    summaryHeader.setColspan(2);
                    summaryHeader.setBackgroundColor(new BaseColor(240, 240, 240));
                    summaryHeader.setBorder(Rectangle.NO_BORDER);
                    summaryHeader.setPadding(5);
                    summaryTable.addCell(summaryHeader);
                    
                    // Summary data
                    addLabelValueRow(summaryTable, "Tarif catégorie:", String.format("%.2f €", tarifCategorie));
                    addLabelValueRow(summaryTable, "Coût main-d'œuvre:", String.format("%.2f €", coutMO));
                    addLabelValueRow(summaryTable, "Total pièces:", String.format("%.2f €", totalPieces));
                    
                    // Total
                    double total = tarifCategorie + coutMO + totalPieces.doubleValue();
                    
                    PdfPCell totalLineLabel = new PdfPCell(new Phrase("TOTAL:", 
                            FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14)));
                    totalLineLabel.setBorder(Rectangle.TOP);
                    totalLineLabel.setPaddingTop(5);
                    summaryTable.addCell(totalLineLabel);
                    
                    PdfPCell totalLineValue = new PdfPCell(new Phrase(String.format("%.2f €", total),
                            FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14)));
                    totalLineValue.setBorder(Rectangle.TOP);
                    totalLineValue.setPaddingTop(5);
                    summaryTable.addCell(totalLineValue);
                    
                    document.add(summaryTable);
                }
                
                document.close();
                
                showAlert("Succès", "Le fichier PDF a été créé avec succès.");
                
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Erreur", "Une erreur est survenue lors de la création du PDF: " + e.getMessage());
            }
        }
    }
    
    private static void addLabelValueRow(PdfPTable table, String label, String value) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, 
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPadding(5);
        table.addCell(labelCell);
        
        PdfPCell valueCell = new PdfPCell(new Phrase(value, 
                FontFactory.getFont(FontFactory.HELVETICA, 12)));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setPadding(5);
        table.addCell(valueCell);
    }
    
    private static void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}