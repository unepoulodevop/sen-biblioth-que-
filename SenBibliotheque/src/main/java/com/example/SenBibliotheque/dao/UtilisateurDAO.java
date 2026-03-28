package com.example.SenBibliotheque.dao;

import com.example.SenBibliotheque.entity.Utilisateur;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class UtilisateurDAO {

    private static final Logger logger = LoggerFactory.getLogger(UtilisateurDAO.class);
    private EntityManager em;

    public UtilisateurDAO(EntityManager em) {
        this.em = em;
    }

    /**
     * Créer un nouvel utilisateur
     */
    public Utilisateur create(Utilisateur utilisateur) {
        try {
            em.getTransaction().begin();
            em.persist(utilisateur);
            em.getTransaction().commit();
            logger.info("✅ Utilisateur créé: " + utilisateur.getLogin());
            return utilisateur;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            logger.error("❌ Erreur lors de la création de l'utilisateur", e);
            throw new RuntimeException("Erreur lors de la création de l'utilisateur", e);
        }
    }

    /**
     * Mettre à jour un utilisateur
     */
    public Utilisateur update(Utilisateur utilisateur) {
        try {
            em.getTransaction().begin();
            utilisateur = em.merge(utilisateur);
            em.getTransaction().commit();
            logger.info("✅ Utilisateur mis à jour: " + utilisateur.getLogin());
            return utilisateur;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            logger.error("❌ Erreur lors de la mise à jour de l'utilisateur", e);
            throw new RuntimeException("Erreur lors de la mise à jour de l'utilisateur", e);
        }
    }

    /**
     * Supprimer un utilisateur
     */
    public void delete(Integer id) {
        try {
            em.getTransaction().begin();
            Utilisateur utilisateur = em.find(Utilisateur.class, id);
            if (utilisateur != null) {
                em.remove(utilisateur);
                em.getTransaction().commit();
                logger.info("✅ Utilisateur supprimé: " + id);
            }
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            logger.error("❌ Erreur lors de la suppression de l'utilisateur", e);
            throw new RuntimeException("Erreur lors de la suppression de l'utilisateur", e);
        }
    }

    /**
     * Récupérer un utilisateur par ID
     */
    public Utilisateur findById(Integer id) {
        return em.find(Utilisateur.class, id);
    }

    /**
     * Récupérer un utilisateur par login
     */
    public Utilisateur findByLogin(String login) {
        try {
            return em.createQuery("SELECT u FROM Utilisateur u WHERE u.login = :login", Utilisateur.class)
                    .setParameter("login", login)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * Récupérer un utilisateur par email
     */
    public Utilisateur findByEmail(String email) {
        try {
            return em.createQuery("SELECT u FROM Utilisateur u WHERE u.email = :email", Utilisateur.class)
                    .setParameter("email", email)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * Récupérer tous les utilisateurs
     */
    public List<Utilisateur> findAll() {
        return em.createQuery("SELECT u FROM Utilisateur u ORDER BY u.nom, u.prenom", Utilisateur.class)
                .getResultList();
    }

    /**
     * Récupérer les utilisateurs actifs
     */
    public List<Utilisateur> findActifs() {
        return em.createQuery("SELECT u FROM Utilisateur u WHERE u.actif = true ORDER BY u.nom, u.prenom", Utilisateur.class)
                .getResultList();
    }

    /**
     * Récupérer les utilisateurs par profil
     */
    public List<Utilisateur> findByProfil(String profil) {
        return em.createQuery("SELECT u FROM Utilisateur u WHERE u.profil = :profil ORDER BY u.nom, u.prenom", Utilisateur.class)
                .setParameter("profil", profil)
                .getResultList();
    }

    /**
     * Compter les utilisateurs
     */
    public long countAll() {
        return em.createQuery("SELECT COUNT(u) FROM Utilisateur u", Long.class)
                .getSingleResult();
    }
}