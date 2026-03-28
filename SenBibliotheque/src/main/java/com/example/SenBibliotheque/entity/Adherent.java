package com.example.SenBibliotheque.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "adherents", indexes = {
        @Index(name = "idx_matricule", columnList = "matricule", unique = true),
        @Index(name = "idx_email", columnList = "email")
})
public class Adherent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "matricule", nullable = false, unique = true, length = 50)
    private String matricule;

    @Column(name = "nom", nullable = false, length = 100)
    private String nom;

    @Column(name = "prenom", nullable = false, length = 100)
    private String prenom;

    @Column(name = "email", unique = true, length = 100)
    private String email;

    @Column(name = "telephone", length = 20)
    private String telephone;

    @Column(name = "adresse", length = 255)
    private String adresse;

    @Column(name = "date_inscription")
    private LocalDateTime dateInscription;

    @Column(name = "actif")
    private Boolean actif = true;

    @Column(name = "date_suspension")
    private LocalDateTime dateSuspension;

    @OneToMany(mappedBy = "adherent", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Emprunt> emprunts = new HashSet<>();

    // Constructeurs
    public Adherent() {
    }

    public Adherent(String matricule, String nom, String prenom, String email, String telephone) {
        this.matricule = matricule;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.telephone = telephone;
        this.dateInscription = LocalDateTime.now();
    }

    // Getters & Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getMatricule() {
        return matricule;
    }

    public void setMatricule(String matricule) {
        this.matricule = matricule;
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

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public LocalDateTime getDateInscription() {
        return dateInscription;
    }

    public void setDateInscription(LocalDateTime dateInscription) {
        this.dateInscription = dateInscription;
    }

    public Boolean getActif() {
        return actif;
    }

    public void setActif(Boolean actif) {
        this.actif = actif;
    }

    public LocalDateTime getDateSuspension() {
        return dateSuspension;
    }

    public void setDateSuspension(LocalDateTime dateSuspension) {
        this.dateSuspension = dateSuspension;
    }

    public Set<Emprunt> getEmprunts() {
        return emprunts;
    }

    public void setEmprunts(Set<Emprunt> emprunts) {
        this.emprunts = emprunts;
    }

    @Override
    public String toString() {
        return prenom + " " + nom;
    }
}