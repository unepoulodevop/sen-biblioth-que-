package com.example.SenBibliotheque.service;

import com.example.SenBibliotheque.config.DatabaseManager;
import com.example.SenBibliotheque.dao.CategorieDAO;
import com.example.SenBibliotheque.entity.Categorie;
import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class CategorieService {

    private static final Logger logger = LoggerFactory.getLogger(CategorieService.class);
    private CategorieDAO dao;
    private EntityManager em;

    public CategorieService() {
        this.em = DatabaseManager.getEntityManager();
        this.dao = new CategorieDAO(em);
    }

    /**
     * Créer une nouvelle catégorie
     */
    public Categorie creer(String libelle, String description) {
        try {
            // Vérifier que la catégorie n'existe pas
            if (dao.findByLibelle(libelle) != null) {
                throw new RuntimeException("Cette catégorie existe déjà");
            }

            Categorie categorie = new Categorie(libelle, description);
            dao.create(categorie);
            logger.info("✅ Catégorie créée: " + libelle);
            return categorie;
        } catch (Exception e) {
            logger.error("❌ Erreur lors de la création de la catégorie", e);
            throw new RuntimeException("Erreur lors de la création de la catégorie", e);
        }
    }

    /**
     * Mettre à jour une catégorie
     */
    public Categorie update(Categorie categorie) {
        try {
            return dao.update(categorie);
        } catch (Exception e) {
            logger.error("❌ Erreur lors de la mise à jour de la catégorie", e);
            throw new RuntimeException("Erreur lors de la mise à jour de la catégorie", e);
        }
    }

    /**
     * Supprimer une catégorie
     */
    public void supprimer(Integer categorieId) {
        try {
            Categorie categorie = dao.findById(categorieId);
            if (categorie != null && categorie.getLivres().isEmpty()) {
                dao.delete(categorieId);
                logger.info("✅ Catégorie supprimée: " + categorieId);
            } else if (categorie != null) {
                throw new RuntimeException("Impossible de supprimer une catégorie avec des livres");
            }
        } catch (Exception e) {
            logger.error("❌ Erreur lors de la suppression de la catégorie", e);
            throw new RuntimeException("Erreur lors de la suppression de la catégorie", e);
        }
    }

    /**
     * Obtenir une catégorie par ID
     */
    public Categorie obtenirParId(Integer id) {
        try {
            return dao.findById(id);
        } catch (Exception e) {
            logger.error("❌ Erreur lors de la récupération de la catégorie", e);
            return null;
        }
    }

    /**
     * Obtenir toutes les catégories
     */
    public List<Categorie> obtenirTous() {
        try {
            return dao.findAll();
        } catch (Exception e) {
            logger.error("❌ Erreur lors de la récupération des catégories", e);
            return List.of();
        }
    }

    /**
     * Compter les catégories
     */
    public long compterTous() {
        try {
            return dao.countAll();
        } catch (Exception e) {
            logger.error("❌ Erreur lors du comptage des catégories", e);
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