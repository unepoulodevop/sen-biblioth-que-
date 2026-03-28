package com.example.SenBibliotheque.util;

import com.example.SenBibliotheque.entity.Utilisateur;

import java.time.LocalDateTime;

public class SessionManager {

    private static SessionManager instance;
    private Utilisateur utilisateurConnecte;
    private LocalDateTime dateConnexion;

    private SessionManager() {
    }

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public void setUtilisateurConnecte(Utilisateur utilisateur) {
        this.utilisateurConnecte = utilisateur;
        this.dateConnexion = java.time.LocalDateTime.now();
    }

    public Utilisateur getUtilisateurConnecte() {
        return utilisateurConnecte;
    }

    public boolean isConnecte() {
        return utilisateurConnecte != null;
    }

    public void deconnecter() {
        utilisateurConnecte = null;
        dateConnexion = null;
    }

    public boolean isAdmin() {
        return utilisateurConnecte != null && "ADMIN".equals(utilisateurConnecte.getProfil());
    }

    public boolean isBibliothecaire() {
        return utilisateurConnecte != null && "BIBLIOTHECAIRE".equals(utilisateurConnecte.getProfil());
    }
}