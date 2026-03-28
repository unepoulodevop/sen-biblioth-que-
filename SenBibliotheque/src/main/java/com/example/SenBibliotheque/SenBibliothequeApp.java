package com.example.SenBibliotheque;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import com.example.SenBibliotheque.config.DatabaseManager;
import com.example.SenBibliotheque.util.WindowConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SenBibliothequeApp extends Application {

    private static final Logger logger = LoggerFactory.getLogger(SenBibliothequeApp.class);

    // ✅ CONSTANTES LOGIN (petit écran de connexion)
    private static final double LOGIN_WINDOW_MIN_WIDTH = 700;
    private static final double LOGIN_WINDOW_MIN_HEIGHT = 450;
    private static final double LOGIN_WINDOW_PREF_WIDTH = 800;
    private static final double LOGIN_WINDOW_PREF_HEIGHT = 600;

    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            logger.info("🚀 Démarrage de SenBibliotheque...");

            // Initialiser la base de données
            DatabaseManager.initialize();

            // Charger l'écran de connexion
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/LoginScreen.fxml"));
            Scene scene = new Scene(loader.load(), LOGIN_WINDOW_PREF_WIDTH, LOGIN_WINDOW_PREF_HEIGHT);

            System.out.println(getClass().getResource("/css/style.css"));
            System.out.println(getClass().getResource("/css/login.css"));
            // Ajouter les feuilles de style
            scene.getStylesheets().addAll(
                    getClass().getResource("/css/style.css").toExternalForm(),
                    getClass().getResource("/css/login.css").toExternalForm()
            );

            // ✅ CONFIGURER LA FENÊTRE LOGIN
            primaryStage.setTitle("SenBibliotheque - Gestion Bibliothèque Municipale");
            primaryStage.setScene(scene);

            // ✅ DIMENSIONS LOGIN
            primaryStage.setWidth(LOGIN_WINDOW_PREF_WIDTH);
            primaryStage.setHeight(LOGIN_WINDOW_PREF_HEIGHT);
            primaryStage.setMinWidth(LOGIN_WINDOW_MIN_WIDTH);
            primaryStage.setMinHeight(LOGIN_WINDOW_MIN_HEIGHT);
            primaryStage.setResizable(true);

            // ✅ PAS DE FULLSCREEN OU MAXIMIZED
            primaryStage.setMaximized(false);
            primaryStage.setFullScreen(false);

            // ✅ CENTRER SUR L'ÉCRAN
            centerWindowOnScreen(primaryStage);

            // ✅ AJOUTER UNE ICÔNE
            try {
                javafx.scene.image.Image icon = new javafx.scene.image.Image(
                        getClass().getResourceAsStream("/images/icon.png"));
                if (!icon.isError()) {
                    primaryStage.getIcons().add(icon);
                }
            } catch (Exception e) {
                logger.warn("⚠️ Icône non trouvée: " + e.getMessage());
            }

            // ✅ ÉVENEMENT DE FERMETURE
            primaryStage.setOnCloseRequest(event -> {
                logger.info("📴 Fermeture de l'application");
                DatabaseManager.shutdown();
                System.exit(0);
            });

            // ✅ AFFICHER LA FENÊTRE
            primaryStage.show();
            logger.info("✅ Application démarrée avec succès!");
            logger.info("📐 Dimensions Login: " + LOGIN_WINDOW_PREF_WIDTH + "x" + LOGIN_WINDOW_PREF_HEIGHT);

        } catch (Exception e) {
            logger.error("❌ Erreur lors du démarrage de l'application", e);
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Centre la fenêtre sur l'écran principal
     */
    private void centerWindowOnScreen(Stage stage) {
        try {
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();

            double centerX = (screenBounds.getWidth() - stage.getWidth()) / 2;
            double centerY = (screenBounds.getHeight() - stage.getHeight()) / 2;

            centerY = Math.max(centerY, 50);

            stage.setX(centerX);
            stage.setY(centerY);

            logger.debug("📍 Fenêtre centrée à: (" + centerX + ", " + centerY + ")");
        } catch (Exception e) {
            logger.error("❌ Erreur lors du centrage de la fenêtre", e);
        }
    }

    @Override
    public void stop() throws Exception {
        logger.info("🔌 Arrêt du gestionnaire de base de données");
        DatabaseManager.shutdown();
        super.stop();
    }

    public static void main(String[] args) {
        launch(args);
    }
}