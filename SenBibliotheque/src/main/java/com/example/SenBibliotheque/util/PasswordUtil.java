package com.example.SenBibliotheque.util;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtil {

    private static final int BCRYPT_STRENGTH = 12;

    /**
     * Hacher un mot de passe en BCrypt
     */
    public static String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(BCRYPT_STRENGTH));
    }

    /**
     * Vérifier un mot de passe
     */
    public static boolean verifyPassword(String password, String hash) {
        try {
            return BCrypt.checkpw(password, hash);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Générer un mot de passe aléatoire
     */
    public static String generateRandomPassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt((int) (Math.random() * chars.length())));
        }
        return sb.toString();
    }

    /**
     * Valider la force d'un mot de passe
     */
    public static PasswordStrength validatePasswordStrength(String password) {
        if (password == null || password.length() < 8) {
            return PasswordStrength.FAIBLE;
        }

        boolean hasUpper = password.matches(".*[A-Z].*");
        boolean hasLower = password.matches(".*[a-z].*");
        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasSpecial = password.matches(".*[!@#$%^&*].*");

        int score = 0;
        if (hasUpper) score++;
        if (hasLower) score++;
        if (hasDigit) score++;
        if (hasSpecial) score++;

        return switch (score) {
            case 1 -> PasswordStrength.FAIBLE;
            case 2 -> PasswordStrength.MOYEN;
            case 3 -> PasswordStrength.BON;
            case 4 -> PasswordStrength.TRES_BON;
            default -> PasswordStrength.FAIBLE;
        };
    }

    public enum PasswordStrength {
        FAIBLE("Faible"),
        MOYEN("Moyen"),
        BON("Bon"),
        TRES_BON("Très bon");

        private final String label;

        PasswordStrength(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }
}