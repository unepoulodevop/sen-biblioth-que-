package com.example.SenBibliotheque.controller.modules;

import com.example.SenBibliotheque.entity.Categorie;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AddCategoryDialogController {

    private static final Logger logger = LoggerFactory.getLogger(AddCategoryDialogController.class);

    @FXML
    private TextField libelleField;
    @FXML
    private TextArea descriptionArea;
    @FXML
    private Label booksCountLabel, errorLabel;

    private Categorie categorieActuelle = null;

    @FXML
    public void initialize() {
        logger.info("✅ AddCategoryDialogController initialisé");
    }

    public void setCategorie(Categorie categorie) {
        this.categorieActuelle = categorie;

        libelleField.setText(categorie.getLibelle());
        descriptionArea.setText(categorie.getDescription() != null ? categorie.getDescription() : "");
        booksCountLabel.setText(categorie.getLivres().size() + " livre(s)");
    }

    public boolean validateForm() {
        errorLabel.setText("");

        if (libelleField.getText().trim().isEmpty()) {
            afficherErreur("Le libellé est obligatoire");
            return false;
        }

        if (libelleField.getText().length() < 3) {
            afficherErreur("Le libellé doit contenir au moins 3 caractères");
            return false;
        }

        if (libelleField.getText().length() > 100) {
            afficherErreur("Le libellé ne peut pas dépasser 100 caractères");
            return false;
        }

        return true;
    }

    private void afficherErreur(String message) {
        errorLabel.setText("⚠️ " + message);
        logger.warn("❌ Erreur de validation: " + message);
    }

    // Getters
    public String getLibelle() {
        return libelleField.getText().trim();
    }

    public String getDescription() {
        return descriptionArea.getText().trim();
    }

    public Categorie getCategorieActuelle() {
        return categorieActuelle;
    }
}