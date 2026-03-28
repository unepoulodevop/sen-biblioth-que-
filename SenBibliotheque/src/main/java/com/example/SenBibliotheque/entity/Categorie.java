package com.example.SenBibliotheque.entity;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "categories", indexes = {
        @Index(name = "idx_libelle", columnList = "libelle", unique = true)
})
public class Categorie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "libelle", nullable = false, unique = true, length = 100)
    private String libelle;

    @Column(name = "description", length = 500)
    private String description;

    @OneToMany(mappedBy = "categorie", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Livre> livres = new HashSet<>();

    // Constructeurs
    public Categorie() {
    }

    public Categorie(String libelle, String description) {
        this.libelle = libelle;
        this.description = description;
    }

    // Getters & Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<Livre> getLivres() {
        return livres;
    }

    public void setLivres(Set<Livre> livres) {
        this.livres = livres;
    }

    @Override
    public String toString() {
        return libelle;
    }
}