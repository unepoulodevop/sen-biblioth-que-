package com.example.SenBibliotheque.util;

import javafx.stage.Stage;
import javafx.beans.value.ChangeListener;
import javafx.scene.Scene;

public class ResponsiveManager {

    public static void makeResponsive(Stage stage, Scene scene) {
        ChangeListener<Number> sizeListener = (observable, oldValue, newValue) -> {
            double width = stage.getWidth();
            double height = stage.getHeight();

            // Appliquer les styles selon la taille
            if (width < 1024) {
                scene.getRoot().getStyleClass().add("root-small");
                scene.getRoot().getStyleClass().remove("root-large");
            } else {
                scene.getRoot().getStyleClass().add("root-large");
                scene.getRoot().getStyleClass().remove("root-small");
            }
        };

        // Écouter les changements de taille
        stage.widthProperty().addListener(sizeListener);
        stage.heightProperty().addListener(sizeListener);

        // Appel initial
        sizeListener.changed(null, null, stage.getWidth());
    }
}