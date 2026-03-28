package com.example.SenBibliotheque.entity.controller.modules;

import com.example.SenBibliotheque.entity.Utilisateur;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

public class AddUserDialogController {

    private static final Logger logger = LoggerFactory.getLogger(AddUserDialogController.class);

    @FXML
    private TextField prenomField, nomField, emailField, loginField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private ComboBox<String> profilComboBox, statutComboBox;
    @FXML
    private CheckBox forcePasswordCheckBox;
    @FXML
    private ProgressBar passwordStrengthBar;
    @FXML
    private Label passwordStrengthLabel, errorLabel;

    @FXML
    public void initialize() {
        // Initialiser les ComboBox
        profilComboBox.getItems().addAll("ADMIN", "BIBLIOTHECAIRE");
        profilComboBox.setValue("BIBLIOTHECAIRE");

        statutComboBox.getItems().addAll("Actif", "Inactif");
        statutComboBox.setValue("Actif");

        // Cocher par défaut
        forcePasswordCheckBox.setSelected(true);

        // Générer un mot de passe automatiquement
        generatePassword();

        // Écouter les changements du mot de passe
        passwordField.textProperty().addListener((obs, oldVal, newVal) -> {
            updatePasswordStrength(newVal);
        });

        logger.info("✅ AddUserDialogController initialisé");
    }

    @FXML
    private void generatePassword() {
        String password = generateSecurePassword();
        passwordField.setText(password);
        updatePasswordStrength(password);
    }

    private String generateSecurePassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
        StringBuilder password = new StringBuilder();
        Random random = new Random();

        for (int i = 0; i < 12; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }

        return password.toString();
    }

    private void updatePasswordStrength(String password) {
        if (password == null || password.isEmpty()) {
            passwordStrengthBar.setProgress(0);
            passwordStrengthLabel.setText("Vide");
            passwordStrengthLabel.setStyle("-fx-text-fill: #ef4444;");
            return;
        }

        int strength = 0;

        if (password.length() >= 8) strength++;
        if (password.length() >= 12) strength++;
        if (password.matches(".*[A-Z].*")) strength++;
        if (password.matches(".*[a-z].*")) strength++;
        if (password.matches(".*\\d.*")) strength++;
        if (password.matches(".*[!@#$%^&*].*")) strength++;

        double progress = strength / 6.0;
        passwordStrengthBar.setProgress(progress);

        String label;
        String color;
        if (strength <= 2) {
            label = "Faible";
            color = "-fx-text-fill: #ef4444;";
        } else if (strength <= 4) {
            label = "Moyen";
            color = "-fx-text-fill: #f59e0b;";
        } else {
            label = "Fort";
            color = "-fx-text-fill: #10b981;";
        }

        passwordStrengthLabel.setText(label);
        passwordStrengthLabel.setStyle(color);
    }

    private Utilisateur utilisateurActuel = null;

    public void setUtilisateur(Utilisateur utilisateur) {
        this.utilisateurActuel = utilisateur;

        // Remplir les champs
        prenomField.setText(utilisateur.getPrenom());
        nomField.setText(utilisateur.getNom());
        emailField.setText(utilisateur.getEmail());
        loginField.setText(utilisateur.getLogin());
        loginField.setEditable(false); // Le login ne peut pas être changé
        loginField.setStyle(loginField.getStyle() + "; -fx-text-fill: #999999;");

        profilComboBox.setValue(utilisateur.getProfil());
        statutComboBox.setValue(utilisateur.getActif() ? "Actif" : "Inactif");
        forcePasswordCheckBox.setSelected(false);

        // Masquer les champs de mot de passe en mode modification
        passwordField.setPromptText("Laisser vide pour conserver le mot de passe");
        passwordField.clear();
    }

    public Utilisateur getUtilisateurActuel() {
        return utilisateurActuel;
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

        if (loginField.getText().trim().isEmpty()) {
            afficherErreur("Le login est obligatoire");
            return false;
        }

        if (passwordField.getText().isEmpty()) {
            afficherErreur("Le mot de passe est obligatoire");
            return true;
        }

        if (emailField.getText().trim().isEmpty()) {
            afficherErreur("L'email est obligatoire");
            return false;
        }

        if (!emailField.getText().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            afficherErreur("L'email n'est pas valide");
            return false;
        }

        if (loginField.getText().length() < 3) {
            afficherErreur("Le login doit contenir au moins 3 caractères");
            return false;
        }

        if (passwordField.getText().length() < 8) {
            afficherErreur("Le mot de passe doit contenir au moins 8 caractères");
            return false;
        }

        return true;
    }

    private void afficherErreur(String message) {
        errorLabel.setText("⚠️ " + message);
        logger.warn("❌ Erreur de validation: " + message);
    }

    // Getters
    public String getPrenom() {
        return prenomField.getText().trim();
    }

    public String getNom() {
        return nomField.getText().trim();
    }

    public String getEmail() {
        return emailField.getText().trim();
    }

    public String getLogin() {
        return loginField.getText().trim();
    }

    public String getPassword() {
        return passwordField.getText();
    }

    public String getProfil() {
        return profilComboBox.getValue();
    }

    public String getStatut() {
        return statutComboBox.getValue();
    }

    public boolean isForcePasswordChange() {
        return forcePasswordCheckBox.isSelected();
    }
}