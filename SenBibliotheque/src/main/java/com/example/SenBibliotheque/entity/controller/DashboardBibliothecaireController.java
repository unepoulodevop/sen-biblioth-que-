package com.example.SenBibliotheque.entity.controller;

import com.example.SenBibliotheque.entity.Utilisateur;
import com.example.SenBibliotheque.util.SessionManager;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class DashboardBibliothecaireController {

    private static final Logger logger = LoggerFactory.getLogger(DashboardBibliothecaireController.class);

    @FXML
    private VBox sidebarPane;
    @FXML
    private StackPane contentPane;
    @FXML
    private Label pageTitle, userNameLabel, userRoleLabel, clockLabel;
    @FXML
    private Button dashboardBtn, booksBtn, membersBtn, borrowsBtn, logoutBtn, collapseBtn;
    @FXML
    private HBox titleBar;
    @FXML
    private Button minimizeBtn, maximizeBtn, closeBtn;

    private boolean sidebarExpanded = true;
    private boolean isMaximized = false;
    private static final int SIDEBAR_WIDTH = 250;
    private static final int SIDEBAR_COLLAPSED_WIDTH = 60;
    private Stage stage;
    private Thread clockThread;

    @FXML
    public void initialize() {
        logger.info("🔍 Initialisation du DashboardBibliothecaireController...");

        // Configurer le profil utilisateur
        Utilisateur utilisateur = SessionManager.getInstance().getUtilisateurConnecte();

        if (utilisateur != null) {
            userNameLabel.setText(utilisateur.getPrenom() + " " + utilisateur.getNom());
            userRoleLabel.setText("Bibliothécaire - Accès restreint");
        }

        // Initialiser l'horloge
        updateClock();

        // Charger le dashboard initial SIMPLIFIÉ
        loadDashboard();
        setActiveButton(dashboardBtn);

        logger.info("✅ DashboardBibliothecaireController initialisé avec succès");
    }

    // ===== GESTION DE LA FENÊTRE (TopBar) =====
    @FXML
    private void minimizeWindow() {
        if (stage != null) {
            stage.setIconified(true);
            logger.info("✅ Fenêtre minimisée");
        }
    }

    @FXML
    private void maximizeWindow() {
        if (stage != null) {
            isMaximized = !isMaximized;
            stage.setMaximized(isMaximized);
            maximizeBtn.setText(isMaximized ? "❒" : "□");
            logger.info("✅ Fenêtre " + (isMaximized ? "maximisée" : "restaurée"));
        }
    }

    @FXML
    private void closeWindow() {
        if (stage != null) {
            stage.close();
            logger.info("✅ Application fermée");
        }
    }

    // ===== NAVIGATION =====
    @FXML
    private void loadDashboard() {
        // ✅ MODIFICATION : Charger le dashboard simplifié pour bibliothécaire
        loadModule("/fxml/modules/DashboardModuleBibliothecaire.fxml", "Dashboard Bibliothécaire");
        setActiveButton(dashboardBtn);
    }

    @FXML
    private void loadBooks() {
        // ✅ Consultation seule, pas de suppression
        loadModule("/fxml/modules/BooksModuleConsultation.fxml", "Consultation des Livres");
        setActiveButton(booksBtn);
    }

    @FXML
    private void loadMembers() {
        // ✅ Gestion complète des adhérents
        loadModule("/fxml/modules/MembersModule.fxml", "Gestion des Adhérents");
        setActiveButton(membersBtn);
    }

    @FXML
    private void loadBorrows() {
        // ✅ Gestion complète des emprunts
        loadModule("/fxml/modules/BorrowsModule.fxml", "Gestion des Emprunts");
        setActiveButton(borrowsBtn);
    }

    @FXML
    private void logout() {
        try {
            // Arrêter le thread d'horloge
            if (clockThread != null && clockThread.isAlive()) {
                clockThread.interrupt();
            }

            SessionManager.getInstance().deconnecter();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/LoginScreen.fxml"));
            Scene scene = new Scene(loader.load(), 600, 400);

            scene.getStylesheets().addAll(
                    getClass().getResource("/css/style.css").toExternalForm(),
                    getClass().getResource("/css/login.css").toExternalForm()
            );

            Stage stage = (Stage) logoutBtn.getScene().getWindow();
            stage.setScene(scene);
            stage.setWidth(600);
            stage.setHeight(400);
            stage.setResizable(false);
            stage.centerOnScreen();

            logger.info("✅ Déconnexion réussie");
        } catch (IOException e) {
            logger.error("❌ Erreur lors de la déconnexion", e);
        }
    }

    // ===== CHARGEMENT DYNAMIQUE DES MODULES =====
    private void loadModule(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(DashboardBibliothecaireController.class.getResource(fxmlPath));
            Node module = loader.load();

            contentPane.getChildren().clear();
            contentPane.getChildren().add(module);
            pageTitle.setText(title);

            logger.info("✅ Module chargé: " + title);
        } catch (IOException e) {
            logger.error("❌ Erreur lors du chargement du module: " + fxmlPath, e);
            pageTitle.setText("❌ Erreur de chargement");
        }
    }

    // ===== SIDEBAR COLLAPSIBLE =====
    @FXML
    private void toggleSidebar() {
        TranslateTransition transition = new TranslateTransition(Duration.millis(300), sidebarPane);

        if (sidebarExpanded) {
            transition.setToX(-SIDEBAR_COLLAPSED_WIDTH);
            collapseBtn.setText("◀");
            sidebarExpanded = false;
        } else {
            transition.setToX(0);
            collapseBtn.setText("▶");
            sidebarExpanded = true;
        }

        transition.play();
    }

    // ===== ACTIVE BUTTON HIGHLIGHT =====
    private void setActiveButton(Button activeButton) {
        for (Button btn : new Button[]{dashboardBtn, booksBtn, membersBtn, borrowsBtn}) {
            btn.setStyle("-fx-padding: 12; -fx-background-color: #1e3a8a; -fx-text-fill: white; -fx-alignment: CENTER_LEFT; -fx-font-size: 12; -fx-border-radius: 5; -fx-background-radius: 5;");
        }
        activeButton.setStyle("-fx-padding: 12; -fx-background-color: #3b82f6; -fx-text-fill: white; -fx-alignment: CENTER_LEFT; -fx-font-size: 12; -fx-border-radius: 5; -fx-background-radius: 5; -fx-border-width: 2; -fx-border-color: #60a5fa;");
    }

    // ===== CLOCK UPDATE =====
    private void updateClock() {
        clockThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(1000);
                    Platform.runLater(() -> {
                        if (clockLabel != null) {
                            LocalTime now = LocalTime.now();
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
                            clockLabel.setText(now.format(formatter));
                        }
                    });
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        clockThread.setDaemon(true);
        clockThread.start();
    }

    // ===== INITIALISATION DE LA STAGE =====
    public void setStage(Stage stage) {
        this.stage = stage;
        logger.info("✅ Stage définie dans DashboardBibliothecaireController");
    }
}