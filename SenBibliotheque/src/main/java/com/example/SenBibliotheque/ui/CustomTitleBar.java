package com.example.SenBibliotheque.ui;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

public class CustomTitleBar extends HBox {
    private Stage stage;
    private Label titleLabel;
    private Button minimizeBtn;
    private Button maximizeBtn;
    private Button closeBtn;
    private double xOffset = 0;
    private double yOffset = 0;

    public CustomTitleBar(Stage stage, String title) {
        this.stage = stage;
        initializeComponents(title);
        setupStyles();
        setupEventHandlers();
    }

    private void initializeComponents(String title) {
        // Logo
        Label logo = new Label("📚");
        logo.setStyle("-fx-font-size: 16; -fx-padding: 0 10 0 0;");

        // Titre
        titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: #ffffff;");

        // Région spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Boutons de contrôle
        minimizeBtn = createControlButton("_", "Minimiser");
        maximizeBtn = createControlButton("□", "Maximiser");
        closeBtn = createControlButton("✕", "Fermer");

        // Ajouter les éléments
        this.getChildren().addAll(logo, titleLabel, spacer, minimizeBtn, maximizeBtn, closeBtn);
        this.setAlignment(Pos.CENTER_LEFT);
        this.setPrefHeight(35);
    }

    private Button createControlButton(String text, String tooltip) {
        Button btn = new Button(text);
        btn.setPrefWidth(45);
        btn.setPrefHeight(35);
        btn.setStyle(
                "-fx-background-color: transparent; " +
                        "-fx-text-fill: #ffffff; " +
                        "-fx-font-size: 14; " +
                        "-fx-font-weight: bold; " +
                        "-fx-cursor: hand; " +
                        "-fx-border-width: 0; " +
                        "-fx-padding: 0;"
        );

        // Effet au survol
        btn.setOnMouseEntered(e -> btn.setStyle(
                "-fx-background-color: rgba(255,255,255,0.1); " +
                        "-fx-text-fill: #ffffff; " +
                        "-fx-font-size: 14; " +
                        "-fx-font-weight: bold; " +
                        "-fx-cursor: hand; " +
                        "-fx-border-width: 0; " +
                        "-fx-padding: 0;"
        ));

        btn.setOnMouseExited(e -> btn.setStyle(
                "-fx-background-color: transparent; " +
                        "-fx-text-fill: #ffffff; " +
                        "-fx-font-size: 14; " +
                        "-fx-font-weight: bold; " +
                        "-fx-cursor: hand; " +
                        "-fx-border-width: 0; " +
                        "-fx-padding: 0;"
        ));

        return btn;
    }

    private void setupStyles() {
        // Style principal de la barre de titre
        this.setStyle(
                "-fx-background-color: #1e40af; " +
                        "-fx-padding: 0 15 0 15; " +
                        "-fx-border-color: #1e3a8a; " +
                        "-fx-border-width: 0 0 1 0;"
        );
    }

    private void setupEventHandlers() {
        // Rendre la fenêtre draggable
        this.setOnMousePressed(event -> {
            xOffset = event.getSceneX() - stage.getX();
            yOffset = event.getSceneY() - stage.getY();
        });

        this.setOnMouseDragged(event -> {
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        });

        // Double-clic pour maximiser/restaurer
        this.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                toggleMaximize();
            }
        });

        // Bouton Minimiser
        minimizeBtn.setOnAction(event -> stage.setIconified(true));

        // Bouton Maximiser
        maximizeBtn.setOnAction(event -> toggleMaximize());

        // Bouton Fermer
        closeBtn.setOnAction(event -> {
            stage.close();
            System.exit(0);
        });
    }

    private void toggleMaximize() {
        if (stage.isMaximized()) {
            stage.setMaximized(false);
        } else {
            stage.setMaximized(true);
        }
    }

    public void setTitle(String title) {
        titleLabel.setText(title);
    }
}