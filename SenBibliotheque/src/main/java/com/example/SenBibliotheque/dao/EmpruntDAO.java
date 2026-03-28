package com.example.SenBibliotheque.dao;

import com.example.SenBibliotheque.entity.Emprunt;
import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;

public class EmpruntDAO {

    private static final Logger logger = LoggerFactory.getLogger(EmpruntDAO.class);
    private EntityManager em;

    public EmpruntDAO(EntityManager em) {
        this.em = em;
    }

    /**
     * Créer un nouvel emprunt
     */
    public Emprunt create(Emprunt emprunt) {
        try {
            em.getTransaction().begin();
            em.persist(emprunt);
            em.getTransaction().commit();
            logger.info("✅ Emprunt créé pour: " + emprunt.getAdherent().getNom());
            return emprunt;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            logger.error("❌ Erreur lors de la création de l'emprunt", e);
            throw new RuntimeException("Erreur lors de la création de l'emprunt", e);
        }
    }

    /**
     * Mettre à jour un emprunt
     */
    public Emprunt update(Emprunt emprunt) {
        try {
            em.getTransaction().begin();
            emprunt = em.merge(emprunt);
            em.getTransaction().commit();
            logger.info("✅ Emprunt mis à jour: " + emprunt.getId());
            return emprunt;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            logger.error("❌ Erreur lors de la mise à jour de l'emprunt", e);
            throw new RuntimeException("Erreur lors de la mise à jour de l'emprunt", e);
        }
    }

    /**
     * Supprimer un emprunt
     */
    public void delete(Integer id) {
        try {
            em.getTransaction().begin();
            Emprunt emprunt = em.find(Emprunt.class, id);
            if (emprunt != null) {
                em.remove(emprunt);
                em.getTransaction().commit();
                logger.info("✅ Emprunt supprimé: " + id);
            }
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            logger.error("❌ Erreur lors de la suppression de l'emprunt", e);
            throw new RuntimeException("Erreur lors de la suppression de l'emprunt", e);
        }
    }

    /**
     * Récupérer un emprunt par ID
     */
    public Emprunt findById(Integer id) {
        return em.find(Emprunt.class, id);
    }

    /**
     * Récupérer tous les emprunts
     */
    public List<Emprunt> findAll() {
        return em.createQuery("SELECT e FROM Emprunt e ORDER BY e.dateEmprunt DESC", Emprunt.class)
                .getResultList();
    }

    /**
     * Récupérer les emprunts en cours
     */
    public List<Emprunt> findEnCours() {
        return em.createQuery("SELECT e FROM Emprunt e WHERE e.dateRetourEffective IS NULL ORDER BY e.dateEmprunt DESC", Emprunt.class)
                .getResultList();
    }

    /**
     * Récupérer les emprunts en retard
     */
    public List<Emprunt> findEnRetard() {
        return em.createQuery("SELECT e FROM Emprunt e WHERE e.dateRetourEffective IS NULL AND e.dateRetourPrevue < CURRENT_TIMESTAMP ORDER BY e.dateRetourPrevue ASC", Emprunt.class)
                .getResultList();
    }

    /**
     * Récupérer les emprunts d'un adhérent
     */
    public List<Emprunt> findByAdherent(Integer adherentId) {
        return em.createQuery("SELECT e FROM Emprunt e WHERE e.adherent.id = :adherentId ORDER BY e.dateEmprunt DESC", Emprunt.class)
                .setParameter("adherentId", adherentId)
                .getResultList();
    }

    /**
     * Récupérer les emprunts en cours d'un adhérent
     */
    public List<Emprunt> findEnCoursByAdherent(Integer adherentId) {
        return em.createQuery("SELECT e FROM Emprunt e WHERE e.adherent.id = :adherentId AND e.dateRetourEffective IS NULL ORDER BY e.dateEmprunt DESC", Emprunt.class)
                .setParameter("adherentId", adherentId)
                .getResultList();
    }

    /**
     * Récupérer les emprunts d'un livre
     */
    public List<Emprunt> findByLivre(Integer livreId) {
        return em.createQuery("SELECT e FROM Emprunt e WHERE e.livre.id = :livreId ORDER BY e.dateEmprunt DESC", Emprunt.class)
                .setParameter("livreId", livreId)
                .getResultList();
    }

    /**
     * Récupérer les emprunts du mois en cours
     */
    public List<Emprunt> findDuMoisEnCours() {
        LocalDateTime maintenant = LocalDateTime.now();
        LocalDateTime debut = maintenant.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime fin = maintenant.withDayOfMonth(maintenant.getMonth().length(maintenant.toLocalDate().isLeapYear()))
                .withHour(23).withMinute(59).withSecond(59);

        return em.createQuery("SELECT e FROM Emprunt e WHERE e.dateEmprunt BETWEEN :debut AND :fin ORDER BY e.dateEmprunt DESC", Emprunt.class)
                .setParameter("debut", debut)
                .setParameter("fin", fin)
                .getResultList();
    }

    /**
     * Compter les emprunts en cours
     */
    public long countEnCours() {
        return em.createQuery("SELECT COUNT(e) FROM Emprunt e WHERE e.dateRetourEffective IS NULL", Long.class)
                .getSingleResult();
    }

    /**
     * Compter les emprunts en retard
     */
    public long countEnRetard() {
        return em.createQuery("SELECT COUNT(e) FROM Emprunt e WHERE e.dateRetourEffective IS NULL AND e.dateRetourPrevue < CURRENT_TIMESTAMP", Long.class)
                .getSingleResult();
    }
}