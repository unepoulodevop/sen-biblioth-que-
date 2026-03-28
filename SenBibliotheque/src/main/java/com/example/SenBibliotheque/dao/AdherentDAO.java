package com.example.SenBibliotheque.dao;

import com.example.SenBibliotheque.entity.Adherent;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class AdherentDAO {

    private static final Logger logger = LoggerFactory.getLogger(AdherentDAO.class);
    private EntityManager em;

    public AdherentDAO(EntityManager em) {
        this.em = em;
    }

    /**
     * Créer un nouvel adhérent
     */
    public Adherent create(Adherent adherent) {
        try {
            em.getTransaction().begin();
            em.persist(adherent);
            em.getTransaction().commit();
            logger.info("✅ Adhérent créé: " + adherent.getMatricule());
            return adherent;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            logger.error("❌ Erreur lors de la création de l'adhérent", e);
            throw new RuntimeException("Erreur lors de la création de l'adhérent", e);
        }
    }

    /**
     * Mettre à jour un adhérent
     */
    public Adherent update(Adherent adherent) {
        try {
            em.getTransaction().begin();
            adherent = em.merge(adherent);
            em.getTransaction().commit();
            logger.info("✅ Adhérent mis à jour: " + adherent.getMatricule());
            return adherent;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            logger.error("❌ Erreur lors de la mise à jour de l'adhérent", e);
            throw new RuntimeException("Erreur lors de la mise à jour de l'adhérent", e);
        }
    }

    /**
     * Supprimer un adhérent
     */
    public void delete(Integer id) {
        try {
            em.getTransaction().begin();
            Adherent adherent = em.find(Adherent.class, id);
            if (adherent != null) {
                em.remove(adherent);
                em.getTransaction().commit();
                logger.info("✅ Adhérent supprimé: " + id);
            }
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            logger.error("❌ Erreur lors de la suppression de l'adhérent", e);
            throw new RuntimeException("Erreur lors de la suppression de l'adhérent", e);
        }
    }

    /**
     * Récupérer un adhérent par ID
     */
    public Adherent findById(Integer id) {
        return em.find(Adherent.class, id);
    }

    /**
     * Récupérer un adhérent par matricule
     */
    public Adherent findByMatricule(String matricule) {
        try {
            return em.createQuery("SELECT a FROM Adherent a WHERE a.matricule = :matricule", Adherent.class)
                    .setParameter("matricule", matricule)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * Récupérer un adhérent par email
     */
    public Adherent findByEmail(String email) {
        try {
            return em.createQuery("SELECT a FROM Adherent a WHERE a.email = :email", Adherent.class)
                    .setParameter("email", email)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * Récupérer tous les adhérents
     */
    public List<Adherent> findAll() {
        return em.createQuery("SELECT a FROM Adherent a ORDER BY a.nom, a.prenom", Adherent.class)
                .getResultList();
    }

    /**
     * Récupérer les adhérents actifs
     */
    public List<Adherent> findActifs() {
        return em.createQuery("SELECT a FROM Adherent a WHERE a.actif = true ORDER BY a.nom, a.prenom", Adherent.class)
                .getResultList();
    }

    /**
     * Chercher les adhérents par nom
     */
    public List<Adherent> searchByNom(String nom) {
        return em.createQuery("SELECT a FROM Adherent a WHERE LOWER(a.nom) LIKE LOWER(:nom) OR LOWER(a.prenom) LIKE LOWER(:nom) ORDER BY a.nom, a.prenom", Adherent.class)
                .setParameter("nom", "%" + nom + "%")
                .getResultList();
    }

    /**
     * Compter les adhérents
     */
    public long countAll() {
        return em.createQuery("SELECT COUNT(a) FROM Adherent a", Long.class)
                .getSingleResult();
    }

    /**
     * Compter les adhérents actifs
     */
    public long countActifs() {
        return em.createQuery("SELECT COUNT(a) FROM Adherent a WHERE a.actif = true", Long.class)
                .getSingleResult();
    }
}