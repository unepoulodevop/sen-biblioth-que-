package com.example.SenBibliotheque.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "livres", indexes = {
        @Index(name = "idx_isbn", columnList = "isbn", unique = true),
        @Index(name = "idx_titre", columnList = "titre"),
        @Index(name = "idx_auteur", columnList = "auteur")
})
public class Livre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "isbn", nullable = false, unique = true, length = 20)
    private String isbn;

    @Column(name = "titre", nullable = false, length = 255)
    private String titre;

    @Column(name = "auteur", nullable = false, length = 150)
    private String auteur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categorie_id")
    private Categorie categorie;

    @Column(name = "annee_publication")
    private Integer anneePublication;

    @Column(name = "nombre_exemplaires", nullable = false)
    private Integer nombreExemplaires = 1;

    @Column(name = "disponible", nullable = false)
    private Integer disponible = 1;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "date_ajout")
    private LocalDateTime dateAjout;

    @Column(name = "date_modification")
    private LocalDateTime dateModification;

    @OneToMany(mappedBy = "livre", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Emprunt> emprunts = new HashSet<>();

    // Constructeurs
    public Livre() {
    }

    public Livre(String isbn, String titre, String auteur, Categorie categorie, Integer anneePublication) {
        this.isbn = isbn;
        this.titre = titre;
        this.auteur = auteur;
        this.categorie = categorie;
        this.anneePublication = anneePublication;
        this.dateAjout = LocalDateTime.now();
    }

    // Getters & Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getAuteur() {
        return auteur;
    }

    public void setAuteur(String auteur) {
        this.auteur = auteur;
    }

    public Categorie getCategorie() {
        return categorie;
    }

    public void setCategorie(Categorie categorie) {
        this.categorie = categorie;
    }

    public Integer getAnneePublication() {
        return anneePublication;
    }

    public void setAnneePublication(Integer anneePublication) {
        this.anneePublication = anneePublication;
    }

    public Integer getNombreExemplaires() {
        return nombreExemplaires;
    }

    public void setNombreExemplaires(Integer nombreExemplaires) {
        this.nombreExemplaires = nombreExemplaires;
    }

    public Integer getDisponible() {
        return disponible;
    }

    public void setDisponible(Integer disponible) {
        this.disponible = disponible;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getDateAjout() {
        return dateAjout;
    }

    public void setDateAjout(LocalDateTime dateAjout) {
        this.dateAjout = dateAjout;
    }

    public LocalDateTime getDateModification() {
        return dateModification;
    }

    public void setDateModification(LocalDateTime dateModification) {
        this.dateModification = dateModification;
    }

    public Set<Emprunt> getEmprunts() {
        return emprunts;
    }

    public void setEmprunts(Set<Emprunt> emprunts) {
        this.emprunts = emprunts;
    }

    @Override
    public String toString() {
        return titre + " - " + auteur;
    }
}