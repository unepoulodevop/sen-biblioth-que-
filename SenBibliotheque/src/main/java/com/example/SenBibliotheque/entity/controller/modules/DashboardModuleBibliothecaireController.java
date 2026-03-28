package com.example.SenBibliotheque.entity.controller.modules;

import com.example.SenBibliotheque.entity.Emprunt;
import com.example.SenBibliotheque.service.AdherentService;
import com.example.SenBibliotheque.service.EmpruntService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class DashboardModuleBibliothecaireController {

    private static final Logger logger = LoggerFactory.getLogger(DashboardModuleBibliothecaireController.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter DATE_ONLY = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ===== LABELS STATISTIQUES =====
    @FXML
    private Label activeBorrowsLabel, borrowsDetailsLabel;
    @FXML
    private Label overdueLabel, overdueDetailsLabel;
    @FXML
    private Label activeMembersLabel, membersDetailsLabel;

    // ===== TABLEAUX =====
    @FXML
    private TableView<Emprunt> borrowsToHandleTable;
    @FXML
    private Label borrowsStatus;

    @FXML
    private TableView<Emprunt> overdueTable;
    @FXML
    private Label overdueTableStatus;

    // ===== SERVICES =====
    private EmpruntService empruntService;
    private AdherentService adherentService;

    private ObservableList<Emprunt> borrowsToHandle;
    private ObservableList<Emprunt> overdue;

    @FXML
    public void initialize() {
        try {
            empruntService = new EmpruntService();
            adherentService = new AdherentService();

            borrowsToHandle = FXCollections.observableArrayList();
            overdue = FXCollections.observableArrayList();

            // Configurer les tableaux
            configurerTableauEmprunts();
            configurerTableauRetards();

            // Charger les statistiques
            chargerStatistiques();

            logger.info("✅ DashboardModuleBibliothecaireController initialisé");
        } catch (Exception e) {
            logger.error("❌ Erreur lors de l'initialisation du dashboard bibliothécaire", e);
        }
    }

    // ===== CONFIGURATION DES TABLEAUX =====
    private void configurerTableauEmprunts() {
        // Colonne Adhérent
        TableColumn<Emprunt, String> adherentCol = new TableColumn<>("Adhérent");
        adherentCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getAdherent().getPrenom() + " " + cellData.getValue().getAdherent().getNom()));
        adherentCol.setPrefWidth(120);

        // Colonne Livre
        TableColumn<Emprunt, String> livreCol = new TableColumn<>("Livre");
        livreCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getLivre().getTitre()));
        livreCol.setPrefWidth(150);

        // Colonne Date d'emprunt
        TableColumn<Emprunt, String> dateEmpruntCol = new TableColumn<>("Date d'emprunt");
        dateEmpruntCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getDateEmprunt().format(DATE_FORMATTER)));
        dateEmpruntCol.setPrefWidth(120);

        // Colonne Retour prévu
        TableColumn<Emprunt, String> dateRetourCol = new TableColumn<>("Retour prévu");
        dateRetourCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getDateRetourPrevue().format(DATE_FORMATTER)));
        dateRetourCol.setPrefWidth(120);

        // Colonne Status
        TableColumn<Emprunt, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(cellData -> {
            if (cellData.getValue().isEnRetard()) {
                return new javafx.beans.property.SimpleStringProperty("⚠️ Retard");
            } else {
                return new javafx.beans.property.SimpleStringProperty("✅ En cours");
            }
        });
        statusCol.setPrefWidth(80);

        borrowsToHandleTable.getColumns().clear();
        borrowsToHandleTable.getColumns().addAll(adherentCol, livreCol, dateEmpruntCol, dateRetourCol, statusCol);
        borrowsToHandleTable.setItems(borrowsToHandle);
    }

    private void configurerTableauRetards() {
        // Colonne Adhérent
        TableColumn<Emprunt, String> adherentCol = new TableColumn<>("Adhérent");
        adherentCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getAdherent().getPrenom() + " " + cellData.getValue().getAdherent().getNom()));
        adherentCol.setPrefWidth(120);

        // Colonne Livre
        TableColumn<Emprunt, String> livreCol = new TableColumn<>("Livre");
        livreCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getLivre().getTitre()));
        livreCol.setPrefWidth(150);

        // Colonne Retour prévu
        TableColumn<Emprunt, String> dateRetourCol = new TableColumn<>("Retour prévu");
        dateRetourCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getDateRetourPrevue().format(DATE_ONLY)));
        dateRetourCol.setPrefWidth(120);

        // Colonne Jours de retard
        TableColumn<Emprunt, String> jourRetardCol = new TableColumn<>("Retard (j)");
        jourRetardCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(String.valueOf(cellData.getValue().getJoursRetard())));
        jourRetardCol.setPrefWidth(80);

        // Colonne Pénalité
        TableColumn<Emprunt, String> penaliteCol = new TableColumn<>("Pénalité (FCFA)");
        penaliteCol.setCellValueFactory(cellData -> {
            cellData.getValue().calculerPenalite();
            return new javafx.beans.property.SimpleStringProperty(
                    String.format("%.2f", cellData.getValue().getPenalite()));
        });
        penaliteCol.setPrefWidth(120);

        // Colonne Actions
        TableColumn<Emprunt, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setPrefWidth(100);
        actionsCol.setCellFactory(param -> new ActionCell());

        overdueTable.getColumns().clear();
        overdueTable.getColumns().addAll(adherentCol, livreCol, dateRetourCol, jourRetardCol, penaliteCol, actionsCol);
        overdueTable.setItems(overdue);
    }

    // ===== CHARGEMENT DES STATISTIQUES =====
    private void chargerStatistiques() {
        try {
            // ===== EMPRUNTS EN COURS =====
            List<Emprunt> empruntsEnCours = empruntService.obtenirEnCours();
            borrowsToHandle.clear();
            borrowsToHandle.addAll(empruntsEnCours);
            borrowsStatus.setText(empruntsEnCours.size() + " emprunt(s)");

            activeBorrowsLabel.setText(String.valueOf(empruntsEnCours.size()));
            borrowsDetailsLabel.setText("À gérer aujourd'hui");

            logger.info("🔄 Emprunts en cours: " + empruntsEnCours.size());

            // ===== EMPRUNTS EN RETARD =====
            List<Emprunt> empruntsEnRetard = empruntService.obtenirEnRetard();
            overdue.clear();
            overdue.addAll(empruntsEnRetard);
            overdueTableStatus.setText(empruntsEnRetard.size() + " retard(s)");

            overdueLabel.setText(String.valueOf(empruntsEnRetard.size()));
            if (empruntsEnRetard.isEmpty()) {
                overdueDetailsLabel.setText("✅ Aucun retard");
                overdueDetailsLabel.setStyle("-fx-text-fill: #10b981;");
            } else {
                overdueDetailsLabel.setText("⚠️ À signaler");
                overdueDetailsLabel.setStyle("-fx-text-fill: #ef5350;");
            }

            logger.info("⏰ Emprunts en retard: " + empruntsEnRetard.size());

            // ===== ADHÉRENTS ACTIFS =====
            long totalAdherents = adherentService.compterTous();
            long adherentsActifs = adherentService.compterActifs();
            long adherentsSuspendus = totalAdherents - adherentsActifs;

            activeMembersLabel.setText(String.valueOf(adherentsActifs));
            membersDetailsLabel.setText("Dont " + adherentsSuspendus + " suspendu(s)");

            logger.info("👥 Adhérents actifs: " + adherentsActifs + ", Suspendus: " + adherentsSuspendus);

        } catch (Exception e) {
            logger.error("❌ Erreur lors du chargement des statistiques", e);
        }
    }

    // ===== ACTION CELL =====
    private class ActionCell extends TableCell<Emprunt, Void> {
        @Override
        protected void updateItem(Void item, boolean empty) {
            super.updateItem(item, empty);

            if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                setGraphic(null);
                return;
            }

            Emprunt emprunt = getTableRow().getItem();

            Button contactBtn = new Button("📞");
            contactBtn.setStyle(
                    "-fx-font-size: 12; " +
                            "-fx-padding: 6 10; " +
                            "-fx-background-color: #f59e0b; " +
                            "-fx-text-fill: white; " +
                            "-fx-border-radius: 4; " +
                            "-fx-background-radius: 4; " +
                            "-fx-cursor: hand;"
            );

            Tooltip tip = new Tooltip("Contacter l'adhérent");
            tip.setStyle("-fx-font-size: 11; -fx-text-fill: #ffffff; -fx-background-color: #1f2937; -fx-padding: 8;");
            Tooltip.install(contactBtn, tip);

            contactBtn.setOnAction(e -> contacterAdherent(emprunt));

            setGraphic(contactBtn);
        }

        private void contacterAdherent(Emprunt emprunt) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Contacter l'adhérent");
            alert.setHeaderText("Coordonnées de l'adhérent");
            alert.setContentText(
                    "Nom: " + emprunt.getAdherent().getPrenom() + " " + emprunt.getAdherent().getNom() + "\n" +
                            "Téléphone: " + emprunt.getAdherent().getTelephone() + "\n" +
                            "Email: " + emprunt.getAdherent().getEmail() + "\n\n" +
                            "Sujet: Retour du livre \"" + emprunt.getLivre().getTitre() + "\""
            );
            alert.showAndWait();
        }
    }
}