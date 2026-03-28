package com.example.SenBibliotheque.entity.controller.modules;

import com.example.SenBibliotheque.entity.Adherent;
import com.example.SenBibliotheque.entity.Emprunt;
import com.example.SenBibliotheque.entity.Livre;
import com.example.SenBibliotheque.service.LivreService;
import com.example.SenBibliotheque.service.AdherentService;
import com.example.SenBibliotheque.service.EmpruntService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.io.font.constants.StandardFonts;
import javafx.stage.FileChooser;
import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ReportsModuleController {

    private static final Logger logger = LoggerFactory.getLogger(ReportsModuleController.class);

    @FXML
    private ComboBox<String> periodCombo;
    @FXML
    private Label totalBorrowsLabel, totalPenaltiesLabel, mostBorrowedLabel;
    @FXML
    private Label activeMembers, suspendedMembers, mostActiveMember;
    @FXML
    private Label totalBooks, availableBooks, circulationRate;
    @FXML
    private TableView<MemberReport> membersReportTable;
    @FXML
    private TableView<BookReport> booksReportTable;

    private LivreService livreService;
    private AdherentService adherentService;
    private EmpruntService empruntService;

    private ObservableList<MemberReport> memberReports;
    private ObservableList<BookReport> bookReports;

    @FXML
    public void initialize() {
        try {
            livreService = new LivreService();
            adherentService = new AdherentService();
            empruntService = new EmpruntService();

            memberReports = FXCollections.observableArrayList();
            bookReports = FXCollections.observableArrayList();

            // Configurer les tableaux
            configurerTableauAdherents();
            configurerTableauLivres();

            periodCombo.getSelectionModel().selectFirst();
            periodCombo.setOnAction(e -> chargerRapports());

            chargerRapports();

            logger.info("✅ ReportsModuleController initialisé");
        } catch (Exception e) {
            logger.error("❌ Erreur lors de l'initialisation du module rapports", e);
        }
    }

    // ===== CONFIGURATION DES TABLEAUX =====
    private void configurerTableauAdherents() {
        // Colonne Matricule
        TableColumn<MemberReport, String> matriculeCol = new TableColumn<>("Matricule");
        matriculeCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().matricule));
        matriculeCol.setPrefWidth(100);

        // Colonne Nom
        TableColumn<MemberReport, String> nomCol = new TableColumn<>("Nom");
        nomCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().nom));
        nomCol.setPrefWidth(100);

        // Colonne Emprunts
        TableColumn<MemberReport, Integer> empruntsCol = new TableColumn<>("Emprunts");
        empruntsCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().emprunts));
        empruntsCol.setPrefWidth(80);

        // Colonne Retards
        TableColumn<MemberReport, Integer> retardsCol = new TableColumn<>("Retards");
        retardsCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().retards));
        retardsCol.setPrefWidth(80);

        // Colonne Pénalités
        TableColumn<MemberReport, String> penalitesCol = new TableColumn<>("Pénalités (FCFA)");
        penalitesCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        String.format("%.2f", cellData.getValue().penalites)));
        penalitesCol.setPrefWidth(120);

        membersReportTable.getColumns().clear();
        membersReportTable.getColumns().addAll(matriculeCol, nomCol, empruntsCol, retardsCol, penalitesCol);
        membersReportTable.setItems(memberReports);
    }

    private void configurerTableauLivres() {
        // Colonne Titre
        TableColumn<BookReport, String> titreCol = new TableColumn<>("Titre");
        titreCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().titre));
        titreCol.setPrefWidth(150);

        // Colonne Auteur
        TableColumn<BookReport, String> auteurCol = new TableColumn<>("Auteur");
        auteurCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().auteur));
        auteurCol.setPrefWidth(120);

        // Colonne Total
        TableColumn<BookReport, Integer> totalCol = new TableColumn<>("Total");
        totalCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().total));
        totalCol.setPrefWidth(60);

        // Colonne Emprunts
        TableColumn<BookReport, Integer> empruntsCol = new TableColumn<>("Emprunts");
        empruntsCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().emprunts));
        empruntsCol.setPrefWidth(80);

        // Colonne Disponible
        TableColumn<BookReport, Integer> disponibleCol = new TableColumn<>("Disponible");
        disponibleCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().disponible));
        disponibleCol.setPrefWidth(80);

        booksReportTable.getColumns().clear();
        booksReportTable.getColumns().addAll(titreCol, auteurCol, totalCol, empruntsCol, disponibleCol);
        booksReportTable.setItems(bookReports);
    }

    // ===== CHARGEMENT DES RAPPORTS =====
    private void chargerRapports() {
        try {
            // ===== STATISTIQUES GÉNÉRALES =====
            List<Emprunt> tousLesEmprunts = empruntService.obtenirTous();
            List<Emprunt> empruntsEnCours = empruntService.obtenirEnCours();
            List<Emprunt> empruntsEnRetard = empruntService.obtenirEnRetard();

            totalBorrowsLabel.setText(String.valueOf(empruntsEnCours.size()));

            // Pénalités totales
            double totalPenalites = empruntsEnRetard.stream()
                    .mapToDouble(e -> {
                        e.calculerPenalite();
                        return e.getPenalite();
                    })
                    .sum();

            totalPenaltiesLabel.setText(String.format("%.2f FCFA", totalPenalites));

            // Livre le plus emprunté
            String mostBorrowed = tousLesEmprunts.stream()
                    .collect(Collectors.groupingBy(
                            e -> e.getLivre().getTitre(),
                            Collectors.counting()
                    ))
                    .entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse("N/A");

            mostBorrowedLabel.setText(mostBorrowed);

            logger.info("✅ Statistiques générales chargées");

            // ===== RAPPORT ADHÉRENTS =====
            List<Adherent> tousLesAdherents = adherentService.obtenirTous();
            long activeMembersCount = adherentService.compterActifs();

            activeMembers.setText(String.valueOf(activeMembersCount));
            suspendedMembers.setText(String.valueOf(tousLesAdherents.size() - activeMembersCount));

            // Adhérent le plus actif
            String mostActive = tousLesAdherents.stream()
                    .max((a1, a2) -> Integer.compare(
                            a1.getEmprunts().size(),
                            a2.getEmprunts().size()
                    ))
                    .map(a -> a.getPrenom() + " " + a.getNom())
                    .orElse("N/A");

            mostActiveMember.setText(mostActive);

            // Tableau des adhérents
            memberReports.clear();
            tousLesAdherents.forEach(adherent -> {
                long empruntsAd = adherent.getEmprunts().stream()
                        .filter(e -> e.getDateRetourEffective() == null)
                        .count();

                long retardsAd = adherent.getEmprunts().stream()
                        .filter(e -> e.isEnRetard())
                        .count();

                double penalitesAd = adherent.getEmprunts().stream()
                        .mapToDouble(e -> {
                            e.calculerPenalite();
                            return e.getPenalite();
                        })
                        .sum();

                memberReports.add(new MemberReport(
                        adherent.getMatricule(),
                        adherent.getPrenom() + " " + adherent.getNom(),
                        (int) empruntsAd,
                        (int) retardsAd,
                        penalitesAd
                ));
            });

            logger.info("✅ Rapport adhérents chargé: " + memberReports.size() + " adhérents");

            // ===== RAPPORT LIVRES =====
            List<Livre> tousLesLivres = livreService.obtenirTous();
            long totalBooksCount = livreService.compterTous();
            long availableBooksCount = livreService.compterDisponibles();

            totalBooks.setText(String.valueOf(totalBooksCount));
            availableBooks.setText(String.valueOf(availableBooksCount));

            // Taux de circulation
            if (totalBooksCount > 0) {
                double rate = (empruntsEnCours.size() * 100.0) / totalBooksCount;
                circulationRate.setText(String.format("%.1f%%", rate));
            }

            // Tableau des livres
            bookReports.clear();
            tousLesLivres.forEach(livre -> {
                long empruntsLivre = tousLesEmprunts.stream()
                        .filter(e -> e.getLivre().getId().equals(livre.getId()))
                        .count();

                bookReports.add(new BookReport(
                        livre.getTitre(),
                        livre.getAuteur(),
                        livre.getNombreExemplaires(),
                        (int) empruntsLivre,
                        livre.getDisponible()
                ));
            });

            logger.info("✅ Rapport livres chargé: " + bookReports.size() + " livres");

        } catch (Exception e) {
            logger.error("❌ Erreur lors du chargement des rapports", e);
        }
    }

    @FXML
    private void exporterPDF() {
        try {
            // Créer un FileChooser pour choisir l'emplacement de sauvegarde
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Exporter les rapports en PDF");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
            );

            // Définir le nom du fichier par défaut avec la date actuelle
            LocalDate today = LocalDate.now();
            fileChooser.setInitialFileName("Rapports_Bibliotheque_" + today + ".pdf");

            File selectedFile = fileChooser.showSaveDialog(null);

            if (selectedFile != null) {
                logger.info("📥 Export PDF en cours vers: " + selectedFile.getAbsolutePath());

                // Créer le PDF
                PdfWriter writer = new PdfWriter(selectedFile);
                PdfDocument pdf = new PdfDocument(writer);
                Document document = new Document(pdf);

                // Style et fonts
                PdfFont titleFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
                PdfFont headerFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
                PdfFont normalFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);

                // Titre principal
                Paragraph title = new Paragraph("RAPPORT D'ACTIVITÉ - BIBLIOTHÈQUE MUNICIPALE")
                        .setFont(titleFont)
                        .setFontSize(16)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setMarginBottom(10);
                document.add(title);

                // Date du rapport
                Paragraph dateReport = new Paragraph("Date : " + LocalDateTime.now().format(
                        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")))
                        .setFont(normalFont)
                        .setFontSize(10)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setMarginBottom(20);
                document.add(dateReport);

                // ===== SECTION STATISTIQUES GÉNÉRALES =====
                document.add(new Paragraph("1. STATISTIQUES GÉNÉRALES")
                        .setFont(headerFont)
                        .setFontSize(12)
                        .setMarginTop(15)
                        .setMarginBottom(10));

                Table statsTable = new Table(2);
                statsTable.setWidth(UnitValue.createPercentValue(100));
                statsTable.addCell(new Cell().add(new Paragraph("Emprunts en cours")));
                statsTable.addCell(new Cell().add(new Paragraph(totalBorrowsLabel.getText())));
                statsTable.addCell(new Cell().add(new Paragraph("Pénalités totales")));
                statsTable.addCell(new Cell().add(new Paragraph(totalPenaltiesLabel.getText())));
                statsTable.addCell(new Cell().add(new Paragraph("Livre le plus emprunté")));
                statsTable.addCell(new Cell().add(new Paragraph(mostBorrowedLabel.getText())));

                document.add(statsTable);
                document.add(new Paragraph("\n"));

                // ===== SECTION ADHÉRENTS =====
                document.add(new Paragraph("2. RAPPORT ADHÉRENTS")
                        .setFont(headerFont)
                        .setFontSize(12)
                        .setMarginTop(15)
                        .setMarginBottom(10));

                Table membersStatsTable = new Table(3);
                membersStatsTable.setWidth(UnitValue.createPercentValue(100));
                membersStatsTable.addCell(new Cell().add(new Paragraph("Adhérents actifs")));
                membersStatsTable.addCell(new Cell().add(new Paragraph(activeMembers.getText())));
                membersStatsTable.addCell(new Cell().add(new Paragraph("")));
                membersStatsTable.addCell(new Cell().add(new Paragraph("Adhérents suspendus")));
                membersStatsTable.addCell(new Cell().add(new Paragraph(suspendedMembers.getText())));
                membersStatsTable.addCell(new Cell().add(new Paragraph("")));
                membersStatsTable.addCell(new Cell().add(new Paragraph("Adhérent le plus actif")));
                membersStatsTable.addCell(new Cell().add(new Paragraph(mostActiveMember.getText())));
                membersStatsTable.addCell(new Cell().add(new Paragraph("")));

                document.add(membersStatsTable);
                document.add(new Paragraph("\n"));

                // ===== TABLEAU DÉTAILLÉ DES ADHÉRENTS =====
                document.add(new Paragraph("Détail des adhérents")
                        .setFont(headerFont)
                        .setFontSize(11)
                        .setMarginTop(10)
                        .setMarginBottom(5));

                Table memberTable = new Table(5);
                memberTable.setWidth(UnitValue.createPercentValue(100));
                memberTable.addHeaderCell(new Cell().add(new Paragraph("Matricule")).setFont(headerFont));
                memberTable.addHeaderCell(new Cell().add(new Paragraph("Nom")).setFont(headerFont));
                memberTable.addHeaderCell(new Cell().add(new Paragraph("Emprunts")).setFont(headerFont));
                memberTable.addHeaderCell(new Cell().add(new Paragraph("Retards")).setFont(headerFont));
                memberTable.addHeaderCell(new Cell().add(new Paragraph("Pénalités (FCFA)")).setFont(headerFont));

                for (MemberReport member : memberReports) {
                    memberTable.addCell(new Cell().add(new Paragraph(member.matricule)));
                    memberTable.addCell(new Cell().add(new Paragraph(member.nom)));
                    memberTable.addCell(new Cell().add(new Paragraph(String.valueOf(member.emprunts))));
                    memberTable.addCell(new Cell().add(new Paragraph(String.valueOf(member.retards))));
                    memberTable.addCell(new Cell().add(new Paragraph(
                            String.format("%.2f", member.penalites))));
                }

                document.add(memberTable);
                document.add(new Paragraph("\n"));

                // ===== SECTION LIVRES =====
                document.add(new Paragraph("3. RAPPORT LIVRES")
                        .setFont(headerFont)
                        .setFontSize(12)
                        .setMarginTop(15)
                        .setMarginBottom(10));

                Table booksStatsTable = new Table(3);
                booksStatsTable.setWidth(UnitValue.createPercentValue(100));
                booksStatsTable.addCell(new Cell().add(new Paragraph("Total livres")));
                booksStatsTable.addCell(new Cell().add(new Paragraph(totalBooks.getText())));
                booksStatsTable.addCell(new Cell().add(new Paragraph("")));
                booksStatsTable.addCell(new Cell().add(new Paragraph("Livres disponibles")));
                booksStatsTable.addCell(new Cell().add(new Paragraph(availableBooks.getText())));
                booksStatsTable.addCell(new Cell().add(new Paragraph("")));
                booksStatsTable.addCell(new Cell().add(new Paragraph("Taux de circulation")));
                booksStatsTable.addCell(new Cell().add(new Paragraph(circulationRate.getText())));
                booksStatsTable.addCell(new Cell().add(new Paragraph("")));

                document.add(booksStatsTable);
                document.add(new Paragraph("\n"));

                // ===== TABLEAU DÉTAILLÉ DES LIVRES =====
                document.add(new Paragraph("Détail des livres")
                        .setFont(headerFont)
                        .setFontSize(11)
                        .setMarginTop(10)
                        .setMarginBottom(5));

                Table bookTable = new Table(5);
                bookTable.setWidth(UnitValue.createPercentValue(100));
                bookTable.addHeaderCell(new Cell().add(new Paragraph("Titre")).setFont(headerFont));
                bookTable.addHeaderCell(new Cell().add(new Paragraph("Auteur")).setFont(headerFont));
                bookTable.addHeaderCell(new Cell().add(new Paragraph("Total")).setFont(headerFont));
                bookTable.addHeaderCell(new Cell().add(new Paragraph("Emprunts")).setFont(headerFont));
                bookTable.addHeaderCell(new Cell().add(new Paragraph("Disponible")).setFont(headerFont));

                for (BookReport book : bookReports) {
                    bookTable.addCell(new Cell().add(new Paragraph(book.titre)));
                    bookTable.addCell(new Cell().add(new Paragraph(book.auteur)));
                    bookTable.addCell(new Cell().add(new Paragraph(String.valueOf(book.total))));
                    bookTable.addCell(new Cell().add(new Paragraph(String.valueOf(book.emprunts))));
                    bookTable.addCell(new Cell().add(new Paragraph(String.valueOf(book.disponible))));
                }

                document.add(bookTable);

                // Fermer le document
                document.close();

                logger.info("✅ PDF exporté avec succès: " + selectedFile.getAbsolutePath());

                // Afficher un message de succès
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Export PDF");
                alert.setHeaderText("✅ Succès");
                alert.setContentText("Le rapport PDF a été exporté avec succès!\n\n" +
                        selectedFile.getAbsolutePath());
                alert.showAndWait();

            }
        } catch (Exception e) {
            logger.error("❌ Erreur lors de l'export PDF", e);
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur Export PDF");
            alert.setHeaderText("❌ Erreur lors de l'export");
            alert.setContentText("Erreur: " + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void actualiser() {
        chargerRapports();
        logger.info("✅ Rapports actualisés");
    }

    // ===== CLASSES INTERNES POUR LES RAPPORTS =====
    public static class MemberReport {
        public String matricule;
        public String nom;
        public int emprunts;
        public int retards;
        public double penalites;

        public MemberReport(String matricule, String nom, int emprunts, int retards, double penalites) {
            this.matricule = matricule;
            this.nom = nom;
            this.emprunts = emprunts;
            this.retards = retards;
            this.penalites = penalites;
        }
    }

    public static class BookReport {
        public String titre;
        public String auteur;
        public int total;
        public int emprunts;
        public int disponible;

        public BookReport(String titre, String auteur, int total, int emprunts, int disponible) {
            this.titre = titre;
            this.auteur = auteur;
            this.total = total;
            this.emprunts = emprunts;
            this.disponible = disponible;
        }
    }
}