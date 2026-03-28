package com.example.SenBibliotheque.util;

import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Classe utilitaire pour configurer les fenêtres de l'application
 * Gère les dimensions, le positionnement et les modes d'affichage
 */
public class WindowConfig {

    private static final Logger logger = LoggerFactory.getLogger(WindowConfig.class);

    // ===== CONSTANTES DASHBOARD =====
    private static final double DASHBOARD_MIN_WIDTH = 1000;
    private static final double DASHBOARD_MIN_HEIGHT = 650;
    private static final double DASHBOARD_PREF_WIDTH = 1300;
    private static final double DASHBOARD_PREF_HEIGHT = 800;

    // ===== CONSTANTES LOGIN =====
    private static final double LOGIN_PREF_WIDTH = 1200;
    private static final double LOGIN_PREF_HEIGHT = 700;
    private static final double LOGIN_MIN_WIDTH = 1000;
    private static final double LOGIN_MIN_HEIGHT = 600;

    /**
     * Configure la stage pour le dashboard avec les dimensions correctes
     * ✅ FORCER LE MODE WINDOWED
     * ✅ FIXER LES DIMENSIONS
     * ✅ CENTRER LA FENÊTRE
     */
    public static void configureDashboardStage(Stage stage) {
        try {
            // ✅ S'ASSURER QUE C'EST PAS EN FULLSCREEN
            stage.setFullScreen(false);
            stage.setMaximized(false);

            // ✅ DÉFINIR LES LIMITES
            stage.setMinWidth(DASHBOARD_MIN_WIDTH);
            stage.setMinHeight(DASHBOARD_MIN_HEIGHT);

            // ✅ DÉFINIR LES DIMENSIONS PRÉFÉRÉES
            stage.setWidth(DASHBOARD_PREF_WIDTH);
            stage.setHeight(DASHBOARD_PREF_HEIGHT);

            // ✅ PERMETTRE LE REDIMENSIONNEMENT
            stage.setResizable(true);

            // ✅ CENTRER LA FENÊTRE
            centerWindowOnScreen(stage);

            logger.info("✅ Dashboard stage configurée: " + DASHBOARD_PREF_WIDTH + "x" + DASHBOARD_PREF_HEIGHT);
        } catch (Exception e) {
            logger.error("❌ Erreur lors de la configuration du dashboard", e);
        }
    }

    /**
     * Configure la stage pour la page de login
     */
    public static void configureLoginStage(Stage stage) {
        try {
            // ✅ MODE WINDOWED
            stage.setFullScreen(false);
            stage.setMaximized(false);

            // ✅ DIMENSIONS
            stage.setMinWidth(LOGIN_MIN_WIDTH);
            stage.setMinHeight(LOGIN_MIN_HEIGHT);
            stage.setWidth(LOGIN_PREF_WIDTH);
            stage.setHeight(LOGIN_PREF_HEIGHT);

            stage.setResizable(true);

            // ✅ CENTRER
            centerWindowOnScreen(stage);

            logger.info("✅ Login stage configurée: " + LOGIN_PREF_WIDTH + "x" + LOGIN_PREF_HEIGHT);
        } catch (Exception e) {
            logger.error("❌ Erreur lors de la configuration du login", e);
        }
    }

    /**
     * Centre la fenêtre sur l'écran principal
     * Prend en compte les résolutions d'écran différentes
     */
    private static void centerWindowOnScreen(Stage stage) {
        try {
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();

            double centerX = (screenBounds.getWidth() - stage.getWidth()) / 2;
            double centerY = (screenBounds.getHeight() - stage.getHeight()) / 2;

            // Éviter que la fenêtre soit trop haute
            centerY = Math.max(centerY, 50);

            stage.setX(centerX);
            stage.setY(centerY);

            logger.debug("📍 Fenêtre centrée à: (" + centerX + ", " + centerY + ")");
        } catch (Exception e) {
            logger.error("❌ Erreur lors du centrage de la fenêtre", e);
        }
    }

    /**
     * Force le mode windowed (pas de fullscreen)
     */
    public static void forceWindowedMode(Stage stage) {
        try {
            stage.setFullScreen(false);
            stage.setMaximized(false);
            stage.setResizable(true);
            logger.info("✅ Mode windowed activé");
        } catch (Exception e) {
            logger.error("❌ Erreur lors de l'activation du mode windowed", e);
        }
    }

    /**
     * Obtient les dimensions recommandées pour le dashboard
     */
    public static double[] getDashboardDimensions() {
        return new double[]{DASHBOARD_PREF_WIDTH, DASHBOARD_PREF_HEIGHT};
    }

    /**
     * Obtient les dimensions recommandées pour le login
     */
    public static double[] getLoginDimensions() {
        return new double[]{LOGIN_PREF_WIDTH, LOGIN_PREF_HEIGHT};
    }
}