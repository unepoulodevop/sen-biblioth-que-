package com.example.SenBibliotheque.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "emprunts", indexes = {
        @Index(name = "idx_adherent", columnList = "adherent_id"),
        @Index(name = "idx_livre", columnList = "livre_id"),
        @Index(name = "idx_date_emprunt", columnList = "date_emprunt")
})
public class Emprunt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "livre_id", nullable = false)
    private Livre livre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "adherent_id", nullable = false)
    private Adherent adherent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id")
    private Utilisateur utilisateur;

    @Column(name = "date_emprunt")
    private LocalDateTime dateEmprunt;

    @Column(name = "date_retour_prevue")
    private LocalDateTime dateRetourPrevue;

    @Column(name = "date_retour_effective")
    private LocalDateTime dateRetourEffective;

    @Column(name = "penalite")
    private Double penalite = 0.0;

    @Column(name = "statut")
    private String statut = "EN_COURS"; // EN_COURS, RETOURNE, EN_RETARD

    // Constructeurs
    public Emprunt() {
    }

    public Emprunt(Livre livre, Adherent adherent, Utilisateur utilisateur, LocalDateTime dateRetourPrevue) {
        this.livre = livre;
        this.adherent = adherent;
        this.utilisateur = utilisateur;
        this.dateEmprunt = LocalDateTime.now();
        this.dateRetourPrevue = dateRetourPrevue;
        this.penalite = 0.0;
        this.statut = "EN_COURS";
    }

    // Méthodes métier
    public void calculerPenalite() {
        if (dateRetourEffective != null && dateRetourEffective.isAfter(dateRetourPrevue)) {
            long joursRetard = ChronoUnit.DAYS.between(dateRetourPrevue, dateRetourEffective);
            penalite = joursRetard * 1000.0; // 1000 FCFA par jour
            statut = "RETOURNE_AVEC_RETARD";
        } else if (dateRetourEffective != null) {
            penalite = 0.0;
            statut = "RETOURNE";
        } else if (LocalDateTime.now().isAfter(dateRetourPrevue)) {
            // En retard mais pas encore retourné
            long joursRetard = ChronoUnit.DAYS.between(dateRetourPrevue, LocalDateTime.now());
            penalite = joursRetard * 1000.0;
            statut = "EN_RETARD";
        } else {
            penalite = 0.0;
            statut = "EN_COURS";
        }
    }

    public long getJoursRetard() {
        LocalDateTime dateComparaison = dateRetourEffective != null ? dateRetourEffective : LocalDateTime.now();
        if (dateComparaison.isAfter(dateRetourPrevue)) {
            return ChronoUnit.DAYS.between(dateRetourPrevue, dateComparaison);
        }
        return 0;
    }

    public boolean isEnRetard() {
        return LocalDateTime.now().isAfter(dateRetourPrevue) && dateRetourEffective == null;
    }
    // Getters & Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Livre getLivre() {
        return livre;
    }

    public void setLivre(Livre livre) {
        this.livre = livre;
    }

    public Adherent getAdherent() {
        return adherent;
    }

    public void setAdherent(Adherent adherent) {
        this.adherent = adherent;
    }

    public Utilisateur getUtilisateur() {
        return utilisateur;
    }

    public void setUtilisateur(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
    }

    public LocalDateTime getDateEmprunt() {
        return dateEmprunt;
    }

    public void setDateEmprunt(LocalDateTime dateEmprunt) {
        this.dateEmprunt = dateEmprunt;
    }

    public LocalDateTime getDateRetourPrevue() {
        return dateRetourPrevue;
    }

    public void setDateRetourPrevue(LocalDateTime dateRetourPrevue) {
        this.dateRetourPrevue = dateRetourPrevue;
    }

    public LocalDateTime getDateRetourEffective() {
        return dateRetourEffective;
    }

    public void setDateRetourEffective(LocalDateTime dateRetourEffective) {
        this.dateRetourEffective = dateRetourEffective;
    }

    public Double getPenalite() {
        return penalite;
    }

    public void setPenalite(Double penalite) {
        this.penalite = penalite;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    @Override
    public String toString() {
        return livre.getTitre() + " - " + adherent.getPrenom() + " " + adherent.getNom();
    }
}