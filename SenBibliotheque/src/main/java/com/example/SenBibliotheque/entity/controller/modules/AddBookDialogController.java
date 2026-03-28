package com.example.SenBibliotheque.controller.modules;

import com.example.SenBibliotheque.entity.Livre;
import com.example.SenBibliotheque.entity.Categorie;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class AddBookDialogController {

    private static final Logger logger = LoggerFactory.getLogger(AddBookDialogController.class);

    @FXML
    private TextField isbnField, titreField, auteurField, anneeField;
    @FXML
    private ComboBox<Categorie> categorieComboBox;
    @FXML
    private Spinner<Integer> exemplairesSpinner, disponibleSpinner;
    @FXML
    private TextArea descriptionArea;
    @FXML
    private Label errorLabel;

    private Livre livreActuel = null;

    @FXML
    public void initialize() {

        // Spinners
        exemplairesSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 999, 1));

        disponibleSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 999, 0));

        // Reset erreurs à la saisie (UX moderne)
        addResetListener(isbnField);
        addResetListener(titreField);
        addResetListener(auteurField);
        addResetListener(anneeField);

        logger.info("✅ AddBookDialogController initialisé");
    }

    private void addResetListener(TextField field) {
        field.textProperty().addListener((obs, oldVal, newVal) -> {
            field.setStyle("-fx-border-color: #a7f3d0;");
            errorLabel.setText("");
        });
    }

    public void setCategories(List<Categorie> categories) {
        categorieComboBox.getItems().setAll(categories);
        if (!categories.isEmpty()) {
            categorieComboBox.getSelectionModel().selectFirst();
        }
    }

    public void setLivre(Livre livre) {
        this.livreActuel = livre;

        isbnField.setText(livre.getIsbn());
        isbnField.setEditable(false);
        isbnField.setStyle(isbnField.getStyle() + "; -fx-text-fill: #999999;");

        titreField.setText(livre.getTitre());
        auteurField.setText(livre.getAuteur());
        anneeField.setText(livre.getAnneePublication() != null
                ? livre.getAnneePublication().toString() : "");

        categorieComboBox.setValue(livre.getCategorie());

        exemplairesSpinner.getValueFactory().setValue(livre.getNombreExemplaires());
        disponibleSpinner.getValueFactory().setValue(livre.getDisponible());

        descriptionArea.setText(livre.getDescription() != null
                ? livre.getDescription() : "");
    }

    public boolean validateForm() {

        resetStyles();
        errorLabel.setText("");

        if (isEmpty(titreField)) {
            return error(titreField, "Le titre est obligatoire");
        }

        if (isEmpty(auteurField)) {
            return error(auteurField, "L'auteur est obligatoire");
        }

        if (isEmpty(isbnField)) {
            return error(isbnField, "L'ISBN est obligatoire");
        }

        String isbn = isbnField.getText().trim();
        if (!isbn.matches("[0-9\\-]{10,20}")) {
            return error(isbnField, "ISBN invalide (ex: 978-2-07-074456-2)");
        }

        if (isEmpty(anneeField)) {
            return error(anneeField, "L'année est obligatoire");
        }

        try {
            Integer.parseInt(anneeField.getText().trim());
        } catch (NumberFormatException e) {
            return error(anneeField, "Année invalide");
        }

        if (categorieComboBox.getValue() == null) {
            afficherErreur("Veuillez sélectionner une catégorie");
            return false;
        }

        if (exemplairesSpinner.getValue() < 1) {
            return error(null, "Le nombre d'exemplaires doit être ≥ 1");
        }

        if (disponibleSpinner.getValue() > exemplairesSpinner.getValue()) {
            return error(null, "Disponibles > total");
        }

        return true;
    }

    private boolean isEmpty(TextField field) {
        return field.getText() == null || field.getText().trim().isEmpty();
    }

    private boolean error(TextField field, String message) {
        afficherErreur(message);

        if (field != null) {
            field.setStyle("-fx-border-color: #ef4444; -fx-border-width: 2;");
        }

        return false;
    }

    private void afficherErreur(String message) {
        errorLabel.setText("⚠️ " + message);
        logger.warn("❌ Erreur: {}", message);
    }

    private void resetStyles() {
        String normalStyle = "-fx-border-color: #a7f3d0;";

        isbnField.setStyle(normalStyle);
        titreField.setStyle(normalStyle);
        auteurField.setStyle(normalStyle);
        anneeField.setStyle(normalStyle);
    }

    // Getters
    public String getIsbn() { return isbnField.getText().trim(); }
    public String getTitre() { return titreField.getText().trim(); }
    public String getAuteur() { return auteurField.getText().trim(); }
    public Integer getAnnee() { return Integer.parseInt(anneeField.getText().trim()); }
    public Categorie getCategorie() { return categorieComboBox.getValue(); }
    public Integer getNombreExemplaires() { return exemplairesSpinner.getValue(); }
    public Integer getDisponible() { return disponibleSpinner.getValue(); }
    public String getDescription() { return descriptionArea.getText().trim(); }
    public Livre getLivreActuel() { return livreActuel; }
}