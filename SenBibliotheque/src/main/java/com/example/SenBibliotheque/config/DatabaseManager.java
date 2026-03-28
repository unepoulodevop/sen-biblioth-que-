package com.example.SenBibliotheque.config;

import com.example.SenBibliotheque.entity.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

public class DatabaseManager {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseManager.class);
    private static final String PERSISTENCE_UNIT = "SenBibliotheque_pu";
    private static EntityManagerFactory emf;

    public static void initialize() {
        try {
            emf = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT);
            EntityManager em = emf.createEntityManager();

            logger.info("✅ Base de données initialisée avec succès!");

            // Initialiser les données de test
            initializeData(em);

            em.close();
        } catch (Exception e) {
            logger.error("❌ Erreur lors de l'initialisation de la BD:", e);
            throw new RuntimeException("Impossible de démarrer l'application", e);
        }
    }

    private static void initializeData(EntityManager em) {
        try {
            em.getTransaction().begin();

            // Vérifier si l'admin existe déjà
            long adminCount = em.createQuery(
                            "SELECT COUNT(u) FROM Utilisateur u WHERE u.login = 'admin'",
                            Long.class)
                    .getSingleResult();

            if (adminCount == 0) {
                // Créer l'utilisateur administrateur par défaut
                Utilisateur admin = new Utilisateur(
                        "admin",
                        BCrypt.hashpw("admin123", BCrypt.gensalt()),
                        "Administrateur",
                        "Système",
                        "admin@SenBibliotheque.local",
                        "ADMIN"
                );
                admin.setForceChangementMdp(true);
                em.persist(admin);

                logger.info("✅ Compte administrateur créé (login: admin, mot de passe: admin123)");
            }

            // Vérifier et créer les catégories de test
            long categCount = em.createQuery(
                            "SELECT COUNT(c) FROM Categorie c",
                            Long.class)
                    .getSingleResult();

            if (categCount == 0) {
                String[] categories = {
                        "Romans",
                        "Science-Fiction",
                        "Histoire",
                        "Biographie",
                        "Enfants",
                        "Technique",
                        "Philosophie",
                        "Poésie"
                };

                for (String categName : categories) {
                    Categorie cat = new Categorie(categName, "Catégorie " + categName);
                    em.persist(cat);
                }

                logger.info("✅ Catégories de test créées");
            }

            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            logger.warn("⚠️ Erreur lors de l'initialisation des données: " + e.getMessage());
        }
    }

    public static EntityManagerFactory getEntityManagerFactory() {
        if (emf == null) {
            initialize();
        }
        return emf;
    }

    public static EntityManager getEntityManager() {
        return getEntityManagerFactory().createEntityManager();
    }

    public static void shutdown() {
        if (emf != null && emf.isOpen()) {
            emf.close();
            logger.info("✅ Base de données fermée correctement");
        }
    }
}