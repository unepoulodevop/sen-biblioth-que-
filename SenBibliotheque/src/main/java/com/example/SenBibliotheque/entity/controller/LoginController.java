package com.example.SenBibliotheque.controller;

import com.example.SenBibliotheque.entity.Utilisateur;
import com.example.SenBibliotheque.service.UtilisateurService;
import com.example.SenBibliotheque.util.SessionManager;
import com.example.SenBibliotheque.util.WindowConfig;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    // ===== FXML FIELDS =====
    @FXML
    private TextField loginField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginBtn;

    @FXML
    private CheckBox rememberMeCheckbox;

    @FXML
    private ProgressIndicator loadingIndicator;

    @FXML
    private Label errorLabel;

    @FXML
    private Hyperlink forgotPasswordLink;

    // ===== SERVICE =====
    private UtilisateurService utilisateurService;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        utilisateurService = new UtilisateurService();

        // Initialiser les styles
        applyStyles();

        // Effacer le message d'erreur au démarrage
        errorLabel.setText("");

        // Configurer les écouteurs d'événements
        setupEventListeners();

        // Charger les identifiants mémorisés si disponibles
        loadSavedCredentials();

        // Focus initial sur le champ de login
        Platform.runLater(() -> {
            loginField.requestFocus();
            loginField.setCursor(javafx.scene.Cursor.TEXT);
        });

        logger.info("✅ LoginController initialisé");
    }

    /**
     * Applique les classes CSS aux nœuds
     */
    private void applyStyles() {
        if (loginField != null) {
            loginField.getStyleClass().add("login-text-field");
        }
        if (passwordField != null) {
            passwordField.getStyleClass().add("login-password-field");
        }
        if (loginBtn != null) {
            loginBtn.getStyleClass().add("login-button");
        }
        if (errorLabel != null) {
            errorLabel.getStyleClass().add("login-error-label");
        }
        if (rememberMeCheckbox != null) {
            rememberMeCheckbox.getStyleClass().add("login-remember-checkbox");
        }
        if (loadingIndicator != null) {
            loadingIndicator.getStyleClass().add("login-loading-indicator");
        }
        if (forgotPasswordLink != null) {
            forgotPasswordLink.getStyleClass().add("login-forgot-password");
        }
    }

    /**
     * Configure les écouteurs d'événements pour les champs
     */
    private void setupEventListeners() {
        // Appuyer sur Entrée pour se connecter
        loginField.setOnKeyPressed(this::handleKeyPressed);
        passwordField.setOnKeyPressed(this::handleKeyPressed);

        // Effacer le message d'erreur lors de la saisie
        loginField.textProperty().addListener((obs, oldVal, newVal) -> clearError());
        passwordField.textProperty().addListener((obs, oldVal, newVal) -> clearError());
    }

    /**
     * Gère la pression sur les touches du clavier
     */
    private void handleKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            handleLogin();
        }
    }

    /**
     * Charge les identifiants sauvegardés depuis les préférences
     */
    private void loadSavedCredentials() {
        try {
            String savedLogin = getPreference("saved_login", "");
            boolean rememberMe = Boolean.parseBoolean(getPreference("remember_me", "false"));

            if (rememberMe && !savedLogin.isEmpty()) {
                loginField.setText(savedLogin);
                if (rememberMeCheckbox != null) {
                    rememberMeCheckbox.setSelected(true);
                }
                passwordField.requestFocus();
            }
        } catch (Exception e) {
            logger.warn("⚠️ Erreur lors du chargement des identifiants mémorisés: " + e.getMessage());
        }
    }

    /**
     * Sauvegarde les identifiants si "Se souvenir de moi" est coché
     */
    private void saveCredentials() {
        if (rememberMeCheckbox != null && rememberMeCheckbox.isSelected()) {
            savePreference("saved_login", loginField.getText());
            savePreference("remember_me", "true");
        } else {
            savePreference("saved_login", "");
            savePreference("remember_me", "false");
        }
    }

    /**
     * Gère l'action de connexion
     */
    @FXML
    private void handleLogin() {
        String login = loginField.getText().trim();
        String password = passwordField.getText();

        // Validation des champs
        if (!validateInput(login, password)) {
            return;
        }

        // Désactiver le bouton et afficher le chargement
        loginBtn.setDisable(true);
        loadingIndicator.setVisible(true);

        // Authentification en thread séparé
        Thread authThread = new Thread(() -> {
            try {
                Utilisateur utilisateur = utilisateurService.authentifier(login, password);

                Platform.runLater(() -> {
                    if (utilisateur != null) {
                        // Sauvegarder la session
                        SessionManager.getInstance().setUtilisateurConnecte(utilisateur);

                        logger.info("✅ Connexion réussie: " + utilisateur.getLogin());

                        // Vérifier si le mot de passe doit être changé
                        if (utilisateur.getForceChangementMdp()) {
                            afficherDialogueChangementMdp(utilisateur);
                        } else {
                            saveCredentials();
                            chargerDashboard();
                        }
                    } else {
                        afficherErreur("Identifiant ou mot de passe incorrect!");
                        loginBtn.setDisable(false);
                        loadingIndicator.setVisible(false);
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    logger.error("❌ Erreur lors de l'authentification", e);
                    afficherErreur("Erreur de connexion: " + e.getMessage());
                    loginBtn.setDisable(false);
                    loadingIndicator.setVisible(false);
                });
            }
        });

        authThread.setDaemon(true);
        authThread.start();
    }

    /**
     * Valide les entrées utilisateur
     */
    private boolean validateInput(String login, String password) {
        if (login.isEmpty()) {
            afficherErreur("Veuillez entrer votre identifiant");
            loginField.requestFocus();
            return false;
        }

        if (password.isEmpty()) {
            afficherErreur("Veuillez entrer votre mot de passe");
            passwordField.requestFocus();
            return false;
        }

        if (login.length() < 3) {
            afficherErreur("L'identifiant doit contenir au moins 3 caractères");
            return false;
        }

        if (password.length() < 6) {
            afficherErreur("Le mot de passe doit contenir au moins 6 caractères");
            return false;
        }

        clearError();
        return true;
    }

    /**
     * Affiche un message d'erreur
     */
    private void afficherErreur(String message) {
        errorLabel.setText("❌ " + message);
        errorLabel.getStyleClass().removeAll("login-error-label", "login-success-label");
        errorLabel.getStyleClass().add("login-error-label");
    }

    /**
     * Affiche un message d'information
     */
    private void afficherInfo(String message) {
        errorLabel.setText("✅ " + message);
        errorLabel.getStyleClass().removeAll("login-error-label", "login-success-label");
        errorLabel.getStyleClass().add("login-success-label");
    }

    /**
     * Efface le message d'erreur
     */
    private void clearError() {
        if (!errorLabel.getText().isEmpty()) {
            errorLabel.setText("");
        }
    }

    /**
     * Affiche le dialogue de changement de mot de passe
     */
    private void afficherDialogueChangementMdp(Utilisateur utilisateur) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Changement de mot de passe requis");
        dialog.setHeaderText("Première connexion - Changez votre mot de passe");

        PasswordField nouveauMdpField = new PasswordField();
        nouveauMdpField.setPromptText("Nouveau mot de passe");
        nouveauMdpField.getStyleClass().add("login-password-field");

        PasswordField confirmMdpField = new PasswordField();
        confirmMdpField.setPromptText("Confirmez le mot de passe");
        confirmMdpField.getStyleClass().add("login-password-field");

        VBox vbox = new VBox(10);
        vbox.setStyle("-fx-padding: 20;");
        vbox.getChildren().addAll(
                new Label("Veuillez définir un nouveau mot de passe:"),
                nouveauMdpField,
                confirmMdpField
        );

        dialog.getDialogPane().setContent(vbox);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                String mdp1 = nouveauMdpField.getText();
                String mdp2 = confirmMdpField.getText();

                if (mdp1.isEmpty() || mdp2.isEmpty()) {
                    afficherAlerte("Erreur", "Veuillez remplir tous les champs");
                    return null;
                }

                if (!mdp1.equals(mdp2)) {
                    afficherAlerte("Erreur", "Les mots de passe ne correspondent pas");
                    return null;
                }

                if (mdp1.length() < 8) {
                    afficherAlerte("Erreur", "Le mot de passe doit contenir au moins 8 caractères");
                    return null;
                }

                return mdp1;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(nouveauMdp -> {
            try {
                utilisateurService.changerMotDePasse(utilisateur.getId(), passwordField.getText(), nouveauMdp);
                afficherInfo("Mot de passe changé avec succès!");

                // Attendre un moment avant de charger le dashboard
                new Thread(() -> {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        logger.error("❌ Erreur d'interruption", e);
                    }
                    Platform.runLater(this::chargerDashboard);
                }).start();

            } catch (Exception e) {
                afficherErreur("Erreur: " + e.getMessage());
                loginBtn.setDisable(false);
                loadingIndicator.setVisible(false);
            }
        });
    }

    /**
     * Gère l'action "Mot de passe oublié" (Admin uniquement)
     */
    @FXML
    private void handleForgotPassword() {
        logger.info("🔑 Clique sur 'Mot de passe oublié'");
        afficherDialogueRechercheMdpOublie();
    }

    /**
     * Affiche le dialogue de recherche pour mot de passe oublié
     */
    private void afficherDialogueRechercheMdpOublie() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Réinitialiser le mot de passe");
        dialog.setHeaderText("Récupération du compte administrateur");

        TextField identifiantField = new TextField();
        identifiantField.setPromptText("Entrez votre identifiant ou email");
        identifiantField.getStyleClass().add("login-text-field");

        Label infoLabel = new Label("⚠️ Cette fonction est réservée aux administrateurs uniquement");
        infoLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #6b7280; -fx-wrap-text: true;");

        VBox vbox = new VBox(10);
        vbox.setStyle("-fx-padding: 20;");
        vbox.getChildren().addAll(
                new Label("Pour réinitialiser votre mot de passe, entrez votre identifiant ou email:"),
                identifiantField,
                infoLabel
        );

        dialog.getDialogPane().setContent(vbox);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                return identifiantField.getText().trim();
            }
            return null;
        });

        dialog.showAndWait().ifPresent(identifiant -> {
            if (!identifiant.isEmpty()) {
                traiterResetMotDePasse(identifiant);
            }
        });
    }

    /**
     * Traite la réinitialisation du mot de passe (Admin uniquement)
     */
    private void traiterResetMotDePasse(String identifiant) {
        loadingIndicator.setVisible(true);

        Thread resetThread = new Thread(() -> {
            try {
                // Chercher l'utilisateur
                Utilisateur utilisateur = utilisateurService.rechercherParLoginOuEmail(identifiant);

                Platform.runLater(() -> {
                    loadingIndicator.setVisible(false);

                    if (utilisateur == null) {
                        afficherAlerte("Non trouvé",
                                "❌ Aucun compte trouvé avec cet identifiant ou email.\n\n" +
                                        "Vérifiez votre saisie et réessayez.");
                        return;
                    }

                    // Vérifier que c'est un admin
                    if (!"ADMIN".equals(utilisateur.getProfil())) {
                        afficherAlerte("Accès refusé",
                                "❌ Seuls les administrateurs peuvent réinitialiser leur mot de passe via cette interface.\n\n" +
                                        "Contactez un administrateur système.");
                        logger.warn("🚫 Tentative non autorisée de reset pour: " + identifiant + " (Profil: " + utilisateur.getProfil() + ")");
                        return;
                    }

                    // Afficher le dialogue de réinitialisation
                    afficherDialogueResetMdpAdmin(utilisateur);
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    loadingIndicator.setVisible(false);
                    logger.error("❌ Erreur lors de la recherche de l'utilisateur", e);
                    afficherAlerte("Erreur", "❌ Une erreur s'est produite: " + e.getMessage());
                });
            }
        });

        resetThread.setDaemon(true);
        resetThread.start();
    }

    /**
     * Affiche le dialogue de réinitialisation du mot de passe pour un admin
     */
    private void afficherDialogueResetMdpAdmin(Utilisateur utilisateur) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Réinitialiser le mot de passe");
        dialog.setHeaderText("Réinitialiser le mot de passe pour: " + utilisateur.getNom() + " " + utilisateur.getPrenom());

        PasswordField nouveauMdpField = new PasswordField();
        nouveauMdpField.setPromptText("Nouveau mot de passe (min 8 caractères)");
        nouveauMdpField.getStyleClass().add("login-password-field");

        PasswordField confirmMdpField = new PasswordField();
        confirmMdpField.setPromptText("Confirmez le mot de passe");
        confirmMdpField.getStyleClass().add("login-password-field");

        Label passwordStrengthLabel = new Label();
        passwordStrengthLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #6b7280;");

        Label requirementsLabel = new Label("Exigences: 8 caractères, majuscules, minuscules, chiffres");
        requirementsLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #9ca3af; -fx-wrap-text: true;");

        // Indicateur de force du mot de passe en temps réel
        nouveauMdpField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.isEmpty()) {
                passwordStrengthLabel.setText("");
            } else {
                String strength = evaluerForceMotDePasse(newVal);
                passwordStrengthLabel.setText("Force: " + strength);
            }
        });

        VBox vbox = new VBox(10);
        vbox.setStyle("-fx-padding: 20;");
        vbox.getChildren().addAll(
                new Label("Nouveau mot de passe:"),
                nouveauMdpField,
                passwordStrengthLabel,
                requirementsLabel,
                new Label("Confirmez le mot de passe:"),
                confirmMdpField
        );

        dialog.getDialogPane().setContent(vbox);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.setWidth(400);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                String mdp1 = nouveauMdpField.getText();
                String mdp2 = confirmMdpField.getText();

                if (mdp1.isEmpty() || mdp2.isEmpty()) {
                    afficherAlerte("Erreur", "❌ Veuillez remplir tous les champs");
                    return null;
                }

                if (!mdp1.equals(mdp2)) {
                    afficherAlerte("Erreur", "❌ Les mots de passe ne correspondent pas");
                    return null;
                }

                if (mdp1.length() < 8) {
                    afficherAlerte("Erreur", "❌ Le mot de passe doit contenir au moins 8 caractères");
                    return null;
                }

                if (!mdp1.matches(".*[A-Z].*")) {
                    afficherAlerte("Erreur", "❌ Le mot de passe doit contenir au moins une majuscule");
                    return null;
                }

                if (!mdp1.matches(".*[a-z].*")) {
                    afficherAlerte("Erreur", "❌ Le mot de passe doit contenir au moins une minuscule");
                    return null;
                }

                if (!mdp1.matches(".*[0-9].*")) {
                    afficherAlerte("Erreur", "❌ Le mot de passe doit contenir au moins un chiffre");
                    return null;
                }

                return mdp1;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(nouveauMdp -> {
            loadingIndicator.setVisible(true);

            Thread resetThread = new Thread(() -> {
                try {
                    // Réinitialiser le mot de passe
                    utilisateurService.resetMotDePasse(utilisateur.getId(), nouveauMdp);

                    Platform.runLater(() -> {
                        loadingIndicator.setVisible(false);
                        afficherAlerte("✅ Succès",
                                "Mot de passe réinitialisé avec succès!\n\n" +
                                        "Vous pouvez maintenant vous connecter avec votre nouveau mot de passe.");
                        logger.info("✅ Mot de passe réinitialisé pour: " + utilisateur.getLogin());
                    });

                } catch (Exception e) {
                    Platform.runLater(() -> {
                        loadingIndicator.setVisible(false);
                        logger.error("❌ Erreur lors de la réinitialisation", e);
                        afficherAlerte("❌ Erreur", "Erreur lors de la réinitialisation:\n" + e.getMessage());
                    });
                }
            });

            resetThread.setDaemon(true);
            resetThread.start();
        });
    }

    /**
     * Évalue la force d'un mot de passe
     */
    private String evaluerForceMotDePasse(String password) {
        int score = 0;

        if (password.length() >= 8) score++;
        if (password.length() >= 12) score++;
        if (password.matches(".*[a-z].*")) score++;
        if (password.matches(".*[A-Z].*")) score++;
        if (password.matches(".*[0-9].*")) score++;
        if (password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) score++;

        switch (score) {
            case 1:
            case 2:
                return "🔴 Faible";
            case 3:
            case 4:
                return "🟡 Moyen";
            case 5:
            case 6:
                return "🟢 Fort";
            default:
                return "Faible";
        }
    }

    /**
     * Charge le dashboard approprié selon le profil de l'utilisateur
     */
    /**
     * Charge le dashboard approprié selon le profil de l'utilisateur
     */
    /**
     * Charge le dashboard approprié selon le profil de l'utilisateur
     */
    private void chargerDashboard() {
        try {
            Utilisateur utilisateur = SessionManager.getInstance().getUtilisateurConnecte();
            Stage stage = (Stage) loginBtn.getScene().getWindow();

            // ✅ CONFIGURER LA STAGE POUR LE DASHBOARD AVANT DE CHARGER
            WindowConfig.configureDashboardStage(stage);

            // Déterminer le type de dashboard selon le profil
            String fxmlPath;
            if ("ADMIN".equals(utilisateur.getProfil())) {
                fxmlPath = "/fxml/Dashboard.fxml";
            } else if ("BIBLIOTHECAIRE".equals(utilisateur.getProfil())) {
                fxmlPath = "/fxml/DashboardBibliothecaire.fxml";
            } else {
                throw new RuntimeException("Profil utilisateur non reconnu: " + utilisateur.getProfil());
            }

            // ✅ CHARGER LE DASHBOARD
            FXMLLoader loader = new FXMLLoader(LoginController.class.getResource(fxmlPath));
            BorderPane dashboard = loader.load();

            // ✅ CRÉER LA SCENE SANS DIMENSIONS (elle utilisera celles de la stage)
            Scene scene = new Scene(dashboard);

            // ✅ APPLIQUER LES STYLESHEETS
            scene.getStylesheets().addAll(
                    getClass().getResource("/css/style.css").toExternalForm(),
                    getClass().getResource("/css/login.css").toExternalForm(),
                    getClass().getResource("/css/sidebar.css").toExternalForm(),
                    getClass().getResource("/css/dashboard.css").toExternalForm()
            );

            // ✅ DÉFINIR LA SCENE
            stage.setScene(scene);

            // ✅ PASSER LA STAGE AU CONTRÔLEUR
            if ("ADMIN".equals(utilisateur.getProfil())) {
                DashboardController controller = loader.getController();
                controller.setStage(stage);
            } else {
                DashboardBibliothecaireController controller = loader.getController();
                controller.setStage(stage);
            }

            logger.info("✅ Dashboard chargé pour le profil: " + utilisateur.getProfil());

        } catch (Exception e) {
            logger.error("❌ Erreur lors du chargement du dashboard", e);
            afficherErreur("Erreur lors du chargement de l'application: " + e.getMessage());
            loginBtn.setDisable(false);
            loadingIndicator.setVisible(false);
        }
    }

    /**
     * Affiche une alerte d'information
     */
    private void afficherAlerte(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Récupère une préférence utilisateur
     */
    private String getPreference(String key, String defaultValue) {
        return System.getProperty("bookhome." + key, defaultValue);
    }

    /**
     * Sauvegarde une préférence utilisateur
     */
    private void savePreference(String key, String value) {
        System.setProperty("bookhome." + key, value);
    }

    @FXML
    public void onLoginFieldClicked() {
        loginField.setCursor(javafx.scene.Cursor.TEXT);
        loginField.requestFocus();
    }

    @FXML
    public void onPasswordFieldClicked() {
        passwordField.setCursor(javafx.scene.Cursor.TEXT);
        passwordField.requestFocus();
    }
}