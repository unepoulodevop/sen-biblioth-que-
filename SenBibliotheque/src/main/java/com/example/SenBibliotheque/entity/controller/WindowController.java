package com.example.SenBibliotheque.controller;

import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WindowController {

    private static final Logger logger = LoggerFactory.getLogger(WindowController.class);

    @FXML
    public HBox titleBar;
    @FXML
    public Button minimizeBtn;
    @FXML
    public Button maximizeBtn;
    @FXML
    public Button closeBtn;

    private Stage stage;
    private double xOffset = 0;
    private double yOffset = 0;
    private boolean isMaximized = false;
    private double previousX = 0;
    private double previousY = 0;
    private double previousWidth = 0;
    private double previousHeight = 0;

    /**
     * Initialiser le contrôleur avec la Stage
     */
    public void initialize(Stage stage) {
        this.stage = stage;

        if (titleBar != null) {
            // Rendre la barre de titre draggable
            titleBar.setOnMousePressed(this::handleMousePressed);
            titleBar.setOnMouseDragged(this::handleMouseDragged);
            titleBar.setOnMouseClicked(this::handleMouseClicked);
        }

        if (minimizeBtn != null) {
            minimizeBtn.setOnAction(event -> minimize());
        }

        if (maximizeBtn != null) {
            maximizeBtn.setOnAction(event -> toggleMaximize());
        }

        if (closeBtn != null) {
            closeBtn.setOnAction(event -> close());
        }

        logger.info("✅ Window Controller initialisé");
    }

    /**
     * Gérer le mousePressed pour drag
     */
    private void handleMousePressed(MouseEvent event) {
        xOffset = event.getSceneX();
        yOffset = event.getSceneY();
    }

    /**
     * Gérer le mouseDragged pour déplacer la fenêtre
     */
    private void handleMouseDragged(MouseEvent event) {
        if (!isMaximized) {
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        }
    }

    /**
     * Double-clic sur la barre de titre = maximize/restore
     */
    private void handleMouseClicked(MouseEvent event) {
        if (event.getClickCount() == 2) {
            toggleMaximize();
        }
    }

    /**
     * Minimiser la fenêtre
     */
    private void minimize() {
        stage.setIconified(true);
        logger.info("✅ Fenêtre minimisée");
    }

    /**
     * Toggle Maximize/Restore
     */
    private void toggleMaximize() {
        if (!isMaximized) {
            maximize();
        } else {
            restore();
        }
    }

    /**
     * Maximiser la fenêtre
     */
    private void maximize() {
        // Sauvegarder la position et taille actuelle
        previousX = stage.getX();
        previousY = stage.getY();
        previousWidth = stage.getWidth();
        previousHeight = stage.getHeight();

        // Obtenir l'écran
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        // Maximiser
        stage.setX(screenBounds.getMinX());
        stage.setY(screenBounds.getMinY());
        stage.setWidth(screenBounds.getWidth());
        stage.setHeight(screenBounds.getHeight());

        isMaximized = true;

        // Changer l'icône du bouton
        if (maximizeBtn != null) {
            maximizeBtn.setText("⬜");
        }

        logger.info("✅ Fenêtre maximisée");
    }

    /**
     * Restaurer la fenêtre à sa taille précédente
     */
    private void restore() {
        stage.setX(previousX);
        stage.setY(previousY);
        stage.setWidth(previousWidth);
        stage.setHeight(previousHeight);

        isMaximized = false;

        // Changer l'icône du bouton
        if (maximizeBtn != null) {
            maximizeBtn.setText("⬜");
        }

        logger.info("✅ Fenêtre restaurée");
    }

    /**
     * Fermer l'application
     */
    private void close() {
        logger.info("📴 Fermeture de l'application");
        stage.close();
    }

    /**
     * Vérifier si la fenêtre est maximisée
     */
    public boolean isMaximized() {
        return isMaximized;
    }
}