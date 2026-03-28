package com.example.SenBibliotheque.ui;

import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

public class WindowControlBar extends HBox {

    public WindowControlBar(Stage stage, String title) {
        // Style de la barre
        setStyle("-fx-background-color: #1e40af; -fx-padding: 10; -fx-alignment: CENTER_LEFT; -fx-spacing: 10;");

        // Titre de la fenêtre
        javafx.scene.control.Label titleLabel = new javafx.scene.control.Label("📚 " + title);
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14; -fx-font-weight: bold;");

        // Espace flexible
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Bouton réduire
        Button minimizeBtn = new Button("−");
        minimizeBtn.setStyle("""
                -fx-background-color: transparent;
                -fx-text-fill: white;
                -fx-font-size: 18;
                -fx-padding: 0 10;
                -fx-cursor: hand;
                """);
        minimizeBtn.setOnAction(e -> stage.setIconified(true));
        minimizeBtn.setOnMouseEntered(me -> minimizeBtn.setStyle("""
                -fx-background-color: rgba(255, 255, 255, 0.2);
                -fx-text-fill: white;
                -fx-font-size: 18;
                -fx-padding: 0 10;
                -fx-cursor: hand;
                """));
        minimizeBtn.setOnMouseExited(me -> minimizeBtn.setStyle("""
                -fx-background-color: transparent;
                -fx-text-fill: white;
                -fx-font-size: 18;
                -fx-padding: 0 10;
                -fx-cursor: hand;
                """));

        // Bouton agrandir/restaurer
        Button maximizeBtn = new Button("□");
        maximizeBtn.setStyle("""
                -fx-background-color: transparent;
                -fx-text-fill: white;
                -fx-font-size: 18;
                -fx-padding: 0 10;
                -fx-cursor: hand;
                """);
        maximizeBtn.setOnAction(e -> {
            stage.setMaximized(!stage.isMaximized());
            maximizeBtn.setText(stage.isMaximized() ? "❒" : "□");
        });
        maximizeBtn.setOnMouseEntered(me -> maximizeBtn.setStyle("""
                -fx-background-color: rgba(255, 255, 255, 0.2);
                -fx-text-fill: white;
                -fx-font-size: 18;
                -fx-padding: 0 10;
                -fx-cursor: hand;
                """));
        maximizeBtn.setOnMouseExited(me -> maximizeBtn.setStyle("""
                -fx-background-color: transparent;
                -fx-text-fill: white;
                -fx-font-size: 18;
                -fx-padding: 0 10;
                -fx-cursor: hand;
                """));

        // Bouton fermer
        Button closeBtn = new Button("✕");
        closeBtn.setStyle("""
                -fx-background-color: transparent;
                -fx-text-fill: white;
                -fx-font-size: 18;
                -fx-padding: 0 10;
                -fx-cursor: hand;
                """);
        closeBtn.setOnAction(e -> stage.close());
        closeBtn.setOnMouseEntered(me -> closeBtn.setStyle("""
                -fx-background-color: #dc2626;
                -fx-text-fill: white;
                -fx-font-size: 18;
                -fx-padding: 0 10;
                -fx-cursor: hand;
                """));
        closeBtn.setOnMouseExited(me -> closeBtn.setStyle("""
                -fx-background-color: transparent;
                -fx-text-fill: white;
                -fx-font-size: 18;
                -fx-padding: 0 10;
                -fx-cursor: hand;
                """));

        // Ajouter les éléments
        getChildren().addAll(titleLabel, spacer, minimizeBtn, maximizeBtn, closeBtn);

        // Permettre le déplacement de la fenêtre en traînant la barre
        setOnMousePressed(event -> {
            if (event.isPrimaryButtonDown()) {
                stage.setX(event.getScreenX() - event.getSceneX());
                stage.setY(event.getScreenY() - event.getSceneY());
            }
        });

        setOnMouseDragged(event -> {
            if (event.isPrimaryButtonDown()) {
                stage.setX(event.getScreenX() - event.getSceneX());
                stage.setY(event.getScreenY() - event.getSceneY());
            }
        });
    }
}