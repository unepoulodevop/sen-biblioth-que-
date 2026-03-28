package com.example.SenBibliotheque.service;

import com.example.SenBibliotheque.config.DatabaseManager;
import com.example.SenBibliotheque.dao.UtilisateurDAO;
import com.example.SenBibliotheque.entity.Utilisateur;
import com.example.SenBibliotheque.util.PasswordUtil;
import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;

public class UtilisateurService {

    private static final Logger logger = LoggerFactory.getLogger(UtilisateurService.class);
    private UtilisateurDAO dao;
    private EntityManager em;

    public UtilisateurService() {
        this.em = DatabaseManager.getEntityManager();
        this.dao = new UtilisateurDAO(em);
    }

    /**
     * Authentifier un utilisateur
     */
    public Utilisateur authentifier(String login, String motDePasse) {
        try {
            Utilisateur utilisateur = dao.findByLogin(login);

            if (utilisateur == null) {
                logger.warn("❌ Tentative de connexion avec un login inexistant: " + login);
                return null;
            }

            if (!utilisateur.getActif()) {
                logger.warn("❌ Tentative de connexion avec un compte désactivé: " + login);
                return null;
            }

            if (!PasswordUtil.verifyPassword(motDePasse, utilisateur.getMotDePasse())) {
                logger.warn("❌ Mot de passe incorrect pour: " + login);
                return null;
            }

            // Mettre à jour la dernière connexion
            utilisateur.setDerniereConnexion(LocalDateTime.now());
            update(utilisateur);

            logger.info("✅ Connexion réussie: " + login);
            return utilisateur;
        } catch (Exception e) {
            logger.error("❌ Erreur lors de l'authentification", e);
            return null;
        }
    }

    /**
     * Créer un nouvel utilisateur
     */
    public Utilisateur creer(String login, String nom, String prenom, String email, String profil) {
        try {
            // Vérifier que le login n'existe pas
            if (dao.findByLogin(login) != null) {
                throw new RuntimeException("Ce login existe déjà");
            }

            // Générer un mot de passe aléatoire
            String motDePasse = PasswordUtil.generateRandomPassword(12);

            Utilisateur utilisateur = new Utilisateur(login, PasswordUtil.hashPassword(motDePasse), nom, prenom, email, profil);
            utilisateur.setForceChangementMdp(true);

            dao.create(utilisateur);
            logger.info("✅ Utilisateur créé: " + login + " (mot de passe temporaire généré)");
            return utilisateur;
        } catch (Exception e) {
            logger.error("❌ Erreur lors de la création de l'utilisateur", e);
            throw new RuntimeException("Erreur lors de la création de l'utilisateur", e);
        }
    }

    /**
     * Mettre à jour un utilisateur
     */
    public Utilisateur update(Utilisateur utilisateur) {
        try {
            utilisateur.setDateModification(LocalDateTime.now());
            return dao.update(utilisateur);
        } catch (Exception e) {
            logger.error("❌ Erreur lors de la mise à jour de l'utilisateur", e);
            throw new RuntimeException("Erreur lors de la mise à jour de l'utilisateur", e);
        }
    }

    /**
     * Changer le mot de passe
     */
    public boolean changerMotDePasse(Integer userId, String ancienMotDePasse, String nouveauMotDePasse) {
        try {
            Utilisateur utilisateur = dao.findById(userId);

            if (utilisateur == null) {
                throw new RuntimeException("Utilisateur non trouvé");
            }

            // Vérifier l'ancien mot de passe
            if (!PasswordUtil.verifyPassword(ancienMotDePasse, utilisateur.getMotDePasse())) {
                throw new RuntimeException("Ancien mot de passe incorrect");
            }

            // Vérifier la force du nouveau mot de passe
            PasswordUtil.PasswordStrength strength = PasswordUtil.validatePasswordStrength(nouveauMotDePasse);
            if (strength == PasswordUtil.PasswordStrength.FAIBLE) {
                throw new RuntimeException("Le mot de passe est trop faible");
            }

            // Mettre à jour le mot de passe
            utilisateur.setMotDePasse(PasswordUtil.hashPassword(nouveauMotDePasse));
            utilisateur.setForceChangementMdp(false);
            update(utilisateur);

            logger.info("✅ Mot de passe changé pour: " + utilisateur.getLogin());
            return true;
        } catch (Exception e) {
            logger.error("❌ Erreur lors du changement de mot de passe", e);
            throw new RuntimeException("Erreur lors du changement de mot de passe", e);
        }
    }

    /**
     * Réinitialiser le mot de passe (Admin only)
     */
    public String reinitialiserMotDePasse(Integer userId) {
        try {
            Utilisateur utilisateur = dao.findById(userId);

            if (utilisateur == null) {
                throw new RuntimeException("Utilisateur non trouvé");
            }

            String nouveauMotDePasse = PasswordUtil.generateRandomPassword(12);
            utilisateur.setMotDePasse(PasswordUtil.hashPassword(nouveauMotDePasse));
            utilisateur.setForceChangementMdp(true);
            update(utilisateur);

            logger.info("✅ Mot de passe réinitialisé pour: " + utilisateur.getLogin());
            return nouveauMotDePasse;
        } catch (Exception e) {
            logger.error("❌ Erreur lors de la réinitialisation du mot de passe", e);
            throw new RuntimeException("Erreur lors de la réinitialisation du mot de passe", e);
        }
    }

    /**
     * Rechercher un utilisateur par login ou email (NOUVEAU - Pour mot de passe oublié)
     */
    public Utilisateur rechercherParLoginOuEmail(String loginOuEmail) {
        try {
            // Chercher par login d'abord
            Utilisateur utilisateur = dao.findByLogin(loginOuEmail);

            // Si non trouvé, chercher par email
            if (utilisateur == null) {
                utilisateur = dao.findByEmail(loginOuEmail);
            }

            if (utilisateur != null) {
                logger.info("✅ Utilisateur trouvé: " + loginOuEmail);
            } else {
                logger.warn("⚠️ Aucun utilisateur trouvé pour: " + loginOuEmail);
            }

            return utilisateur;
        } catch (Exception e) {
            logger.error("❌ Erreur lors de la recherche de l'utilisateur", e);
            throw new RuntimeException("Erreur lors de la recherche de l'utilisateur: " + e.getMessage());
        }
    }

    /**
     * Réinitialiser le mot de passe depuis l'écran de connexion (Admin only - NOUVEAU)
     */
    public void resetMotDePasse(Integer utilisateurId, String nouveauMotDePasse) {
        try {
            Utilisateur utilisateur = dao.findById(utilisateurId);

            if (utilisateur == null) {
                throw new RuntimeException("Utilisateur non trouvé");
            }

            // Vérifier que c'est un admin
            if (!"ADMIN".equals(utilisateur.getProfil())) {
                throw new RuntimeException("Seuls les administrateurs peuvent réinitialiser leur mot de passe via cette interface");
            }

            // Vérifier la force du nouveau mot de passe
            PasswordUtil.PasswordStrength strength = PasswordUtil.validatePasswordStrength(nouveauMotDePasse);
            if (strength == PasswordUtil.PasswordStrength.FAIBLE) {
                throw new RuntimeException("Le mot de passe est trop faible. Utilisez au moins 8 caractères avec majuscules, minuscules et chiffres");
            }

            // Hacher et mettre à jour le mot de passe
            utilisateur.setMotDePasse(PasswordUtil.hashPassword(nouveauMotDePasse));
            utilisateur.setForceChangementMdp(false);

            update(utilisateur);

            logger.info("✅ Mot de passe réinitialisé avec succès pour: " + utilisateur.getLogin());
        } catch (Exception e) {
            logger.error("❌ Erreur lors de la réinitialisation du mot de passe", e);
            throw new RuntimeException("Erreur lors de la réinitialisation: " + e.getMessage());
        }
    }

    /**
     * Désactiver un utilisateur
     */
    public void desactiver(Integer userId) {
        try {
            Utilisateur utilisateur = dao.findById(userId);
            if (utilisateur != null) {
                utilisateur.setActif(false);
                update(utilisateur);
                logger.info("✅ Utilisateur désactivé: " + utilisateur.getLogin());
            }
        } catch (Exception e) {
            logger.error("❌ Erreur lors de la désactivation de l'utilisateur", e);
            throw new RuntimeException("Erreur lors de la désactivation de l'utilisateur", e);
        }
    }

    /**
     * Réactiver un utilisateur
     */
    public void reactiver(Integer userId) {
        try {
            Utilisateur utilisateur = dao.findById(userId);
            if (utilisateur != null) {
                utilisateur.setActif(true);
                update(utilisateur);
                logger.info("✅ Utilisateur réactivé: " + utilisateur.getLogin());
            }
        } catch (Exception e) {
            logger.error("❌ Erreur lors de la réactivation de l'utilisateur", e);
            throw new RuntimeException("Erreur lors de la réactivation de l'utilisateur", e);
        }
    }

    /**
     * Supprimer un utilisateur
     */
    public void supprimer(Integer userId) {
        try {
            dao.delete(userId);
            logger.info("✅ Utilisateur supprimé: " + userId);
        } catch (Exception e) {
            logger.error("❌ Erreur lors de la suppression de l'utilisateur", e);
            throw new RuntimeException("Erreur lors de la suppression de l'utilisateur", e);
        }
    }

    /**
     * Récupérer tous les utilisateurs
     */
    public List<Utilisateur> obtenirTous() {
        try {
            return dao.findAll();
        } catch (Exception e) {
            logger.error("❌ Erreur lors de la récupération des utilisateurs", e);
            return List.of();
        }
    }

    /**
     * Récupérer les utilisateurs actifs
     */
    public List<Utilisateur> obtenirActifs() {
        try {
            return dao.findActifs();
        } catch (Exception e) {
            logger.error("❌ Erreur lors de la récupération des utilisateurs actifs", e);
            return List.of();
        }
    }

    /**
     * Obtenir un utilisateur par ID
     */
    public Utilisateur obtenirParId(Integer id) {
        try {
            return dao.findById(id);
        } catch (Exception e) {
            logger.error("❌ Erreur lors de la récupération de l'utilisateur", e);
            return null;
        }
    }

    /**
     * Compter les utilisateurs
     */
    public long compterTous() {
        try {
            return dao.countAll();
        } catch (Exception e) {
            logger.error("❌ Erreur lors du comptage des utilisateurs", e);
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