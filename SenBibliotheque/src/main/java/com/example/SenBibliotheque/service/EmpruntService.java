package com.example.SenBibliotheque.service;

import com.example.SenBibliotheque.config.DatabaseManager;
import com.example.SenBibliotheque.dao.EmpruntDAO;
import com.example.SenBibliotheque.dao.LivreDAO;
import com.example.SenBibliotheque.entity.Adherent;
import com.example.SenBibliotheque.entity.Emprunt;
import com.example.SenBibliotheque.entity.Livre;
import com.example.SenBibliotheque.entity.Utilisateur;
import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;

public class EmpruntService {

    private static final Logger logger = LoggerFactory.getLogger(EmpruntService.class);
    private EmpruntDAO dao;
    private LivreDAO livreDAO;
    private EntityManager em;

    public EmpruntService() {
        this.em = DatabaseManager.getEntityManager();
        this.dao = new EmpruntDAO(em);
        this.livreDAO = new LivreDAO(em);
    }

    /**
     * Créer un nouvel emprunt
     */
    public Emprunt creer(Livre livre, Adherent adherent, Utilisateur utilisateur, LocalDateTime dateRetourPrevue) {
        try {
            // Vérifier la disponibilité du livre
            if (livre.getDisponible() <= 0) {
                throw new RuntimeException("Le livre n'est pas disponible");
            }

            // Vérifier que l'adhérent est actif
            if (!adherent.getActif()) {
                throw new RuntimeException("L'adhérent n'est pas actif");
            }

            // Créer l'emprunt
            Emprunt emprunt = new Emprunt(livre, adherent, utilisateur, dateRetourPrevue);
            dao.create(emprunt);

            // Mettre à jour la disponibilité du livre
            livre.setDisponible(livre.getDisponible() - 1);
            livreDAO.update(livre);

            logger.info("✅ Emprunt créé: " + adherent.getNom() + " - " + livre.getTitre());
            return emprunt;
        } catch (Exception e) {
            logger.error("❌ Erreur lors de la création de l'emprunt", e);
            throw new RuntimeException("Erreur lors de la création de l'emprunt", e);
        }
    }
    /**
     * Enregistrer un retour de livre
     */
    public Emprunt enregistrerRetour(Integer empruntId, LocalDateTime dateRetourEffective) {
        try {
            Emprunt emprunt = dao.findById(empruntId);

            if (emprunt == null) {
                throw new RuntimeException("Emprunt non trouvé");
            }

            if (emprunt.getDateRetourEffective() != null) {
                throw new RuntimeException("Ce livre a déjà été retourné");
            }

            // Enregistrer le retour
            emprunt.setDateRetourEffective(dateRetourEffective);

            // Calculer les pénalités
            emprunt.calculerPenalite();

            // Log les informations
            if (emprunt.getPenalite() > 0) {
                logger.warn("⚠️ Retard détecté - " +
                        "Jours: " + emprunt.getJoursRetard() +
                        ", Pénalité: " + String.format("%.2f", emprunt.getPenalite()) + " FCFA");
            }

            dao.update(emprunt);

            // Mettre à jour la disponibilité du livre
            Livre livre = emprunt.getLivre();
            livre.setDisponible(livre.getDisponible() + 1);
            livreDAO.update(livre);

            logger.info("✅ Retour enregistré: " + emprunt.getAdherent().getNom() + " - " + emprunt.getLivre().getTitre());
            return emprunt;
        } catch (Exception e) {
            logger.error("❌ Erreur lors de l'enregistrement du retour", e);
            throw new RuntimeException("Erreur lors de l'enregistrement du retour", e);
        }
    }

    /**
     * Obtenir un emprunt par ID
     */
    public Emprunt obtenirParId(Integer id) {
        try {
            return dao.findById(id);
        } catch (Exception e) {
            logger.error("❌ Erreur lors de la récupération de l'emprunt", e);
            return null;
        }
    }

    /**
     * Obtenir tous les emprunts
     */
    public List<Emprunt> obtenirTous() {
        try {
            return dao.findAll();
        } catch (Exception e) {
            logger.error("❌ Erreur lors de la récupération des emprunts", e);
            return List.of();
        }
    }

    /**
     * Obtenir les emprunts en cours
     */
    public List<Emprunt> obtenirEnCours() {
        try {
            return dao.findEnCours();
        } catch (Exception e) {
            logger.error("❌ Erreur lors de la récupération des emprunts en cours", e);
            return List.of();
        }
    }

    /**
     * Obtenir les emprunts en retard
     */
    public List<Emprunt> obtenirEnRetard() {
        try {
            return dao.findEnRetard();
        } catch (Exception e) {
            logger.error("❌ Erreur lors de la récupération des emprunts en retard", e);
            return List.of();
        }
    }

    /**
     * Obtenir les emprunts d'un adhérent
     */
    public List<Emprunt> obtenirParAdherent(Integer adherentId) {
        try {
            return dao.findByAdherent(adherentId);
        } catch (Exception e) {
            logger.error("❌ Erreur lors de la récupération des emprunts de l'adhérent", e);
            return List.of();
        }
    }

    /**
     * Obtenir les emprunts en cours d'un adhérent
     */
    public List<Emprunt> obtenirEnCoursByAdherent(Integer adherentId) {
        try {
            return dao.findEnCoursByAdherent(adherentId);
        } catch (Exception e) {
            logger.error("❌ Erreur lors de la récupération des emprunts en cours de l'adhérent", e);
            return List.of();
        }
    }

    /**
     * Obtenir les emprunts d'un livre
     */
    public List<Emprunt> obtenirParLivre(Integer livreId) {
        try {
            return dao.findByLivre(livreId);
        } catch (Exception e) {
            logger.error("❌ Erreur lors de la récupération des emprunts du livre", e);
            return List.of();
        }
    }

    /**
     * Obtenir les emprunts du mois en cours
     */
    public List<Emprunt> obtenirDuMoisEnCours() {
        try {
            return dao.findDuMoisEnCours();
        } catch (Exception e) {
            logger.error("❌ Erreur lors de la récupération des emprunts du mois", e);
            return List.of();
        }
    }

    /**
     * Compter les emprunts en cours
     */
    public long compterEnCours() {
        try {
            return dao.countEnCours();
        } catch (Exception e) {
            logger.error("❌ Erreur lors du comptage des emprunts en cours", e);
            return 0;
        }
    }

    /**
     * Compter les emprunts en retard
     */
    public long compterEnRetard() {
        try {
            return dao.countEnRetard();
        } catch (Exception e) {
            logger.error("❌ Erreur lors du comptage des emprunts en retard", e);
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