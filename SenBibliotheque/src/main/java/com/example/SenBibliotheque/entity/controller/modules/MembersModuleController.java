package com.example.SenBibliotheque.entity.controller.modules;

import com.example.SenBibliotheque.entity.Adherent;
import com.example.SenBibliotheque.service.AdherentService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class MembersModuleController {

    private static final Logger logger = LoggerFactory.getLogger(MembersModuleController.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML
    private TableView<Adherent> membersTable;
    @FXML
    private TextField searchField;
    @FXML
    private Label statusLabel;

    private AdherentService adherentService;
    private ObservableList<Adherent> adherents;

    @FXML
    public void initialize() {
        try {
            adherentService = new AdherentService();
            adherents = FXCollections.observableArrayList();

            // Configurer la table
            configurerTable();

            // Charger les adhérents
            chargerAdherents();
        } catch (Exception e) {
            logger.error("❌ Erreur lors de l'initialisation du module adhérents", e);
        }
    }

    private void configurerTable() {
        // Colonne Matricule
        TableColumn<Adherent, String> matriculeCol = new TableColumn<>("Matricule");
        matriculeCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getMatricule()));
        matriculeCol.setPrefWidth(100);

        // Colonne Nom
        TableColumn<Adherent, String> nomCol = new TableColumn<>("Nom");
        nomCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getNom()));
        nomCol.setPrefWidth(100);

        // Colonne Prénom
        TableColumn<Adherent, String> prenomCol = new TableColumn<>("Prénom");
        prenomCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getPrenom()));
        prenomCol.setPrefWidth(100);

        // Colonne Email
        TableColumn<Adherent, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getEmail()));
        emailCol.setPrefWidth(150);

        // Colonne Téléphone
        TableColumn<Adherent, String> telephoneCol = new TableColumn<>("Téléphone");
        telephoneCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getTelephone() != null ? cellData.getValue().getTelephone() : "N/A"));
        telephoneCol.setPrefWidth(120);

        // Colonne Statut
        TableColumn<Adherent, String> statutCol = new TableColumn<>("Statut");
        statutCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getActif() ? "✅ Actif" : "⛔ Suspendu"));
        statutCol.setPrefWidth(100);

        // Colonne Actions
        TableColumn<Adherent, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setPrefWidth(200);
        actionsCol.setCellFactory(param -> new ActionCell());

        membersTable.getColumns().addAll(matriculeCol, nomCol, prenomCol, emailCol, telephoneCol, statutCol, actionsCol);
        membersTable.setItems(adherents);
    }

    private void chargerAdherents() {
        try {
            List<Adherent> list = adherentService.obtenirTous();
            adherents.clear();
            adherents.addAll(list);
            statusLabel.setText(list.size() + " adhérent(s)");
            logger.info("✅ Adhérents chargés: " + list.size());
        } catch (Exception e) {
            logger.error("❌ Erreur lors du chargement des adhérents", e);
        }
    }

    @FXML
    private void ajouterAdherent() {
        try {
            FXMLLoader loader = new FXMLLoader(MembersModuleController.class.getResource("/fxml/dialogs/AddMemberDialog.fxml"));
            ScrollPane scrollPane = loader.load();
            AddMemberDialogController controller = loader.getController();

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.getDialogPane().setContent(scrollPane);
            dialog.setTitle("Ajouter un nouvel adhérent");
            dialog.setWidth(700);
            dialog.setHeight(480);

            dialog.getDialogPane().getButtonTypes().addAll(
                    new ButtonType("✅ Ajouter", ButtonBar.ButtonData.OK_DONE),
                    new ButtonType("❌ Annuler", ButtonBar.ButtonData.CANCEL_CLOSE)
            );

            dialog.initOwner(membersTable.getScene().getWindow());

            Optional<ButtonType> result = dialog.showAndWait();

            if (result.isPresent() && result.get().getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                if (!controller.validateForm()) {
                    return;
                }

                Adherent nouvelAdherent = new Adherent(
                        "", // Le matricule sera généré par le service
                        controller.getNom(),
                        controller.getPrenom(),
                        controller.getEmail(),
                        controller.getTelephone()
                );
                nouvelAdherent.setAdresse(controller.getAdresse());
                nouvelAdherent.setActif("Actif".equals(controller.getStatut()));
                nouvelAdherent.setDateInscription(LocalDateTime.now());

                adherentService.creer(nouvelAdherent.getNom(), nouvelAdherent.getPrenom(),
                        nouvelAdherent.getEmail(), nouvelAdherent.getTelephone(),
                        nouvelAdherent.getAdresse());

                logger.info("✅ Adhérent créé: " + controller.getNom() + " " + controller.getPrenom());
                afficherSucces("Adhérent ajouté avec succès!",
                        "Nom: " + controller.getNom() + "\n" +
                                "Prénom: " + controller.getPrenom() + "\n" +
                                "Email: " + controller.getEmail());

                chargerAdherents();
            }

        } catch (IOException e) {
            logger.error("❌ Erreur lors du chargement du dialogue", e);
            afficherErreur("Erreur lors de l'ouverture du formulaire");
        } catch (Exception e) {
            logger.error("❌ Erreur lors de l'ajout de l'adhérent", e);
            afficherErreur("Erreur: " + e.getMessage());
        }
    }

    @FXML
    private void rechercher() {
        String terme = searchField.getText().trim();

        if (terme.isEmpty()) {
            chargerAdherents();
            return;
        }

        try {
            List<Adherent> resultats = adherentService.obtenirTous().stream()
                    .filter(a -> a.getNom().toLowerCase().contains(terme.toLowerCase()) ||
                            a.getPrenom().toLowerCase().contains(terme.toLowerCase()) ||
                            a.getMatricule().toLowerCase().contains(terme.toLowerCase()) ||
                            a.getEmail().toLowerCase().contains(terme.toLowerCase()))
                    .toList();

            adherents.clear();
            adherents.addAll(resultats);
            statusLabel.setText(resultats.size() + " adhérent(s) trouvé(s)");

            logger.info("✅ Recherche effectuée: " + resultats.size() + " résultats");
        } catch (Exception e) {
            logger.error("❌ Erreur lors de la recherche", e);
            afficherErreur("Erreur lors de la recherche");
        }
    }

    private void afficherSucces(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void afficherErreur(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // ===== CLASSE INTERNE POUR LES ACTIONS =====
    private class ActionCell extends TableCell<Adherent, Void> {
        @Override
        protected void updateItem(Void item, boolean empty) {
            super.updateItem(item, empty);

            if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                setGraphic(null);
                return;
            }

            Adherent adherent = getTableRow().getItem();

            // Bouton Modifier
            Button modifierBtn = createActionButton("✏️", "#3b82f6", "Modifier cet adhérent");
            modifierBtn.setOnAction(e -> modifierAdherent(adherent));

            // Bouton Historique
            Button historiqueBtn = createActionButton("📋", "#06b6d4", "Voir l'historique des emprunts");
            historiqueBtn.setOnAction(e -> afficherHistorique(adherent));

            // Bouton Suspension/Réactivation
            Button suspensionBtn = createActionButton(
                    adherent.getActif() ? "🔒" : "🔓",
                    adherent.getActif() ? "#f59e0b" : "#10b981",
                    adherent.getActif() ? "Suspendre cet adhérent" : "Réactiver cet adhérent"
            );
            suspensionBtn.setOnAction(e -> basculerSuspension(adherent));

            // Bouton Supprimer
            Button supprimerBtn = createActionButton("🗑️", "#dc2626", "Supprimer cet adhérent");
            supprimerBtn.setOnAction(e -> supprimerAdherent(adherent));

            HBox hBox = new HBox(8);
            hBox.setStyle("-fx-alignment: CENTER_LEFT;");
            hBox.getChildren().addAll(modifierBtn, historiqueBtn, suspensionBtn, supprimerBtn);
            setGraphic(hBox);
        }

        private Button createActionButton(String emoji, String color, String tooltip) {
            Button btn = new Button(emoji);
            btn.setStyle(
                    "-fx-font-size: 14; " +
                            "-fx-background-color: " + color + "; " +
                            "-fx-text-fill: white; " +
                            "-fx-padding: 8 10; " +
                            "-fx-border-radius: 4; " +
                            "-fx-background-radius: 4; " +
                            "-fx-cursor: hand; " +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 3, 0, 0, 1);"
            );

            Tooltip tip = new Tooltip(tooltip);
            tip.setStyle("-fx-font-size: 11; -fx-text-fill: #ffffff; -fx-background-color: #1f2937; -fx-padding: 8;");
            Tooltip.install(btn, tip);

            btn.setOnMouseEntered(e -> btn.setStyle(
                    "-fx-font-size: 14; " +
                            "-fx-background-color: " + lightenColor(color) + "; " +
                            "-fx-text-fill: white; " +
                            "-fx-padding: 8 10; " +
                            "-fx-border-radius: 4; " +
                            "-fx-background-radius: 4; " +
                            "-fx-cursor: hand; " +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 6, 0, 0, 2);"
            ));

            btn.setOnMouseExited(e -> btn.setStyle(
                    "-fx-font-size: 14; " +
                            "-fx-background-color: " + color + "; " +
                            "-fx-text-fill: white; " +
                            "-fx-padding: 8 10; " +
                            "-fx-border-radius: 4; " +
                            "-fx-background-radius: 4; " +
                            "-fx-cursor: hand; " +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 3, 0, 0, 1);"
            ));

            return btn;
        }

        private String lightenColor(String color) {
            return switch (color) {
                case "#3b82f6" -> "#60a5fa";  // Bleu
                case "#06b6d4" -> "#22d3ee";  // Cyan
                case "#f59e0b" -> "#fbbf24";  // Amber
                case "#10b981" -> "#34d399"; // Vert
                case "#dc2626" -> "#ef4444"; // Rouge
                default -> color;
            };
        }
    }

    // ===== ACTIONS SUR LES ADHÉRENTS =====
    private void modifierAdherent(Adherent adherent) {
        try {
            FXMLLoader loader = new FXMLLoader(MembersModuleController.class.getResource("/fxml/dialogs/AddMemberDialog.fxml"));
            ScrollPane scrollPane = loader.load();
            AddMemberDialogController controller = loader.getController();
            controller.setAdherent(adherent);

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.getDialogPane().setContent(scrollPane);
            dialog.setTitle("Modifier l'adhérent");
            dialog.setWidth(700);
            dialog.setHeight(780);

            dialog.getDialogPane().getButtonTypes().addAll(
                    new ButtonType("✅ Enregistrer", ButtonBar.ButtonData.OK_DONE),
                    new ButtonType("❌ Annuler", ButtonBar.ButtonData.CANCEL_CLOSE)
            );

            dialog.initOwner(membersTable.getScene().getWindow());

            Optional<ButtonType> result = dialog.showAndWait();

            if (result.isPresent() && result.get().getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                if (!controller.validateForm()) {
                    return;
                }

                adherent.setNom(controller.getNom());
                adherent.setPrenom(controller.getPrenom());
                adherent.setEmail(controller.getEmail());
                adherent.setTelephone(controller.getTelephone());
                adherent.setAdresse(controller.getAdresse());
                adherent.setActif("Actif".equals(controller.getStatut()));

                adherentService.update(adherent);

                logger.info("✅ Adhérent modifié: " + adherent.getMatricule());
                afficherSucces("Succès", "Adhérent modifié avec succès!");

                chargerAdherents();
            }

        } catch (IOException e) {
            logger.error("❌ Erreur lors du chargement du dialogue", e);
            afficherErreur("Erreur lors de l'ouverture du formulaire");
        } catch (Exception e) {
            logger.error("❌ Erreur lors de la modification de l'adhérent", e);
            afficherErreur("Erreur: " + e.getMessage());
        }
    }

    private void afficherHistorique(Adherent adherent) {
        StringBuilder historique = new StringBuilder();
        historique.append("📋 HISTORIQUE DES EMPRUNTS\n\n");
        historique.append("Adhérent: ").append(adherent.getPrenom()).append(" ").append(adherent.getNom()).append("\n");
        historique.append("Matricule: ").append(adherent.getMatricule()).append("\n");
        historique.append("Email: ").append(adherent.getEmail()).append("\n\n");

        if (adherent.getEmprunts().isEmpty()) {
            historique.append("Aucun emprunt enregistré");
        } else {
            historique.append("Total emprunts: ").append(adherent.getEmprunts().size()).append("\n\n");

            adherent.getEmprunts().forEach(emprunt -> {
                historique.append("📚 ").append(emprunt.getLivre().getTitre()).append("\n");
                historique.append("   Emprunté: ").append(emprunt.getDateEmprunt().format(DATE_FORMATTER)).append("\n");
                if (emprunt.getDateRetourEffective() != null) {
                    historique.append("   Retourné: ").append(emprunt.getDateRetourEffective().format(DATE_FORMATTER)).append("\n");
                } else {
                    historique.append("   Statut: EN COURS\n");
                    if (emprunt.getDateRetourPrevue() != null) {
                        historique.append("   Retour prévu: ").append(emprunt.getDateRetourPrevue().format(DATE_FORMATTER)).append("\n");
                    }
                }
                historique.append("\n");
            });
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Historique des emprunts");
        alert.setHeaderText(null);
        alert.setContentText(historique.toString());
        alert.showAndWait();
    }

    private void basculerSuspension(Adherent adherent) {
        String action = adherent.getActif() ? "suspendre" : "réactiver";
        String titre = adherent.getActif() ? "Suspendre l'adhérent?" : "Réactiver l'adhérent?";

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation");
        confirmation.setHeaderText(titre);
        confirmation.setContentText("Êtes-vous sûr de vouloir " + action + " le compte de:\n\n" +
                adherent.getPrenom() + " " + adherent.getNom() +
                " (" + adherent.getMatricule() + ")?");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                if (adherent.getActif()) {
                    adherentService.suspendre(adherent.getId());
                } else {
                    adherentService.reactiver(adherent.getId());
                }

                logger.info("✅ Statut de l'adhérent modifié: " + adherent.getMatricule());
                afficherSucces("Succès", "Statut de l'adhérent modifié avec succès!");

                chargerAdherents();
            } catch (Exception e) {
                logger.error("❌ Erreur lors du basculement de suspension", e);
                afficherErreur("Erreur: " + e.getMessage());
            }
        }
    }

    private void supprimerAdherent(Adherent adherent) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation de suppression");
        confirmation.setHeaderText("Supprimer l'adhérent?");
        confirmation.setContentText("Êtes-vous sûr de vouloir supprimer définitivement:\n\n" +
                adherent.getPrenom() + " " + adherent.getNom() +
                " (" + adherent.getMatricule() + ")\n\n" +
                "⚠️ Cette action est irréversible!");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                adherentService.supprimer(adherent.getId());
                chargerAdherents();
                logger.info("✅ Adhérent supprimé: " + adherent.getMatricule());
                afficherSucces("Succès", "Adhérent supprimé avec succès!");
            } catch (Exception e) {
                logger.error("❌ Erreur lors de la suppression", e);
                afficherErreur("Erreur: " + e.getMessage());
            }
        }
    }
}