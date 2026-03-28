package com.example.SenBibliotheque.controller.modules;

import com.example.SenBibliotheque.entity.Livre;
import com.example.SenBibliotheque.entity.Categorie;
import com.example.SenBibliotheque.service.LivreService;
import com.example.SenBibliotheque.service.CategorieService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class BooksModuleConsultationController {

    private static final Logger logger = LoggerFactory.getLogger(BooksModuleConsultationController.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML
    private TableView<Livre> booksTable;
    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> filterCategory;
    @FXML
    private Label statusLabel;

    private LivreService livreService;
    private CategorieService categorieService;
    private ObservableList<Livre> livres;
    private List<Categorie> categories;

    @FXML
    public void initialize() {
        try {
            livreService = new LivreService();
            categorieService = new CategorieService();
            livres = FXCollections.observableArrayList();

            // Configurer la table
            configurerTable();

            // Charger les catégories
            chargerCategories();

            // Charger les livres
            chargerLivres();
        } catch (Exception e) {
            logger.error("❌ Erreur lors de l'initialisation du module livres", e);
        }
    }

    private void configurerTable() {
        // Colonne ISBN
        TableColumn<Livre, String> isbnCol = new TableColumn<>("ISBN");
        isbnCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getIsbn()));
        isbnCol.setPrefWidth(100);

        // Colonne Titre
        TableColumn<Livre, String> titreCol = new TableColumn<>("Titre");
        titreCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getTitre()));
        titreCol.setPrefWidth(150);

        // Colonne Auteur
        TableColumn<Livre, String> auteurCol = new TableColumn<>("Auteur");
        auteurCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getAuteur()));
        auteurCol.setPrefWidth(120);

        // Colonne Catégorie
        TableColumn<Livre, String> categorieCol = new TableColumn<>("Catégorie");
        categorieCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getCategorie() != null ? cellData.getValue().getCategorie().getLibelle() : "N/A"));
        categorieCol.setPrefWidth(100);

        // Colonne Année
        TableColumn<Livre, String> anneeCol = new TableColumn<>("Année");
        anneeCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getAnneePublication() != null ? cellData.getValue().getAnneePublication().toString() : "N/A"));
        anneeCol.setPrefWidth(80);

        // Colonne Total
        TableColumn<Livre, Integer> totalCol = new TableColumn<>("Total");
        totalCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getNombreExemplaires()));
        totalCol.setPrefWidth(60);

        // Colonne Disponible
        TableColumn<Livre, String> dispoCol = new TableColumn<>("Disponible");
        dispoCol.setCellValueFactory(cellData -> {
            int dispo = cellData.getValue().getDisponible();
            String status = dispo > 0 ? "✅ " + dispo : "❌ 0";
            return new javafx.beans.property.SimpleStringProperty(status);
        });
        dispoCol.setPrefWidth(100);

        // Colonne Détails (lecture seule)
        TableColumn<Livre, Void> detailsCol = new TableColumn<>("Action");
        detailsCol.setPrefWidth(80);
        detailsCol.setCellFactory(param -> new DetailsCell());

        booksTable.getColumns().addAll(isbnCol, titreCol, auteurCol, categorieCol, anneeCol, totalCol, dispoCol, detailsCol);
        booksTable.setItems(livres);
    }

    private void chargerCategories() {
        try {
            categories = categorieService.obtenirTous();
            ObservableList<String> categoryNames = FXCollections.observableArrayList("Toutes les catégories");
            categories.forEach(cat -> categoryNames.add(cat.getLibelle()));
            filterCategory.setItems(categoryNames);
            filterCategory.getSelectionModel().selectFirst();

            filterCategory.setOnAction(e -> filtrerParCategorie());
        } catch (Exception e) {
            logger.error("❌ Erreur lors du chargement des catégories", e);
        }
    }

    private void chargerLivres() {
        try {
            List<Livre> list = livreService.obtenirTous();
            livres.clear();
            livres.addAll(list);
            statusLabel.setText(list.size() + " livre(s)");
            logger.info("✅ Livres chargés: " + list.size());
        } catch (Exception e) {
            logger.error("❌ Erreur lors du chargement des livres", e);
        }
    }

    @FXML
    private void rechercher() {
        String terme = searchField.getText().trim();

        if (terme.isEmpty()) {
            chargerLivres();
            return;
        }

        try {
            List<Livre> resultats = livreService.obtenirTous().stream()
                    .filter(l -> l.getTitre().toLowerCase().contains(terme.toLowerCase()) ||
                            l.getAuteur().toLowerCase().contains(terme.toLowerCase()) ||
                            l.getIsbn().toLowerCase().contains(terme.toLowerCase()))
                    .toList();

            livres.clear();
            livres.addAll(resultats);
            statusLabel.setText(resultats.size() + " livre(s) trouvé(s)");

            logger.info("✅ Recherche effectuée: " + resultats.size() + " résultats");
        } catch (Exception e) {
            logger.error("❌ Erreur lors de la recherche", e);
        }
    }

    private void filtrerParCategorie() {
        String categorie = filterCategory.getSelectionModel().getSelectedItem();

        if (categorie == null || "Toutes les catégories".equals(categorie)) {
            chargerLivres();
            return;
        }

        try {
            Categorie cat = categories.stream()
                    .filter(c -> c.getLibelle().equals(categorie))
                    .findFirst()
                    .orElse(null);

            if (cat != null) {
                List<Livre> resultats = livreService.obtenirParCategorie(cat.getId());
                livres.clear();
                livres.addAll(resultats);
                statusLabel.setText(resultats.size() + " livre(s)");
            }
        } catch (Exception e) {
            logger.error("❌ Erreur lors du filtrage par catégorie", e);
        }
    }

    private class DetailsCell extends TableCell<Livre, Void> {
        @Override
        protected void updateItem(Void item, boolean empty) {
            super.updateItem(item, empty);

            if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                setGraphic(null);
                return;
            }

            Livre livre = getTableRow().getItem();

            Button detailsBtn = new Button("👁️");
            detailsBtn.setStyle(
                    "-fx-font-size: 14; " +
                            "-fx-background-color: #06b6d4; " +
                            "-fx-text-fill: white; " +
                            "-fx-padding: 8 10; " +
                            "-fx-border-radius: 4; " +
                            "-fx-background-radius: 4; " +
                            "-fx-cursor: hand;"
            );

            Tooltip tip = new Tooltip("Voir les détails");
            tip.setStyle("-fx-font-size: 11; -fx-text-fill: #ffffff; -fx-background-color: #1f2937; -fx-padding: 8;");
            Tooltip.install(detailsBtn, tip);

            detailsBtn.setOnAction(e -> afficherDetails(livre));

            setGraphic(detailsBtn);
        }
    }

    private void afficherDetails(Livre livre) {
        StringBuilder details = new StringBuilder();
        details.append("📚 ").append(livre.getTitre()).append("\n\n");
        details.append("Auteur: ").append(livre.getAuteur()).append("\n");
        details.append("ISBN: ").append(livre.getIsbn()).append("\n");
        details.append("Année: ").append(livre.getAnneePublication()).append("\n");
        details.append("Catégorie: ").append(livre.getCategorie() != null ? livre.getCategorie().getLibelle() : "N/A").append("\n");
        details.append("Exemplaires totaux: ").append(livre.getNombreExemplaires()).append("\n");
        details.append("Exemplaires disponibles: ").append(livre.getDisponible()).append("\n");
        details.append("Exemplaires empruntés: ").append(livre.getNombreExemplaires() - livre.getDisponible()).append("\n");

        if (livre.getDescription() != null && !livre.getDescription().isEmpty()) {
            details.append("\nDescription:\n").append(livre.getDescription());
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Détails du livre");
        alert.setHeaderText(null);
        alert.setContentText(details.toString());
        alert.showAndWait();
    }
}