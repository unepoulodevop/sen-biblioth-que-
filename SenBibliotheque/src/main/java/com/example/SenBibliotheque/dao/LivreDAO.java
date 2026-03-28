package com.example.SenBibliotheque.dao;

import com.example.SenBibliotheque.entity.Livre;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class LivreDAO {

    private static final Logger logger = LoggerFactory.getLogger(LivreDAO.class);
    private EntityManager em;

    public LivreDAO(EntityManager em) {
        this.em = em;
    }

    /**
     * Créer un nouveau livre
     */
    public Livre create(Livre livre) {
        try {
            em.getTransaction().begin();
            em.persist(livre);
            em.getTransaction().commit();
            logger.info("✅ Livre créé: " + livre.getTitre());
            return livre;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            logger.error("❌ Erreur lors de la création du livre", e);
            throw new RuntimeException("Erreur lors de la création du livre", e);
        }
    }

    /**
     * Mettre à jour un livre
     */
    public Livre update(Livre livre) {
        try {
            em.getTransaction().begin();
            livre = em.merge(livre);
            em.getTransaction().commit();
            logger.info("✅ Livre mis à jour: " + livre.getTitre());
            return livre;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            logger.error("❌ Erreur lors de la mise à jour du livre", e);
            throw new RuntimeException("Erreur lors de la mise à jour du livre", e);
        }
    }

    /**
     * Supprimer un livre
     */
    public void delete(Integer id) {
        try {
            em.getTransaction().begin();
            Livre livre = em.find(Livre.class, id);
            if (livre != null) {
                em.remove(livre);
                em.getTransaction().commit();
                logger.info("✅ Livre supprimé: " + id);
            }
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            logger.error("❌ Erreur lors de la suppression du livre", e);
            throw new RuntimeException("Erreur lors de la suppression du livre", e);
        }
    }

    /**
     * Récupérer un livre par ID
     */
    public Livre findById(Integer id) {
        return em.find(Livre.class, id);
    }

    /**
     * Récupérer un livre par ISBN
     */
    public Livre findByIsbn(String isbn) {
        try {
            return em.createQuery("SELECT l FROM Livre l WHERE l.isbn = :isbn", Livre.class)
                    .setParameter("isbn", isbn)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * Récupérer tous les livres
     */
    public List<Livre> findAll() {
        return em.createQuery("SELECT l FROM Livre l ORDER BY l.titre", Livre.class)
                .getResultList();
    }

    /**
     * Chercher les livres par titre
     */
    public List<Livre> searchByTitre(String titre) {
        return em.createQuery("SELECT l FROM Livre l WHERE LOWER(l.titre) LIKE LOWER(:titre) ORDER BY l.titre", Livre.class)
                .setParameter("titre", "%" + titre + "%")
                .getResultList();
    }

    /**
     * Chercher les livres par auteur
     */
    public List<Livre> searchByAuteur(String auteur) {
        return em.createQuery("SELECT l FROM Livre l WHERE LOWER(l.auteur) LIKE LOWER(:auteur) ORDER BY l.auteur", Livre.class)
                .setParameter("auteur", "%" + auteur + "%")
                .getResultList();
    }

    /**
     * Récupérer les livres par catégorie
     */
    public List<Livre> findByCategorie(Integer categorieId) {
        return em.createQuery("SELECT l FROM Livre l WHERE l.categorie.id = :categorieId ORDER BY l.titre", Livre.class)
                .setParameter("categorieId", categorieId)
                .getResultList();
    }

    /**
     * Récupérer les livres disponibles
     */
    public List<Livre> findDisponibles() {
        return em.createQuery("SELECT l FROM Livre l WHERE l.disponible > 0 ORDER BY l.titre", Livre.class)
                .getResultList();
    }

    /**
     * Compter les livres
     */
    public long countAll() {
        return em.createQuery("SELECT COUNT(l) FROM Livre l", Long.class)
                .getSingleResult();
    }

    /**
     * Compter les livres disponibles
     */
    public long countDisponibles() {
        return em.createQuery("SELECT COUNT(l) FROM Livre l WHERE l.disponible > 0", Long.class)
                .getSingleResult();
    }
}