package com.example.SenBibliotheque.entity.controller.modules;

import com.example.SenBibliotheque.entity.Adherent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.format.DateTimeFormatter;

public class AddMemberDialogController {

    private static final Logger logger = LoggerFactory.getLogger(AddMemberDialogController.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    @FXML
    private TextField prenomField;
    @FXML
    private TextField nomField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField telephoneField;
    @FXML
    private TextField dateInscriptionField;
    @FXML
    private TextArea adresseArea;
    @FXML
    private ComboBox<String> statutComboBox;
    @FXML
    private Label errorLabel;

    private Adherent adherentActuel = null;

    @FXML
    public void initialize() {
        // Initialiser le ComboBox
        statutComboBox.getItems().addAll("Actif", "Suspendu");
        statutComboBox.setValue("Actif");

        logger.info("✅ AddMemberDialogController initialisé");
    }

    public void setAdherent(Adherent adherent) {
        this.adherentActuel = adherent;

        prenomField.setText(adherent.getPrenom());
        nomField.setText(adherent.getNom());
        emailField.setText(adherent.getEmail());
        telephoneField.setText(adherent.getTelephone() != null ? adherent.getTelephone() : "");
        adresseArea.setText(adherent.getAdresse() != null ? adherent.getAdresse() : "");

        statutComboBox.setValue(adherent.getActif() ? "Actif" : "Suspendu");
        dateInscriptionField.setText(adherent.getDateInscription().format(DATE_FORMATTER));
    }

    public boolean validateForm() {
        errorLabel.setText("");

        if (prenomField.getText().trim().isEmpty()) {
            afficherErreur("Le prénom est obligatoire");
            return false;
        }

        if (nomField.getText().trim().isEmpty()) {
            afficherErreur("Le nom est obligatoire");
            return false;
        }

        if (emailField.getText().trim().isEmpty()) {
            afficherErreur("L'email est obligatoire");
            return false;
        }

        if (!emailField.getText().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            afficherErreur("L'email n'est pas valide");
            return false;
        }

        if (telephoneField.getText().trim().isEmpty()) {
            afficherErreur("Le téléphone est obligatoire");
            return false;
        }

        if (adresseArea.getText().trim().isEmpty()) {
            afficherErreur("L'adresse est obligatoire");
            return false;
        }

        return true;
    }

    private void afficherErreur(String message) {
        errorLabel.setText("⚠️ " + message);
        logger.warn("❌ Erreur de validation: " + message);
    }

    // ===== GETTERS =====
    public String getPrenom() {
        return prenomField.getText().trim();
    }

    public String getNom() {
        return nomField.getText().trim();
    }

    public String getEmail() {
        return emailField.getText().trim();
    }

    public String getTelephone() {
        return telephoneField.getText().trim();
    }

    public String getAdresse() {
        return adresseArea.getText().trim();
    }

    public String getStatut() {
        return statutComboBox.getValue();
    }

    public Adherent getAdherentActuel() {
        return adherentActuel;
    }
}