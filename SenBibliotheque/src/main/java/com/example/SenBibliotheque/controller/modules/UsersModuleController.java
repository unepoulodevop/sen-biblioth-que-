package com.example.SenBibliotheque.controller.modules;

import com.example.SenBibliotheque.entity.Utilisateur;
import com.example.SenBibliotheque.service.UtilisateurService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class UsersModuleController {

    private static final Logger logger = LoggerFactory.getLogger(UsersModuleController.class);

    @FXML
    private TableView<Utilisateur> usersTable;
    @FXML
    private TextField searchField;
    @FXML
    private Label statusLabel;
    @FXML
    private Button ajouterBtn;

    private UtilisateurService utilisateurService;
    private ObservableList<Utilisateur> utilisateurs;

    @FXML
    public void initialize() {
        try {
            utilisateurService = new UtilisateurService();
            utilisateurs = FXCollections.observableArrayList();

            // Configurer la table
            configurerTable();

            // Charger les utilisateurs
            chargerUtilisateurs();
        } catch (Exception e) {
            logger.error("❌ Erreur lors de l'initialisation du module utilisateurs", e);
        }
    }

    private void configurerTable() {
        // Colonne Login
        TableColumn<Utilisateur, String> loginCol = new TableColumn<>("Login");
        loginCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getLogin()));
        loginCol.setPrefWidth(100);

        // Colonne Nom
        TableColumn<Utilisateur, String> nomCol = new TableColumn<>("Nom");
        nomCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getNom()));
        nomCol.setPrefWidth(100);

        // Colonne Prénom
        TableColumn<Utilisateur, String> prenomCol = new TableColumn<>("Prénom");
        prenomCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getPrenom()));
        prenomCol.setPrefWidth(100);

        // Colonne Email
        TableColumn<Utilisateur, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getEmail()));
        emailCol.setPrefWidth(150);

        // Colonne Profil
        TableColumn<Utilisateur, String> profilCol = new TableColumn<>("Profil");
        profilCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getProfil()));
        profilCol.setPrefWidth(100);

        // Colonne Statut
        TableColumn<Utilisateur, String> statutCol = new TableColumn<>("Statut");
        statutCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getActif() ? "✅ Actif" : "❌ Inactif"));
        statutCol.setPrefWidth(90);

        // Colonne Actions
        TableColumn<Utilisateur, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setPrefWidth(350);
        actionsCol.setCellFactory(param -> new ActionCell());

        usersTable.getColumns().addAll(loginCol, nomCol, prenomCol, emailCol, profilCol, statutCol, actionsCol);
        usersTable.setItems(utilisateurs);
    }

    private void chargerUtilisateurs() {
        try {
            List<Utilisateur> list = utilisateurService.obtenirTous();
            utilisateurs.clear();
            utilisateurs.addAll(list);
            statusLabel.setText(list.size() + " utilisateur(s)");
            logger.info("✅ Utilisateurs chargés: " + list.size());
        } catch (Exception e) {
            logger.error("❌ Erreur lors du chargement des utilisateurs", e);
        }
    }

    @FXML
    private void ajouterUtilisateur() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dialogs/AddUserDialog.fxml"));
            ScrollPane scrollPane = loader.load();
            AddUserDialogController controller = loader.getController();

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.getDialogPane().setContent(scrollPane);
            dialog.setTitle("Créer un nouvel utilisateur");
            dialog.setWidth(830);
            dialog.setHeight(500);

            dialog.getDialogPane().getButtonTypes().addAll(
                    new ButtonType("✅ Créer", ButtonBar.ButtonData.OK_DONE),
                    new ButtonType("❌ Annuler", ButtonBar.ButtonData.CANCEL_CLOSE)
            );

            dialog.initOwner(usersTable.getScene().getWindow());

            Optional<ButtonType> result = dialog.showAndWait();

            if (result.isPresent() && result.get().getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                if (!controller.validateForm()) {
                    return;
                }

                Utilisateur nouvelUtilisateur = new Utilisateur(
                        controller.getLogin(),
                        BCrypt.hashpw(controller.getPassword(), BCrypt.gensalt()),
                        controller.getNom(),
                        controller.getPrenom(),
                        controller.getEmail(),
                        controller.getProfil()
                );
                nouvelUtilisateur.setForceChangementMdp(controller.isForcePasswordChange());
                nouvelUtilisateur.setActif("Actif".equals(controller.getStatut()));

                utilisateurService.update(nouvelUtilisateur);

                logger.info("✅ Utilisateur créé: " + controller.getLogin());

                afficherSucces("Utilisateur créé avec succès!",
                        "Login: " + controller.getLogin() + "\n" +
                                "Mot de passe temporaire: " + controller.getPassword() + "\n\n" +
                                "L'utilisateur devra changer son mot de passe à la première connexion.");

                chargerUtilisateurs();
            }

        } catch (IOException e) {
            logger.error("❌ Erreur lors du chargement du dialogue", e);
            afficherErreur("Erreur lors de l'ouverture du formulaire");
        } catch (Exception e) {
            logger.error("❌ Erreur lors de la création de l'utilisateur", e);
            afficherErreur("Erreur: " + e.getMessage());
        }
    }

    @FXML
    private void rechercher() {
        String terme = searchField.getText().trim();

        if (terme.isEmpty()) {
            chargerUtilisateurs();
            return;
        }

        try {
            List<Utilisateur> resultats = utilisateurService.obtenirTous().stream()
                    .filter(u -> u.getLogin().toLowerCase().contains(terme.toLowerCase()) ||
                            u.getNom().toLowerCase().contains(terme.toLowerCase()) ||
                            u.getPrenom().toLowerCase().contains(terme.toLowerCase()) ||
                            u.getEmail().toLowerCase().contains(terme.toLowerCase()))
                    .toList();

            utilisateurs.clear();
            utilisateurs.addAll(resultats);
            statusLabel.setText(resultats.size() + " utilisateur(s) trouvé(s)");

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
    private class ActionCell extends TableCell<Utilisateur, Void> {
        @Override
        protected void updateItem(Void item, boolean empty) {
            super.updateItem(item, empty);

            if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                setGraphic(null);
                return;
            }

            Utilisateur utilisateur = getTableRow().getItem();

            // Bouton Modifier
            Button modifierBtn = createActionButton("✏️ Modifier", "#3b82f6", "Modifier cet utilisateur");
            modifierBtn.setOnAction(e -> modifierUtilisateur(utilisateur));

            // Bouton Activer/Désactiver
            String btnText = utilisateur.getActif() ? "🔒 Désactiver" : "🔓 Activer";
            String btnColor = utilisateur.getActif() ? "#f59e0b" : "#10b981";
            String tooltip = utilisateur.getActif() ? "Désactiver ce compte" : "Activer ce compte";
            Button activerBtn = createActionButton(btnText, btnColor, tooltip);
            activerBtn.setOnAction(e -> basculerStatutUtilisateur(utilisateur));

            // Bouton Réinitialiser mot de passe
            Button reinitBtn = createActionButton("🔑 Réinit MDP", "#9333ea", "Réinitialiser le mot de passe");
            reinitBtn.setOnAction(e -> reinitialiserMotDePasse(utilisateur));

            // Bouton Supprimer
            Button supprimerBtn = createActionButton("🗑️ Supprimer", "#dc2626", "Supprimer cet utilisateur");
            supprimerBtn.setOnAction(e -> supprimerUtilisateur(utilisateur));

            HBox hBox = new HBox(5);
            hBox.setStyle("-fx-alignment: CENTER_LEFT;");
            hBox.getChildren().addAll(modifierBtn, activerBtn, reinitBtn, supprimerBtn);
            setGraphic(hBox);
        }

        private Button createActionButton(String text, String color, String tooltip) {
            Button btn = new Button(text);
            btn.setStyle(
                    "-fx-font-size: 10; " +
                            "-fx-background-color: " + color + "; " +
                            "-fx-text-fill: white; " +
                            "-fx-padding: 6 12; " +
                            "-fx-border-radius: 5; " +
                            "-fx-background-radius: 5; " +
                            "-fx-cursor: hand; " +
                            "-fx-font-weight: bold; " +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 3, 0, 0, 1);"
            );

            // Ajouter le tooltip
            Tooltip tip = new Tooltip(tooltip);
            tip.setStyle("-fx-font-size: 10; -fx-text-fill: #ffffff; -fx-background-color: #1f2937; -fx-padding: 8;");
            Tooltip.install(btn, tip);

            // Effet au survol
            btn.setOnMouseEntered(e -> btn.setStyle(
                    "-fx-font-size: 10; " +
                            "-fx-background-color: " + lightenColor(color) + "; " +
                            "-fx-text-fill: white; " +
                            "-fx-padding: 6 12; " +
                            "-fx-border-radius: 5; " +
                            "-fx-background-radius: 5; " +
                            "-fx-cursor: hand; " +
                            "-fx-font-weight: bold; " +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 5, 0, 0, 2);"
            ));

            btn.setOnMouseExited(e -> btn.setStyle(
                    "-fx-font-size: 10; " +
                            "-fx-background-color: " + color + "; " +
                            "-fx-text-fill: white; " +
                            "-fx-padding: 6 12; " +
                            "-fx-border-radius: 5; " +
                            "-fx-background-radius: 5; " +
                            "-fx-cursor: hand; " +
                            "-fx-font-weight: bold; " +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 3, 0, 0, 1);"
            ));

            return btn;
        }

        private String lightenColor(String color) {
            return switch (color) {
                case "#3b82f6" -> "#60a5fa";
                case "#f59e0b" -> "#fbbf24";
                case "#10b981" -> "#34d399";
                case "#9333ea" -> "#a855f7";
                case "#dc2626" -> "#ef4444";
                default -> color;
            };
        }
    }

    // ===== ACTIONS SUR LES UTILISATEURS =====
    private void modifierUtilisateur(Utilisateur utilisateur) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dialogs/AddUserDialog.fxml"));
            ScrollPane scrollPane = loader.load();
            AddUserDialogController controller = loader.getController();

            controller.setUtilisateur(utilisateur);

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.getDialogPane().setContent(scrollPane);
            dialog.setTitle("Modifier l'utilisateur");
            dialog.setWidth(860);
            dialog.setHeight(470);

            dialog.getDialogPane().getButtonTypes().addAll(
                    new ButtonType("✅ Enregistrer", ButtonBar.ButtonData.OK_DONE),
                    new ButtonType("❌ Annuler", ButtonBar.ButtonData.CANCEL_CLOSE)
            );

            dialog.initOwner(usersTable.getScene().getWindow());

            Optional<ButtonType> result = dialog.showAndWait();

            if (result.isPresent() && result.get().getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                if (!controller.validateForm()) {
                    return;
                }

                utilisateur.setNom(controller.getNom());
                utilisateur.setPrenom(controller.getPrenom());
                utilisateur.setEmail(controller.getEmail());
                utilisateur.setProfil(controller.getProfil());
                utilisateur.setActif("Actif".equals(controller.getStatut()));

                utilisateurService.update(utilisateur);

                logger.info("✅ Utilisateur modifié: " + utilisateur.getLogin());
                afficherSucces("Succès", "Utilisateur modifié avec succès!");

                chargerUtilisateurs();
            }

        } catch (IOException e) {
            logger.error("❌ Erreur lors du chargement du dialogue", e);
            afficherErreur("Erreur lors de l'ouverture du formulaire");
        } catch (Exception e) {
            logger.error("❌ Erreur lors de la modification de l'utilisateur", e);
            afficherErreur("Erreur: " + e.getMessage());
        }
    }

    private void basculerStatutUtilisateur(Utilisateur utilisateur) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation");
        confirmation.setHeaderText("Modifier le statut?");

        String action = utilisateur.getActif() ? "désactiver" : "activer";
        confirmation.setContentText("Êtes-vous sûr de vouloir " + action + " le compte de " + utilisateur.getLogin() + "?");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                utilisateur.setActif(!utilisateur.getActif());
                utilisateurService.update(utilisateur);

                String message = utilisateur.getActif() ? "activé" : "désactivé";
                logger.info("✅ Compte " + message + ": " + utilisateur.getLogin());
                afficherSucces("Succès", "Compte " + message + " avec succès!");

                chargerUtilisateurs();
            } catch (Exception e) {
                logger.error("❌ Erreur lors du basculement du statut", e);
                afficherErreur("Erreur: " + e.getMessage());
            }
        }
    }

    private void reinitialiserMotDePasse(Utilisateur utilisateur) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Réinitialiser le mot de passe");
        confirmation.setHeaderText("Réinitialiser le mot de passe?");
        confirmation.setContentText("Un nouveau mot de passe temporaire sera généré et l'utilisateur devra le changer à la prochaine connexion.\n\nConfirmer?");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                String nouveauMdp = utilisateurService.reinitialiserMotDePasse(utilisateur.getId());

                logger.info("✅ Mot de passe réinitialisé pour: " + utilisateur.getLogin());

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Mot de passe réinitialisé");
                alert.setHeaderText("Nouveau mot de passe généré");
                alert.setContentText("Login: " + utilisateur.getLogin() + "\n" +
                        "Nouveau mot de passe: " + nouveauMdp + "\n\n" +
                        "⚠️ À communiquer à l'utilisateur\n" +
                        "L'utilisateur devra changer ce mot de passe à sa prochaine connexion.");
                alert.showAndWait();

                chargerUtilisateurs();
            } catch (Exception e) {
                logger.error("❌ Erreur lors de la réinitialisation du mot de passe", e);
                afficherErreur("Erreur: " + e.getMessage());
            }
        }
    }

    private void supprimerUtilisateur(Utilisateur utilisateur) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation de suppression");
        confirmation.setHeaderText("Supprimer l'utilisateur?");
        confirmation.setContentText("Êtes-vous sûr de vouloir supprimer définitivement:\n\n" +
                utilisateur.getPrenom() + " " + utilisateur.getNom() +
                " (" + utilisateur.getLogin() + ")\n\n" +
                "⚠️ Cette action est irréversible!");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                utilisateurService.supprimer(utilisateur.getId());
                chargerUtilisateurs();
                logger.info("✅ Utilisateur supprimé: " + utilisateur.getLogin());
                afficherSucces("Succès", "Utilisateur supprimé avec succès!");
            } catch (Exception e) {
                logger.error("❌ Erreur lors de la suppression", e);
                afficherErreur("Erreur lors de la suppression: " + e.getMessage());
            }
        }
    }
}