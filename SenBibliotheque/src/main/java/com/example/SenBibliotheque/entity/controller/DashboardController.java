package com.example.SenBibliotheque.entity.controller;

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

public class DashboardController {

    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);

    @FXML private VBox sidebarPane;
    @FXML private StackPane contentPane;
    @FXML private Label pageTitle, userNameLabel, userRoleLabel, clockLabel;
    @FXML private Button dashboardBtn, usersBtn, booksBtn, categoriesBtn, membersBtn, borrowsBtn, reportsBtn, logoutBtn, collapseBtn;

    private boolean sidebarExpanded = true;
    private static final int SIDEBAR_WIDTH = 250;
    private static final int SIDEBAR_COLLAPSED_WIDTH = 60;
    @FXML
    private HBox titleBar;
    @FXML
    private Button minimizeBtn;
    @FXML
    private Button maximizeBtn;
    @FXML
    private Button closeBtn;

    private WindowController windowController;

    @FXML
    public void initialize() {
        updateClock();
        loadDashboard();
        setActiveButton(dashboardBtn);
    }

    // ===== NAVIGATION =====
    @FXML
    private void loadDashboard() {
        loadModule("/fxml/modules/DashboardModule.fxml", "Dashboard");
        setActiveButton(dashboardBtn);
    }

    @FXML
    private void loadUsers() {
        loadModule("/fxml/modules/UsersModule.fxml", "Gestion des Utilisateurs");
        setActiveButton(usersBtn);
    }

    @FXML
    private void loadBooks() {
        loadModule("/fxml/modules/BooksModule.fxml", "Gestion des Livres");
        setActiveButton(booksBtn);
    }

    @FXML
    private void loadCategories() {
        loadModule("/fxml/modules/CategoriesModule.fxml", "Gestion des Catégories");
        setActiveButton(categoriesBtn);
    }

    @FXML
    private void loadMembers() {
        loadModule("/fxml/modules/MembersModule.fxml", "Gestion des Adhérents");
        setActiveButton(membersBtn);
    }

    @FXML
    private void loadBorrows() {
        loadModule("/fxml/modules/BorrowsModule.fxml", "Gestion des Emprunts");
        setActiveButton(borrowsBtn);
    }

    @FXML
    private void loadReports() {
        loadModule("/fxml/modules/ReportsModule.fxml", "Rapports et Statistiques");
        setActiveButton(reportsBtn);
    }

    @FXML
    private void logout() {
        try {
            com.example.SenBibliotheque.util.SessionManager.getInstance().deconnecter();

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

    // ===== CHARGEMENT DYNAMIQUE =====
    private void loadModule(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(DashboardController.class.getResource(fxmlPath));
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
        for (Button btn : new Button[]{dashboardBtn, usersBtn, booksBtn, categoriesBtn, membersBtn, borrowsBtn, reportsBtn}) {
            btn.setStyle("-fx-padding: 12; -fx-background-color: #1e3a8a; -fx-text-fill: white; -fx-alignment: CENTER_LEFT; -fx-font-size: 12; -fx-border-radius: 5; -fx-background-radius: 5;");
        }
        activeButton.setStyle("-fx-padding: 12; -fx-background-color: #3b82f6; -fx-text-fill: white; -fx-alignment: CENTER_LEFT; -fx-font-size: 12; -fx-border-radius: 5; -fx-background-radius: 5; -fx-border-width: 2; -fx-border-color: #60a5fa;");
    }

    // ===== CLOCK UPDATE =====
    private void updateClock() {
        Thread clockThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(1000);
                    Platform.runLater(() -> {
                        LocalTime now = LocalTime.now();
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
                        clockLabel.setText(now.format(formatter));
                    });
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        clockThread.setDaemon(true);
        clockThread.start();
    }


    public void initializeWindow(Stage stage) {
        windowController = new WindowController();
        windowController.titleBar = titleBar;
        windowController.minimizeBtn = minimizeBtn;
        windowController.maximizeBtn = maximizeBtn;
        windowController.closeBtn = closeBtn;
        windowController.initialize(stage);
    }
    // ===== INITIALISATION DE LA STAGE =====
    public void setStage(Stage stage) {
        // Connecter les boutons de la TopBar
        minimizeBtn.setOnAction(e -> minimizeWindow());
        maximizeBtn.setOnAction(e -> maximizeWindow());
        closeBtn.setOnAction(e -> closeWindow());

        logger.info("✅ Stage définie dans DashboardController");
    }

    @FXML
    private void minimizeWindow() {
        Stage stage = (Stage) logoutBtn.getScene().getWindow();
        if (stage != null) {
            stage.setIconified(true);
            logger.info("✅ Fenêtre minimisée");
        }
    }

    @FXML
    private void maximizeWindow() {
        Stage stage = (Stage) logoutBtn.getScene().getWindow();
        if (stage != null) {
            boolean isMaximized = stage.isMaximized();
            stage.setMaximized(!isMaximized);
            maximizeBtn.setText(!isMaximized ? "❒" : "□");
            logger.info("✅ Fenêtre " + (!isMaximized ? "maximisée" : "restaurée"));
        }
    }

    @FXML
    private void closeWindow() {
        Stage stage = (Stage) logoutBtn.getScene().getWindow();
        if (stage != null) {
            stage.close();
            logger.info("✅ Application fermée");
        }
    }
}