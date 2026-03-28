package com.example.SenBibliotheque.entity.controller.modules;

import com.example.SenBibliotheque.entity.Livre;
import com.example.SenBibliotheque.entity.Categorie;
import com.example.SenBibliotheque.service.LivreService;
import com.example.SenBibliotheque.service.CategorieService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class BooksModuleController {

    private static final Logger logger = LoggerFactory.getLogger(BooksModuleController.class);

    @FXML
    private TableView<Livre> booksTable;
    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> filterCategory;
    @FXML
    private Label statusLabel;
    @FXML
    private Button ajouterBtn;

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

        // Colonne Total exemplaires
        TableColumn<Livre, Integer> totalCol = new TableColumn<>("Total");
        totalCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getNombreExemplaires()));
        totalCol.setPrefWidth(60);

        // Colonne Exemplaires disponibles
        TableColumn<Livre, String> dispoCol = new TableColumn<>("Disponible");
        dispoCol.setCellValueFactory(cellData -> {
            int dispo = cellData.getValue().getDisponible();
            String status = dispo > 0 ? "✅ " + dispo : "❌ 0";
            return new javafx.beans.property.SimpleStringProperty(status);
        });
        dispoCol.setPrefWidth(100);

        // Colonne Actions
        TableColumn<Livre, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setPrefWidth(200);
        actionsCol.setCellFactory(param -> new ActionCell());

        booksTable.getColumns().addAll(isbnCol, titreCol, auteurCol, categorieCol, anneeCol, totalCol, dispoCol, actionsCol);
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
    private void ajouterLivre() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dialogs/AddBookDialog.fxml"));
            ScrollPane scrollPane = loader.load();
            AddBookDialogController controller = loader.getController();
            controller.setCategories(categories);

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.getDialogPane().setContent(scrollPane);
            dialog.setTitle("Ajouter un nouveau livre");
            dialog.setWidth(800);
            dialog.setHeight(620);

            dialog.getDialogPane().getButtonTypes().addAll(
                    new ButtonType("✅ Ajouter", ButtonBar.ButtonData.OK_DONE),
                    new ButtonType("❌ Annuler", ButtonBar.ButtonData.CANCEL_CLOSE)
            );

            dialog.initOwner(booksTable.getScene().getWindow());

            Optional<ButtonType> result = dialog.showAndWait();

            if (result.isPresent() && result.get().getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                if (!controller.validateForm()) {
                    return;
                }

                Livre nouveauLivre = new Livre(
                        controller.getIsbn(),
                        controller.getTitre(),
                        controller.getAuteur(),
                        controller.getCategorie(),
                        controller.getAnnee()
                );
                nouveauLivre.setNombreExemplaires(controller.getNombreExemplaires());
                nouveauLivre.setDisponible(controller.getDisponible());
                nouveauLivre.setDescription(controller.getDescription());

                livreService.creer(nouveauLivre);

                logger.info("✅ Livre créé: " + controller.getTitre());
                afficherSucces("Livre ajouté avec succès!",
                        "Titre: " + controller.getTitre() + "\n" +
                                "Auteur: " + controller.getAuteur() + "\n" +
                                "ISBN: " + controller.getIsbn());

                chargerLivres();
            }

        } catch (IOException e) {
            logger.error("❌ Erreur lors du chargement du dialogue", e);
            afficherErreur("Erreur lors de l'ouverture du formulaire");
        } catch (Exception e) {
            logger.error("❌ Erreur lors de l'ajout du livre", e);
            afficherErreur("Erreur: " + e.getMessage());
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
            afficherErreur("Erreur lors de la recherche");
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
                logger.info("✅ Filtrage par catégorie: " + resultats.size() + " résultats");
            }
        } catch (Exception e) {
            logger.error("❌ Erreur lors du filtrage par catégorie", e);
            afficherErreur("Erreur lors du filtrage");
        }
    }

    private void afficherSucces(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void afficherErreur(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // ===== CLASSE INTERNE POUR LES ACTIONS =====
    private class ActionCell extends TableCell<Livre, Void> {
        @Override
        protected void updateItem(Void item, boolean empty) {
            super.updateItem(item, empty);

            if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                setGraphic(null);
                return;
            }

            Livre livre = getTableRow().getItem();

            // Bouton Modifier
            Button modifierBtn = createActionButton("✏️ Modifier", "#3b82f6", "Modifier ce livre");
            modifierBtn.setOnAction(e -> modifierLivre(livre));

            // Bouton Détails
            Button detailsBtn = createActionButton("👁️ Détails", "#06b6d4", "Voir les détails");
            detailsBtn.setOnAction(e -> afficherDetails(livre));

            // Bouton Supprimer
            Button supprimerBtn = createActionButton("🗑️ Supprimer", "#dc2626", "Supprimer ce livre");
            supprimerBtn.setOnAction(e -> supprimerLivre(livre));

            HBox hBox = new HBox(5);
            hBox.setStyle("-fx-alignment: CENTER_LEFT;");
            hBox.getChildren().addAll(modifierBtn, detailsBtn, supprimerBtn);
            setGraphic(hBox);
        }

        private Button createActionButton(String text, String color, String tooltip) {
            Button btn = new Button(text);
            btn.setStyle(
                    "-fx-font-size: 10; " +
                            "-fx-background-color: " + color + "; " +
                            "-fx-text-fill: white; " +
                            "-fx-padding: 6 12; " +
                            "-fx-border-radius: 5; " +
                            "-fx-background-radius: 5; " +
                            "-fx-cursor: hand; " +
                            "-fx-font-weight: bold; " +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 3, 0, 0, 1);"
            );

            // Ajouter le tooltip
            Tooltip tip = new Tooltip(tooltip);
            tip.setStyle("-fx-font-size: 10; -fx-text-fill: #ffffff; -fx-background-color: #1f2937; -fx-padding: 8;");
            Tooltip.install(btn, tip);

            // Effet au survol
            btn.setOnMouseEntered(e -> btn.setStyle(
                    "-fx-font-size: 10; " +
                            "-fx-background-color: " + lightenColor(color) + "; " +
                            "-fx-text-fill: white; " +
                            "-fx-padding: 6 12; " +
                            "-fx-border-radius: 5; " +
                            "-fx-background-radius: 5; " +
                            "-fx-cursor: hand; " +
                            "-fx-font-weight: bold; " +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 5, 0, 0, 2);"
            ));

            btn.setOnMouseExited(e -> btn.setStyle(
                    "-fx-font-size: 10; " +
                            "-fx-background-color: " + color + "; " +
                            "-fx-text-fill: white; " +
                            "-fx-padding: 6 12; " +
                            "-fx-border-radius: 5; " +
                            "-fx-background-radius: 5; " +
                            "-fx-cursor: hand; " +
                            "-fx-font-weight: bold; " +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 3, 0, 0, 1);"
            ));

            return btn;
        }

        private String lightenColor(String color) {
            return switch (color) {
                case "#3b82f6" -> "#60a5fa";
                case "#06b6d4" -> "#22d3ee";
                case "#dc2626" -> "#ef4444";
                default -> color;
            };
        }
    }

    // ===== ACTIONS SUR LES LIVRES =====
    private void modifierLivre(Livre livre) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dialogs/AddBookDialog.fxml"));
            ScrollPane scrollPane = loader.load();
            AddBookDialogController controller = loader.getController();
            controller.setCategories(categories);
            controller.setLivre(livre);

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.getDialogPane().setContent(scrollPane);
            dialog.setTitle("Modifier le livre");
            dialog.setWidth(720);
            dialog.setHeight(400);

            dialog.getDialogPane().getButtonTypes().addAll(
                    new ButtonType("✅ Enregistrer", ButtonBar.ButtonData.OK_DONE),
                    new ButtonType("❌ Annuler", ButtonBar.ButtonData.CANCEL_CLOSE)
            );

            dialog.initOwner(booksTable.getScene().getWindow());

            Optional<ButtonType> result = dialog.showAndWait();

            if (result.isPresent() && result.get().getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                if (!controller.validateForm()) {
                    return;
                }

                livre.setTitre(controller.getTitre());
                livre.setAuteur(controller.getAuteur());
                livre.setCategorie(controller.getCategorie());
                livre.setAnneePublication(controller.getAnnee());
                livre.setNombreExemplaires(controller.getNombreExemplaires());
                livre.setDisponible(controller.getDisponible());
                livre.setDescription(controller.getDescription());

                livreService.update(livre);

                logger.info("✅ Livre modifié: " + livre.getTitre());
                afficherSucces("Succès", "Livre modifié avec succès!");

                chargerLivres();
            }

        } catch (IOException e) {
            logger.error("❌ Erreur lors du chargement du dialogue", e);
            afficherErreur("Erreur lors de l'ouverture du formulaire");
        } catch (Exception e) {
            logger.error("❌ Erreur lors de la modification du livre", e);
            afficherErreur("Erreur: " + e.getMessage());
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

    private void supprimerLivre(Livre livre) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation de suppression");
        confirmation.setHeaderText("Supprimer le livre?");
        confirmation.setContentText("Êtes-vous sûr de vouloir supprimer:\n\n" +
                livre.getTitre() + " - " + livre.getAuteur() + "\n" +
                "ISBN: " + livre.getIsbn() + "\n\n" +
                "⚠️ Cette action est irréversible!");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                livreService.supprimer(livre.getId());
                chargerLivres();
                logger.info("✅ Livre supprimé: " + livre.getTitre());
                afficherSucces("Succès", "Livre supprimé avec succès!");
            } catch (Exception e) {
                logger.error("❌ Erreur lors de la suppression", e);
                afficherErreur("Erreur: " + e.getMessage());
            }
        }
    }
}