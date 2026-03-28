package com.example.SenBibliotheque.ui;

import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/**
 * Panneau de contrôle personnalisé pour les boutons de fenêtre
 * (Minimize, Maximize, Close)
 */
public class WindowControlsPane extends HBox {

    private Stage stage;
    private boolean isMaximized = false;
    private double originalWidth, originalHeight;
    private double originalX, originalY;

    public WindowControlsPane(Stage stage) {
        this.stage = stage;
        this.setStyle(
                "-fx-background-color: #1e3a8a; " +
                        "-fx-padding: 0 10 0 0; " +
                        "-fx-spacing: 5; " +
                        "-fx-alignment: CENTER_RIGHT;"
        );
        this.setPrefHeight(45);

        // Bouton Minimize (−)
        Button minimizeBtn = createControlButton("−", "#3b82f6", "#1e3a8a");
        minimizeBtn.setOnAction(e -> stage.setIconified(true));
        minimizeBtn.setStyle(minimizeBtn.getStyle() + "; -fx-padding: 8 15;");

        // Bouton Maximize/Restore (⬜)
        Button maximizeBtn = createControlButton("⬜", "#3b82f6", "#1e3a8a");
        maximizeBtn.setOnAction(e -> toggleMaximize());
        maximizeBtn.setStyle(maximizeBtn.getStyle() + "; -fx-padding: 8 15;");

        // Bouton Close (✕)
        Button closeBtn = createControlButton("✕", "#dc2626", "#b91c1c");
        closeBtn.setOnAction(e -> stage.close());
        closeBtn.setStyle(closeBtn.getStyle() + "; -fx-padding: 8 15;");

        this.getChildren().addAll(minimizeBtn, maximizeBtn, closeBtn);
    }

    private Button createControlButton(String text, String normalColor, String hoverColor) {
        Button btn = new Button(text);
        btn.setStyle(
                "-fx-background-color: " + normalColor + "; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 14; " +
                        "-fx-font-weight: bold; " +
                        "-fx-border-radius: 0; " +
                        "-fx-background-radius: 0; " +
                        "-fx-cursor: hand;"
        );

        // Effet au survol
        btn.setOnMouseEntered(e -> btn.setStyle(
                "-fx-background-color: " + hoverColor + "; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 14; " +
                        "-fx-font-weight: bold; " +
                        "-fx-border-radius: 0; " +
                        "-fx-background-radius: 0; " +
                        "-fx-cursor: hand;"
        ));

        btn.setOnMouseExited(e -> btn.setStyle(
                "-fx-background-color: " + normalColor + "; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 14; " +
                        "-fx-font-weight: bold; " +
                        "-fx-border-radius: 0; " +
                        "-fx-background-radius: 0; " +
                        "-fx-cursor: hand;"
        ));

        return btn;
    }

    private void toggleMaximize() {
        if (!isMaximized) {
            // Sauvegarder les dimensions originales
            originalWidth = stage.getWidth();
            originalHeight = stage.getHeight();
            originalX = stage.getX();
            originalY = stage.getY();

            // Agrandir à plein écran
            stage.setX(0);
            stage.setY(0);
            stage.setWidth(stage.getOwner().getScene().getWindow().getWidth());
            stage.setHeight(stage.getOwner().getScene().getWindow().getHeight());

            isMaximized = true;
        } else {
            // Restaurer les dimensions
            stage.setWidth(originalWidth);
            stage.setHeight(originalHeight);
            stage.setX(originalX);
            stage.setY(originalY);

            isMaximized = false;
        }
    }
}