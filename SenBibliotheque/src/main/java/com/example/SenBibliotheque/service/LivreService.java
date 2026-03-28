package com.example.SenBibliotheque.service;

import com.example.SenBibliotheque.config.DatabaseManager;
import com.example.SenBibliotheque.dao.LivreDAO;
import com.example.SenBibliotheque.entity.Livre;
import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;

public class LivreService {

    private static final Logger logger = LoggerFactory.getLogger(LivreService.class);
    private LivreDAO dao;
    private EntityManager em;

    public LivreService() {
        this.em = DatabaseManager.getEntityManager();
        this.dao = new LivreDAO(em);
    }

    /**
     * Créer un nouveau livre
     */
    public Livre creer(Livre livre) {
        try {
            // Vérifier que l'ISBN n'existe pas
            if (dao.findByIsbn(livre.getIsbn()) != null) {
                throw new RuntimeException("Un livre avec cet ISBN existe déjà");
            }

            livre.setDateAjout(LocalDateTime.now());
            livre.setDateModification(LocalDateTime.now());

            dao.create(livre);
            logger.info("✅ Livre créé: " + livre.getTitre());
            return livre;
        } catch (Exception e) {
            logger.error("❌ Erreur lors de la création du livre", e);
            throw new RuntimeException("Erreur lors de la création du livre", e);
        }
    }

    /**
     * Mettre à jour un livre
     */
    public Livre update(Livre livre) {
        try {
            livre.setDateModification(LocalDateTime.now());
            return dao.update(livre);
        } catch (Exception e) {
            logger.error("❌ Erreur lors de la mise à jour du livre", e);
            throw new RuntimeException("Erreur lors de la mise à jour du livre", e);
        }
    }

    /**
     * Supprimer un livre
     */
    public void supprimer(Integer livreId) {
        try {
            Livre livre = dao.findById(livreId);
            if (livre != null && livre.getEmprunts().isEmpty()) {
                dao.delete(livreId);
                logger.info("✅ Livre supprimé: " + livreId);
            } else if (livre != null) {
                throw new RuntimeException("Impossible de supprimer un livre avec des emprunts actifs");
            }
        } catch (Exception e) {
            logger.error("❌ Erreur lors de la suppression du livre", e);
            throw new RuntimeException("Erreur lors de la suppression du livre", e);
        }
    }

    /**
     * Obtenir un livre par ID
     */
    public Livre obtenirParId(Integer id) {
        try {
            return dao.findById(id);
        } catch (Exception e) {
            logger.error("❌ Erreur lors de la récupération du livre", e);
            return null;
        }
    }

    /**
     * Obtenir un livre par ISBN
     */
    public Livre obtenirParIsbn(String isbn) {
        try {
            return dao.findByIsbn(isbn);
        } catch (Exception e) {
            logger.error("❌ Erreur lors de la récupération du livre", e);
            return null;
        }
    }

    /**
     * Obtenir tous les livres
     */
    public List<Livre> obtenirTous() {
        try {
            return dao.findAll();
        } catch (Exception e) {
            logger.error("❌ Erreur lors de la récupération des livres", e);
            return List.of();
        }
    }

    /**
     * Chercher les livres par titre
     */
    public List<Livre> rechercherParTitre(String titre) {
        try {
            return dao.searchByTitre(titre);
        } catch (Exception e) {
            logger.error("❌ Erreur lors de la recherche par titre", e);
            return List.of();
        }
    }

    /**
     * Chercher les livres par auteur
     */
    public List<Livre> rechercherParAuteur(String auteur) {
        try {
            return dao.searchByAuteur(auteur);
        } catch (Exception e) {
            logger.error("❌ Erreur lors de la recherche par auteur", e);
            return List.of();
        }
    }

    /**
     * Obtenir les livres par catégorie
     */
    public List<Livre> obtenirParCategorie(Integer categorieId) {
        try {
            return dao.findByCategorie(categorieId);
        } catch (Exception e) {
            logger.error("❌ Erreur lors de la récupération des livres par catégorie", e);
            return List.of();
        }
    }

    /**
     * Obtenir les livres disponibles
     */
    public List<Livre> obtenirDisponibles() {
        try {
            return dao.findDisponibles();
        } catch (Exception e) {
            logger.error("❌ Erreur lors de la récupération des livres disponibles", e);
            return List.of();
        }
    }

    /**
     * Mettre à jour la disponibilité d'un livre
     */
    public void mettreAJourDisponibilite(Integer livreId, int quantite) {
        try {
            Livre livre = dao.findById(livreId);
            if (livre != null) {
                int nouveauDisponible = livre.getDisponible() + quantite;
                if (nouveauDisponible < 0) {
                    throw new RuntimeException("Quantité disponible ne peut pas être négative");
                }
                livre.setDisponible(nouveauDisponible);
                update(livre);
                logger.info("✅ Disponibilité mise à jour pour: " + livre.getTitre());
            }
        } catch (Exception e) {
            logger.error("❌ Erreur lors de la mise à jour de la disponibilité", e);
            throw new RuntimeException("Erreur lors de la mise à jour de la disponibilité", e);
        }
    }

    /**
     * Compter les livres
     */
    public long compterTous() {
        try {
            return dao.countAll();
        } catch (Exception e) {
            logger.error("❌ Erreur lors du comptage des livres", e);
            return 0;
        }
    }

    /**
     * Compter les livres disponibles
     */
    public long compterDisponibles() {
        try {
            return dao.countDisponibles();
        } catch (Exception e) {
            logger.error("❌ Erreur lors du comptage des livres disponibles", e);
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