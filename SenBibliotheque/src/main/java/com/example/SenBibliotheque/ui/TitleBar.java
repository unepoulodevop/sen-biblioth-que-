package com.example.SenBibliotheque.ui;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

public class TitleBar extends HBox {
    private Stage stage;

    public TitleBar(Stage stage, String title) {
        this.stage = stage;
        setStyle("-fx-background-color: #1e40af; -fx-padding: 10; -fx-alignment: CENTER_LEFT; -fx-spacing: 10;");
        setPrefHeight(40);

        // Label titre
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14; -fx-font-weight: bold;");

        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Bouton Réduire
        Button minimizeBtn = new Button("_");
        minimizeBtn.setStyle("-fx-padding: 5 10; -fx-font-size: 12; -fx-background-color: transparent; -fx-text-fill: white;");
        minimizeBtn.setOnAction(e -> stage.setIconified(true));

        // Bouton Agrandir/Restaurer
        Button maximizeBtn = new Button("□");
        maximizeBtn.setStyle("-fx-padding: 5 10; -fx-font-size: 12; -fx-background-color: transparent; -fx-text-fill: white;");
        maximizeBtn.setOnAction(e -> {
            if (stage.isMaximized()) {
                stage.setMaximized(false);
            } else {
                stage.setMaximized(true);
            }
        });

        // Bouton Fermer
        Button closeBtn = new Button("✕");
        closeBtn.setStyle("-fx-padding: 5 10; -fx-font-size: 12; -fx-background-color: transparent; -fx-text-fill: white;");
        closeBtn.setOnAction(e -> stage.close());

        // Ajouter les éléments
        getChildren().addAll(titleLabel, spacer, minimizeBtn, maximizeBtn, closeBtn);
    }
}