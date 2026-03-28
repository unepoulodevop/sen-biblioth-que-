package com.example.SenBibliotheque.dao;

import com.example.SenBibliotheque.entity.Categorie;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class CategorieDAO {

    private static final Logger logger = LoggerFactory.getLogger(CategorieDAO.class);
    private EntityManager em;

    public CategorieDAO(EntityManager em) {
        this.em = em;
    }

    /**
     * Créer une nouvelle catégorie
     */
    public Categorie create(Categorie categorie) {
        try {
            em.getTransaction().begin();
            em.persist(categorie);
            em.getTransaction().commit();
            logger.info("✅ Catégorie créée: " + categorie.getLibelle());
            return categorie;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            logger.error("❌ Erreur lors de la création de la catégorie", e);
            throw new RuntimeException("Erreur lors de la création de la catégorie", e);
        }
    }

    /**
     * Mettre à jour une catégorie
     */
    public Categorie update(Categorie categorie) {
        try {
            em.getTransaction().begin();
            categorie = em.merge(categorie);
            em.getTransaction().commit();
            logger.info("✅ Catégorie mise à jour: " + categorie.getLibelle());
            return categorie;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            logger.error("❌ Erreur lors de la mise à jour de la catégorie", e);
            throw new RuntimeException("Erreur lors de la mise à jour de la catégorie", e);
        }
    }

    /**
     * Supprimer une catégorie
     */
    public void delete(Integer id) {
        try {
            em.getTransaction().begin();
            Categorie categorie = em.find(Categorie.class, id);
            if (categorie != null) {
                em.remove(categorie);
                em.getTransaction().commit();
                logger.info("✅ Catégorie supprimée: " + id);
            }
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            logger.error("❌ Erreur lors de la suppression de la catégorie", e);
            throw new RuntimeException("Erreur lors de la suppression de la catégorie", e);
        }
    }

    /**
     * Récupérer une catégorie par ID
     */
    public Categorie findById(Integer id) {
        return em.find(Categorie.class, id);
    }

    /**
     * Récupérer une catégorie par libellé
     */
    public Categorie findByLibelle(String libelle) {
        try {
            return em.createQuery("SELECT c FROM Categorie c WHERE c.libelle = :libelle", Categorie.class)
                    .setParameter("libelle", libelle)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * Récupérer toutes les catégories
     */
    public List<Categorie> findAll() {
        return em.createQuery("SELECT c FROM Categorie c ORDER BY c.libelle", Categorie.class)
                .getResultList();
    }

    /**
     * Compter les catégories
     */
    public long countAll() {
        return em.createQuery("SELECT COUNT(c) FROM Categorie c", Long.class)
                .getSingleResult();
    }
}