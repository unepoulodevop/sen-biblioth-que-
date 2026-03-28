package com.example.SenBibliotheque.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "utilisateurs", indexes = {
        @Index(name = "idx_login", columnList = "login", unique = true),
        @Index(name = "idx_email", columnList = "email", unique = true)
})
public class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "login", nullable = false, unique = true, length = 50)
    private String login;

    @Column(name = "mot_de_passe", nullable = false, length = 255)
    private String motDePasse;

    @Column(name = "nom", nullable = false, length = 100)
    private String nom;

    @Column(name = "prenom", nullable = false, length = 100)
    private String prenom;

    @Column(name = "email", unique = true, length = 100)
    private String email;

    @Column(name = "profil", nullable = false, length = 20)
    private String profil; // ADMIN, BIBLIOTHECAIRE

    @Column(name = "actif")
    private Boolean actif = true;

    @Column(name = "date_creation")
    private LocalDateTime dateCreation;

    @Column(name = "derniere_connexion")
    private LocalDateTime derniereConnexion;

    @Column(name = "force_changement_mdp")
    private Boolean forceChangementMdp = false;

    // ✅ AJOUT : Champ manquant
    @Column(name = "date_modification")
    private LocalDateTime dateModification;

    // Constructeurs
    public Utilisateur() {
    }

    public Utilisateur(String login, String motDePasse, String nom, String prenom, String email, String profil) {
        this.login = login;
        this.motDePasse = motDePasse;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.profil = profil;
        this.actif = true;
        this.forceChangementMdp = true;
        this.dateCreation = LocalDateTime.now();
        this.dateModification = LocalDateTime.now();
    }

    // Getters & Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getMotDePasse() {
        return motDePasse;
    }

    public void setMotDePasse(String motDePasse) {
        this.motDePasse = motDePasse;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProfil() {
        return profil;
    }

    public void setProfil(String profil) {
        this.profil = profil;
    }

    public Boolean getActif() {
        return actif;
    }

    public void setActif(Boolean actif) {
        this.actif = actif;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    public LocalDateTime getDerniereConnexion() {
        return derniereConnexion;
    }

    public void setDerniereConnexion(LocalDateTime derniereConnexion) {
        this.derniereConnexion = derniereConnexion;
    }

    public Boolean getForceChangementMdp() {
        return forceChangementMdp;
    }

    public void setForceChangementMdp(Boolean forceChangementMdp) {
        this.forceChangementMdp = forceChangementMdp;
    }

    // ✅ AJOUT : Getter et Setter manquants
    public LocalDateTime getDateModification() {
        return dateModification;
    }

    public void setDateModification(LocalDateTime dateModification) {
        this.dateModification = dateModification;
    }

    @Override
    public String toString() {
        return prenom + " " + nom + " (" + profil + ")";
    }
}