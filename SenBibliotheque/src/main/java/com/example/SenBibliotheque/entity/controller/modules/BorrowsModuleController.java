package com.example.SenBibliotheque.entity.controller.modules;

import com.example.SenBibliotheque.entity.Adherent;
import com.example.SenBibliotheque.entity.Emprunt;
import com.example.SenBibliotheque.entity.Livre;
import com.example.SenBibliotheque.service.AdherentService;
import com.example.SenBibliotheque.service.EmpruntService;
import com.example.SenBibliotheque.service.LivreService;
import com.example.SenBibliotheque.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class BorrowsModuleController {

    private static final Logger logger = LoggerFactory.getLogger(BorrowsModuleController.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    private ComboBox<Adherent> memberCombo;
    @FXML
    private ComboBox<Livre> bookCombo;
    @FXML
    private DatePicker returnDatePicker;

    @FXML
    private TableView<Emprunt> activeBorrowsTable, overdueTable;
    @FXML
    private TextField searchActiveField, searchOverdueField;
    @FXML
    private Label activeBorrowsStatus, overdueStatus;

    private AdherentService adherentService;
    private LivreService livreService;
    private EmpruntService empruntService;
    private ObservableList<Emprunt> activeBorrows, overdue;

    @FXML
    public void initialize() {
        try {
            adherentService = new AdherentService();
            livreService = new LivreService();
            empruntService = new EmpruntService();

            activeBorrows = FXCollections.observableArrayList();
            overdue = FXCollections.observableArrayList();

            // ✅ Configurer les colonnes des tableaux
            configurerTableauEmpruntsEnCours();
            configurerTableauEmpruntsEnRetard();

            activeBorrowsTable.setItems(activeBorrows);
            overdueTable.setItems(overdue);

            chargerAdherents();
            chargerLivres();
            chargerEmprunts();

            logger.info("✅ BorrowsModuleController initialisé");
        } catch (Exception e) {
            logger.error("❌ Erreur lors de l'initialisation du module emprunts", e);
        }
    }

    // ===== CONFIGURATION DES TABLEAUX =====
    private void configurerTableauEmpruntsEnCours() {
        // Colonne Adhérent
        TableColumn<Emprunt, String> adherentCol = new TableColumn<>("Adhérent");
        adherentCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getAdherent().getPrenom() + " " + cellData.getValue().getAdherent().getNom()));
        adherentCol.setPrefWidth(130);

        // Colonne Livre
        TableColumn<Emprunt, String> livreCol = new TableColumn<>("Livre");
        livreCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getLivre().getTitre()));
        livreCol.setPrefWidth(160);

        // Colonne Date d'emprunt
        TableColumn<Emprunt, String> dateEmpruntCol = new TableColumn<>("Emprunt");
        dateEmpruntCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getDateEmprunt().format(DATE_FORMATTER)));
        dateEmpruntCol.setPrefWidth(130);

        // Colonne Date de retour prévue
        TableColumn<Emprunt, String> dateRetourCol = new TableColumn<>("Retour prévu");
        dateRetourCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getDateRetourPrevue().format(DATE_FORMATTER)));
        dateRetourCol.setPrefWidth(130);

        // Colonne Actions
        TableColumn<Emprunt, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setPrefWidth(200);
        actionsCol.setCellFactory(param -> new ActionCellEnCours());

        activeBorrowsTable.getColumns().clear();
        activeBorrowsTable.getColumns().addAll(adherentCol, livreCol, dateEmpruntCol, dateRetourCol, actionsCol);
    }

    private void configurerTableauEmpruntsEnRetard() {
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

        // Colonne Date de retour prévue
        TableColumn<Emprunt, String> dateRetourCol = new TableColumn<>("Retour prévu");
        dateRetourCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getDateRetourPrevue().format(DATE_FORMATTER)));
        dateRetourCol.setPrefWidth(100);

        // Colonne Jours de retard
        TableColumn<Emprunt, String> jourRetardCol = new TableColumn<>("Retard (j)");
        jourRetardCol.setCellValueFactory(cellData -> {
            long jours = cellData.getValue().getJoursRetard();
            return new javafx.beans.property.SimpleStringProperty(
                    jours > 0 ? String.valueOf(jours) + " ⚠️" : "0");
        });
        jourRetardCol.setPrefWidth(80);

        // Colonne Pénalité
        TableColumn<Emprunt, String> penaliteCol = new TableColumn<>("Pénalité (FCFA)");
        penaliteCol.setCellValueFactory(cellData -> {
            cellData.getValue().calculerPenalite();
            double penalite = cellData.getValue().getPenalite();
            return new javafx.beans.property.SimpleStringProperty(
                    penalite > 0 ? String.format("%.2f", penalite) : "0.00");
        });
        penaliteCol.setPrefWidth(120);

        // Colonne Actions
        TableColumn<Emprunt, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setPrefWidth(150);
        actionsCol.setCellFactory(param -> new ActionCellRetard());

        overdueTable.getColumns().clear();
        overdueTable.getColumns().addAll(adherentCol, livreCol, dateRetourCol, jourRetardCol, penaliteCol, actionsCol);
    }
    // ===== CHARGEMENT DES DONNÉES =====
    private void chargerAdherents() {
        try {
            List<Adherent> list = adherentService.obtenirActifs();
            memberCombo.setItems(FXCollections.observableArrayList(list));
            logger.info("✅ Adhérents chargés: " + list.size());
        } catch (Exception e) {
            logger.error("❌ Erreur lors du chargement des adhérents", e);
        }
    }

    private void chargerLivres() {
        try {
            List<Livre> list = livreService.obtenirDisponibles();
            bookCombo.setItems(FXCollections.observableArrayList(list));
            logger.info("✅ Livres disponibles chargés: " + list.size());
        } catch (Exception e) {
            logger.error("❌ Erreur lors du chargement des livres", e);
        }
    }

    private void chargerEmprunts() {
        try {
            List<Emprunt> active = empruntService.obtenirEnCours();
            activeBorrows.clear();
            activeBorrows.addAll(active);
            activeBorrowsStatus.setText(active.size() + " emprunt(s) en cours");

            List<Emprunt> overdues = empruntService.obtenirEnRetard();
            overdue.clear();
            overdue.addAll(overdues);
            overdueStatus.setText(overdues.size() + " retard(s) détecté(s)");

            logger.info("✅ Emprunts chargés - En cours: " + active.size() + ", En retard: " + overdues.size());
        } catch (Exception e) {
            logger.error("❌ Erreur lors du chargement des emprunts", e);
        }
    }

    // ===== ACTIONS =====
    @FXML
    private void enregistrerEmprunt() {
        try {
            Adherent adherent = memberCombo.getValue();
            Livre livre = bookCombo.getValue();
            LocalDateTime dateRetour = returnDatePicker.getValue() != null ?
                    returnDatePicker.getValue().atStartOfDay() : LocalDateTime.now().plusDays(14);

            if (adherent == null || livre == null) {
                afficherAlerte("Erreur", "Veuillez sélectionner un adhérent et un livre");
                return;
            }

            if (livre.getDisponible() <= 0) {
                afficherAlerte("Erreur", "Ce livre n'est pas disponible");
                return;
            }

            empruntService.creer(livre, adherent, SessionManager.getInstance().getUtilisateurConnecte(), dateRetour);
            afficherSucces("Succès", "Emprunt enregistré avec succès!");

            chargerEmprunts();
            chargerLivres();
            memberCombo.setValue(null);
            bookCombo.setValue(null);
            returnDatePicker.setValue(null);

            logger.info("✅ Emprunt créé: " + adherent.getNom() + " - " + livre.getTitre());
        } catch (Exception e) {
            logger.error("❌ Erreur lors de l'enregistrement de l'emprunt", e);
            afficherAlerte("Erreur", "Erreur: " + e.getMessage());
        }
    }

    @FXML
    private void annulerEmprunt() {
        memberCombo.setValue(null);
        bookCombo.setValue(null);
        returnDatePicker.setValue(null);
    }

    @FXML
    private void rechercherActifs() {
        String terme = searchActiveField.getText().trim();

        if (terme.isEmpty()) {
            chargerEmprunts();
            return;
        }

        try {
            List<Emprunt> resultats = empruntService.obtenirEnCours().stream()
                    .filter(e -> e.getAdherent().getNom().toLowerCase().contains(terme.toLowerCase()) ||
                            e.getAdherent().getPrenom().toLowerCase().contains(terme.toLowerCase()) ||
                            e.getLivre().getTitre().toLowerCase().contains(terme.toLowerCase()))
                    .toList();

            activeBorrows.clear();
            activeBorrows.addAll(resultats);
            activeBorrowsStatus.setText(resultats.size() + " emprunt(s) trouvé(s)");

            logger.info("✅ Recherche emprunts en cours: " + resultats.size() + " résultats");
        } catch (Exception e) {
            logger.error("❌ Erreur lors de la recherche", e);
            afficherAlerte("Erreur", "Erreur lors de la recherche");
        }
    }

    @FXML
    private void rechercherRetards() {
        String terme = searchOverdueField.getText().trim();

        if (terme.isEmpty()) {
            chargerEmprunts();
            return;
        }

        try {
            List<Emprunt> resultats = empruntService.obtenirEnRetard().stream()
                    .filter(e -> e.getAdherent().getNom().toLowerCase().contains(terme.toLowerCase()) ||
                            e.getAdherent().getPrenom().toLowerCase().contains(terme.toLowerCase()) ||
                            e.getLivre().getTitre().toLowerCase().contains(terme.toLowerCase()))
                    .toList();

            overdue.clear();
            overdue.addAll(resultats);
            overdueStatus.setText(resultats.size() + " retard(s) trouvé(s)");

            logger.info("✅ Recherche emprunts en retard: " + resultats.size() + " résultats");
        } catch (Exception e) {
            logger.error("❌ Erreur lors de la recherche", e);
            afficherAlerte("Erreur", "Erreur lors de la recherche");
        }
    }

    // ===== CLASSES INTERNES POUR LES ACTIONS =====
    private class ActionCellEnCours extends TableCell<Emprunt, Void> {
        @Override
        protected void updateItem(Void item, boolean empty) {
            super.updateItem(item, empty);

            if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                setGraphic(null);
                return;
            }

            Emprunt emprunt = getTableRow().getItem();

            Button retourBtn = new Button("↩️");
            retourBtn.setStyle("-fx-font-size: 12; -fx-padding: 6 12; -fx-background-color: #10b981; -fx-text-fill: white; -fx-border-radius: 3; -fx-background-radius: 3; -fx-cursor: hand;");
            Tooltip tooltipRetour = new Tooltip("Enregistrer le retour");
            Tooltip.install(retourBtn, tooltipRetour);
            retourBtn.setOnAction(e -> enregistrerRetour(emprunt));

            Button detailsBtn = new Button("👁️");
            detailsBtn.setStyle("-fx-font-size: 12; -fx-padding: 6 12; -fx-background-color: #06b6d4; -fx-text-fill: white; -fx-border-radius: 3; -fx-background-radius: 3; -fx-cursor: hand;");
            Tooltip tooltipDetails = new Tooltip("Voir les détails");
            Tooltip.install(detailsBtn, tooltipDetails);
            detailsBtn.setOnAction(e -> afficherDetailsEmprunt(emprunt));

            HBox hBox = new HBox(5);
            hBox.setStyle("-fx-alignment: CENTER_LEFT;");
            hBox.getChildren().addAll(retourBtn, detailsBtn);
            setGraphic(hBox);
        }
    }

    private class ActionCellRetard extends TableCell<Emprunt, Void> {
        @Override
        protected void updateItem(Void item, boolean empty) {
            super.updateItem(item, empty);

            if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                setGraphic(null);
                return;
            }

            Emprunt emprunt = getTableRow().getItem();

            Button retourBtn = new Button("↩️");
            retourBtn.setStyle("-fx-font-size: 12; -fx-padding: 6 12; -fx-background-color: #ef4444; -fx-text-fill: white; -fx-border-radius: 3; -fx-background-radius: 3; -fx-cursor: hand;");
            Tooltip tooltipRetour = new Tooltip("Enregistrer le retour avec pénalité");
            Tooltip.install(retourBtn, tooltipRetour);
            retourBtn.setOnAction(e -> enregistrerRetour(emprunt));

            Button detailsBtn = new Button("👁️");
            detailsBtn.setStyle("-fx-font-size: 12; -fx-padding: 6 12; -fx-background-color: #06b6d4; -fx-text-fill: white; -fx-border-radius: 3; -fx-background-radius: 3; -fx-cursor: hand;");
            Tooltip tooltipDetails = new Tooltip("Voir les détails");
            Tooltip.install(detailsBtn, tooltipDetails);
            detailsBtn.setOnAction(e -> afficherDetailsEmprunt(emprunt));

            HBox hBox = new HBox(5);
            hBox.setStyle("-fx-alignment: CENTER_LEFT;");
            hBox.getChildren().addAll(retourBtn, detailsBtn);
            setGraphic(hBox);
        }
    }

    // ===== MÉTHODES UTILITAIRES =====
    private void enregistrerRetour(Emprunt emprunt) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Enregistrer le retour");
        confirmation.setHeaderText("Confirmer le retour du livre?");
        confirmation.setContentText(emprunt.getLivre().getTitre() + "\n" +
                "Adhérent: " + emprunt.getAdherent().getPrenom() + " " + emprunt.getAdherent().getNom());

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // Enregistrer le retour avec la date actuelle
                Emprunt empruntRetourne = empruntService.enregistrerRetour(emprunt.getId(), LocalDateTime.now());

                // Recharger les données
                chargerEmprunts();
                chargerLivres();

                // Afficher le résultat
                if (empruntRetourne.getJoursRetard() > 0) {
                    // Il y a un retard
                    Alert alertRetard = new Alert(Alert.AlertType.WARNING);
                    alertRetard.setTitle("⚠️ Retard détecté");
                    alertRetard.setHeaderText("Ce livre a été retourné en retard");
                    alertRetard.setContentText(
                            "Livre: " + empruntRetourne.getLivre().getTitre() + "\n" +
                                    "Adhérent: " + empruntRetourne.getAdherent().getPrenom() + " " +
                                    empruntRetourne.getAdherent().getNom() + "\n\n" +
                                    "Retour prévu: " + empruntRetourne.getDateRetourPrevue().format(DATE_FORMATTER) + "\n" +
                                    "Retour effectif: " + empruntRetourne.getDateRetourEffective().format(DATE_FORMATTER) + "\n\n" +
                                    "⏱️  Jours de retard: " + empruntRetourne.getJoursRetard() + " jour(s)\n" +
                                    "💰 Pénalité: " + String.format("%.2f", empruntRetourne.getPenalite()) + " FCFA"
                    );
                    alertRetard.showAndWait();
                } else {
                    // Pas de retard
                    Alert alertOK = new Alert(Alert.AlertType.INFORMATION);
                    alertOK.setTitle("✅ Succès");
                    alertOK.setHeaderText("Retour enregistré");
                    alertOK.setContentText(
                            "Le livre a été retourné à temps.\n\n" +
                                    "Livre: " + empruntRetourne.getLivre().getTitre() + "\n" +
                                    "Adhérent: " + empruntRetourne.getAdherent().getPrenom() + " " +
                                    empruntRetourne.getAdherent().getNom()
                    );
                    alertOK.showAndWait();
                }

                logger.info("✅ Retour enregistré: " + emprunt.getId());
            } catch (Exception e) {
                logger.error("❌ Erreur lors de l'enregistrement du retour", e);
                Alert alertErreur = new Alert(Alert.AlertType.ERROR);
                alertErreur.setTitle("❌ Erreur");
                alertErreur.setHeaderText("Impossible d'enregistrer le retour");
                alertErreur.setContentText("Erreur: " + e.getMessage());
                alertErreur.showAndWait();
            }
        }
    }

    private void afficherDetailsEmprunt(Emprunt emprunt) {
        StringBuilder details = new StringBuilder();
        details.append("📚 DÉTAILS DE L'EMPRUNT\n\n");
        details.append("Adhérent: ").append(emprunt.getAdherent().getPrenom()).append(" ").append(emprunt.getAdherent().getNom()).append("\n");
        details.append("Matricule: ").append(emprunt.getAdherent().getMatricule()).append("\n");
        details.append("Email: ").append(emprunt.getAdherent().getEmail()).append("\n\n");
        details.append("Livre: ").append(emprunt.getLivre().getTitre()).append("\n");
        details.append("Auteur: ").append(emprunt.getLivre().getAuteur()).append("\n");
        details.append("ISBN: ").append(emprunt.getLivre().getIsbn()).append("\n\n");
        details.append("Date d'emprunt: ").append(emprunt.getDateEmprunt().format(DATE_FORMATTER)).append("\n");
        details.append("Date de retour prévue: ").append(emprunt.getDateRetourPrevue().format(DATE_FORMATTER)).append("\n");

        if (emprunt.getDateRetourEffective() != null) {
            details.append("Date de retour effective: ").append(emprunt.getDateRetourEffective().format(DATE_FORMATTER)).append("\n");
        }

        if (emprunt.isEnRetard()) {
            details.append("Statut: ⚠️ EN RETARD\n");
            details.append("Jours de retard: ").append(emprunt.getJoursRetard()).append("\n");
            details.append("Pénalité: ").append(String.format("%.2f", emprunt.getPenalite())).append(" FCFA\n");
        } else {
            details.append("Statut: ✅ À JOUR\n");
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Détails de l'emprunt");
        alert.setHeaderText(null);
        alert.setContentText(details.toString());
        alert.showAndWait();
    }

    private void afficherSucces(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void afficherAlerte(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}