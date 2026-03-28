package com.example.SenBibliotheque.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TopBarController {

    private static final Logger logger = LoggerFactory.getLogger(TopBarController.class);

    @FXML
    private Button minimizeBtn, maximizeBtn, closeBtn;

    private Stage stage;
    private boolean isMaximized = false;
    private double previousX, previousY, previousWidth, previousHeight;

    /**
     * Définir la stage à contrôler
     */
    public void setStage(Stage stage) {
        this.stage = stage;
        logger.info("✅ TopBar initialisé pour la stage");
    }

    @FXML
    public void initialize() {
        // Ajouter des styles au survol
        minimizeBtn.setOnMouseEntered(e ->
                minimizeBtn.setStyle("-fx-font-size: 16; -fx-padding: 0 12; -fx-background-color: #e0e0e0; -fx-text-fill: #333333; -fx-cursor: hand;"));
        minimizeBtn.setOnMouseExited(e ->
                minimizeBtn.setStyle("-fx-font-size: 16; -fx-padding: 0 12; -fx-background-color: transparent; -fx-text-fill: #333333; -fx-cursor: hand;"));

        maximizeBtn.setOnMouseEntered(e ->
                maximizeBtn.setStyle("-fx-font-size: 14; -fx-padding: 0 12; -fx-background-color: #e0e0e0; -fx-text-fill: #333333; -fx-cursor: hand;"));
        maximizeBtn.setOnMouseExited(e ->
                maximizeBtn.setStyle("-fx-font-size: 14; -fx-padding: 0 12; -fx-background-color: transparent; -fx-text-fill: #333333; -fx-cursor: hand;"));

        closeBtn.setOnMouseEntered(e ->
                closeBtn.setStyle("-fx-font-size: 16; -fx-padding: 0 12; -fx-background-color: #ff4444; -fx-text-fill: white; -fx-cursor: hand;"));
        closeBtn.setOnMouseExited(e ->
                closeBtn.setStyle("-fx-font-size: 16; -fx-padding: 0 12; -fx-background-color: transparent; -fx-text-fill: #333333; -fx-cursor: hand;"));
    }

    /**
     * Réduire la fenêtre (Minimize)
     */
    @FXML
    private void minimize() {
        if (stage != null) {
            stage.setIconified(true);
            logger.info("✅ Fenêtre réduite");
        }
    }

    /**
     * Agrandir/Restaurer la fenêtre (Maximize/Restore)
     */
    @FXML
    private void toggleMaximize() {
        if (stage == null) {
            return;
        }

        if (isMaximized) {
            // Restaurer la taille précédente
            stage.setX(previousX);
            stage.setY(previousY);
            stage.setWidth(previousWidth);
            stage.setHeight(previousHeight);
            maximizeBtn.setText("⬜");
            isMaximized = false;
            logger.info("✅ Fenêtre restaurée");
        } else {
            // Sauvegarder la position et taille actuelles
            previousX = stage.getX();
            previousY = stage.getY();
            previousWidth = stage.getWidth();
            previousHeight = stage.getHeight();

            // Agrandir au maximum
            javafx.geometry.Rectangle2D screenBounds = javafx.stage.Screen.getPrimary().getVisualBounds();
            stage.setX(screenBounds.getMinX());
            stage.setY(screenBounds.getMinY());
            stage.setWidth(screenBounds.getWidth());
            stage.setHeight(screenBounds.getHeight());
            maximizeBtn.setText("❖");
            isMaximized = true;
            logger.info("✅ Fenêtre agrandie");
        }
    }

    /**
     * Fermer l'application (Close)
     */
    @FXML
    private void close() {
        if (stage != null) {
            stage.close();
            logger.info("✅ Application fermée");
        }
    }
}