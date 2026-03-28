package com.example.SenBibliotheque.repository;

import com.example.SenBibliotheque.entity.Utilisateur;

/**
 * Interface Repository pour les opérations CRUD sur Utilisateur
 * Cette interface est utilisée comme abstraction sur le DAO
 */
public interface UtilisateurRepository {

    /**
     * Trouve un utilisateur par son login
     */
    Utilisateur findByLogin(String login);

    /**
     * Trouve un utilisateur par son email
     */
    Utilisateur findByEmail(String email);

    /**
     * Trouve un utilisateur par son ID
     */
    Utilisateur findById(Integer id);

    /**
     * Crée un nouvel utilisateur
     */
    void create(Utilisateur utilisateur);

    /**
     * Met à jour un utilisateur
     */
    Utilisateur update(Utilisateur utilisateur);

    /**
     * Supprime un utilisateur
     */
    void delete(Integer userId);

    /**
     * Récupère tous les utilisateurs
     */
    java.util.List<Utilisateur> findAll();

    /**
     * Récupère les utilisateurs actifs
     */
    java.util.List<Utilisateur> findActifs();

    /**
     * Compte le nombre total d'utilisateurs
     */
    long countAll();
}