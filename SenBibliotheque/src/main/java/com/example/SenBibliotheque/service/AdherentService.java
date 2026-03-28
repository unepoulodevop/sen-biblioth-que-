package com.example.SenBibliotheque.service;

import com.example.SenBibliotheque.config.DatabaseManager;
import com.example.SenBibliotheque.dao.AdherentDAO;
import com.example.SenBibliotheque.entity.Adherent;
import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class AdherentService {

    private static final Logger logger = LoggerFactory.getLogger(AdherentService.class);
    private AdherentDAO dao;
    private EntityManager em;

    public AdherentService() {
        this.em = DatabaseManager.getEntityManager();
        this.dao = new AdherentDAO(em);
    }

    /**
     * Créer un nouvel adhérent
     */
    public Adherent creer(String nom, String prenom, String email, String telephone, String adresse) {
        try {
            // Vérifier que l'email n'existe pas
            if (dao.findByEmail(email) != null) {
                throw new RuntimeException("Cet email est déjà utilisé");
            }

            // Générer un matricule unique
            String matricule = "ADH" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

            Adherent adherent = new Adherent(matricule, nom, prenom, email, telephone);
            adherent.setAdresse(adresse);

            dao.create(adherent);
            logger.info("✅ Adhérent créé: " + matricule);
            return adherent;
        } catch (Exception e) {
            logger.error("❌ Erreur lors de la création de l'adhérent", e);
            throw new RuntimeException("Erreur lors de la création de l'adhérent", e);
        }
    }

    /**
     * Mettre à jour un adhérent
     */
    public Adherent update(Adherent adherent) {
        try {
            return dao.update(adherent);
        } catch (Exception e) {
            logger.error("❌ Erreur lors de la mise à jour de l'adhérent", e);
            throw new RuntimeException("Erreur lors de la mise à jour de l'adhérent", e);
        }
    }

    /**
     * Supprimer un adhérent
     */
    public void supprimer(Integer adherentId) {
        try {
            Adherent adherent = dao.findById(adherentId);
            if (adherent != null && adherent.getEmprunts().isEmpty()) {
                dao.delete(adherentId);
                logger.info("✅ Adhérent supprimé: " + adherentId);
            } else if (adherent != null) {
                throw new RuntimeException("Impossible de supprimer un adhérent avec des emprunts actifs");
            }
        } catch (Exception e) {
            logger.error("❌ Erreur lors de la suppression de l'adhérent", e);
            throw new RuntimeException("Erreur lors de la suppression de l'adhérent", e);
        }
    }

    /**
     * Suspendre un adhérent
     */
    public void suspendre(Integer adherentId) {
        try {
            Adherent adherent = dao.findById(adherentId);
            if (adherent != null) {
                adherent.setActif(false);
                adherent.setDateSuspension(LocalDateTime.now());
                update(adherent);
                logger.info("✅ Adhérent suspendu: " + adherent.getMatricule());
            }
        } catch (Exception e) {
            logger.error("❌ Erreur lors de la suspension de l'adhérent", e);
            throw new RuntimeException("Erreur lors de la suspension de l'adhérent", e);
        }
    }

    /**
     * Réactiver un adhérent
     */
    public void reactiver(Integer adherentId) {
        try {
            Adherent adherent = dao.findById(adherentId);
            if (adherent != null) {
                adherent.setActif(true);
                adherent.setDateSuspension(null);
                update(adherent);
                logger.info("✅ Adhérent réactivé: " + adherent.getMatricule());
            }
        } catch (Exception e) {
            logger.error("❌ Erreur lors de la réactivation de l'adhérent", e);
            throw new RuntimeException("Erreur lors de la réactivation de l'adhérent", e);
        }
    }

    /**
     * Obtenir un adhérent par ID
     */
    public Adherent obtenirParId(Integer id) {
        try {
            return dao.findById(id);
        } catch (Exception e) {
            logger.error("❌ Erreur lors de la récupération de l'adhérent", e);
            return null;
        }
    }

    /**
     * Obtenir un adhérent par matricule
     */
    public Adherent obtenirParMatricule(String matricule) {
        try {
            return dao.findByMatricule(matricule);
        } catch (Exception e) {
            logger.error("❌ Erreur lors de la récupération de l'adhérent", e);
            return null;
        }
    }

    /**
     * Obtenir tous les adhérents
     */
    public List<Adherent> obtenirTous() {
        try {
            return dao.findAll();
        } catch (Exception e) {
            logger.error("❌ Erreur lors de la récupération des adhérents", e);
            return List.of();
        }
    }

    /**
     * Obtenir les adhérents actifs
     */
    public List<Adherent> obtenirActifs() {
        try {
            return dao.findActifs();
        } catch (Exception e) {
            logger.error("❌ Erreur lors de la récupération des adhérents actifs", e);
            return List.of();
        }
    }

    /**
     * Chercher les adhérents par nom
     */
    public List<Adherent> rechercherParNom(String nom) {
        try {
            return dao.searchByNom(nom);
        } catch (Exception e) {
            logger.error("❌ Erreur lors de la recherche par nom", e);
            return List.of();
        }
    }

    /**
     * Compter les adhérents
     */
    public long compterTous() {
        try {
            return dao.countAll();
        } catch (Exception e) {
            logger.error("❌ Erreur lors du comptage des adhérents", e);
            return 0;
        }
    }

    /**
     * Compter les adhérents actifs
     */
    public long compterActifs() {
        try {
            return dao.countActifs();
        } catch (Exception e) {
            logger.error("❌ Erreur lors du comptage des adhérents actifs", e);
            return 0;
        }
    }

    /**
     * Fermer la session
     */
    public void fermer() {
        if (em != null && em.isOpen()) {
            em.close();
        }
    }
}