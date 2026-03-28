module com.SenBibliotheque {
    // JavaFX
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    // Hibernate & JPA
    requires transitive org.hibernate.orm.core;
    requires jakarta.persistence;
    requires jakarta.transaction;

    // MySQL
    requires java.sql;

    // Logging
    requires org.slf4j;
    requires ch.qos.logback.core;
    requires ch.qos.logback.classic;
    requires jbcrypt;
    requires kernel;
    requires layout;
    requires io;
    requires java.desktop;
    requires java.prefs;


    // ✅ IMPORTANT : Ouvrir le package entity à Hibernate pour la réflexion
    opens com.example.SenBibliotheque.entity to org.hibernate.orm.core;
    opens com.example.SenBibliotheque.config to org.hibernate.orm.core;
    opens com.example.SenBibliotheque to javafx.fxml;
    opens com.example.SenBibliotheque.entity.controller to javafx.fxml;
    opens com.example.SenBibliotheque.entity.controller.modules to javafx.fxml;

    // Exporter les packages publics
    exports com.example.SenBibliotheque;
    exports com.example.SenBibliotheque.entity;
    exports com.example.SenBibliotheque.service;
}