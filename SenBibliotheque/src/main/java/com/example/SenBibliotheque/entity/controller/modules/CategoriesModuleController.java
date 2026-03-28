package com.example.SenBibliotheque.controller.modules;

import com.example.SenBibliotheque.entity.Categorie;
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

public class CategoriesModuleController {

    private static final Logger logger = LoggerFactory.getLogger(CategoriesModuleController.class);

    @FXML
    private TableView<Categorie> categoriesTable;
    @FXML
    private TextField searchField;
    @FXML
    private Label statusLabel;
    @FXML
    private Button ajouterBtn;

    private CategorieService categorieService;
    private ObservableList<Categorie> categories;

    @FXML
    public void initialize() {
        try {
            categorieService = new CategorieService();
            categories = FXCollections.observableArrayList();

            // Configurer la table
            configurerTable();

            // Charger les catégories
            chargerCategories();
        } catch (Exception e) {
            logger.error("❌ Erreur lors de l'initialisation du module catégories", e);
        }
    }

    private void configurerTable() {
        // Colonne Libellé
        TableColumn<Categorie, String> libelleCol = new TableColumn<>("Libellé");
        libelleCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getLibelle()));
        libelleCol.setPrefWidth(150);

        // Colonne Description
        TableColumn<Categorie, String> descriptionCol = new TableColumn<>("Description");
        descriptionCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getDescription() != null ? cellData.getValue().getDescription() : "N/A"));
        descriptionCol.setPrefWidth(300);

        // Colonne Livres
        TableColumn<Categorie, Integer> booksCol = new TableColumn<>("Livres");
        booksCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(
                cellData.getValue().getLivres() != null ? cellData.getValue().getLivres().size() : 0));
        booksCol.setPrefWidth(80);

        // Colonne Actions
        TableColumn<Categorie, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setPrefWidth(180);
        actionsCol.setCellFactory(param -> new ActionCell());

        categoriesTable.getColumns().addAll(libelleCol, descriptionCol, booksCol, actionsCol);
        categoriesTable.setItems(categories);
    }

    private void chargerCategories() {
        try {
            List<Categorie> list = categorieService.obtenirTous();
            categories.clear();
            categories.addAll(list);
            statusLabel.setText(list.size() + " catégorie(s)");
            logger.info("✅ Catégories chargées: " + list.size());
        } catch (Exception e) {
            logger.error("❌ Erreur lors du chargement des catégories", e);
        }
    }

    @FXML
    private void rechercher() {
        String terme = searchField.getText().trim();

        if (terme.isEmpty()) {
            chargerCategories();
            return;
        }

        try {
            List<Categorie> resultats = categorieService.obtenirTous().stream()
                    .filter(c -> c.getLibelle().toLowerCase().contains(terme.toLowerCase()) ||
                            (c.getDescription() != null && c.getDescription().toLowerCase().contains(terme.toLowerCase())))
                    .toList();

            categories.clear();
            categories.addAll(resultats);
            statusLabel.setText(resultats.size() + " catégorie(s) trouvée(s)");

            logger.info("✅ Recherche effectuée: " + resultats.size() + " résultats");
        } catch (Exception e) {
            logger.error("❌ Erreur lors de la recherche", e);
            afficherErreur("Erreur lors de la recherche");
        }
    }

    @FXML
    private void ajouterCategorie() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dialogs/AddCategoryDialog.fxml"));
            ScrollPane scrollPane = loader.load();
            AddCategoryDialogController controller = loader.getController();

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.getDialogPane().setContent(scrollPane);
            dialog.setTitle("Ajouter une nouvelle catégorie");
            dialog.setWidth(800);
            dialog.setHeight(500);

            dialog.getDialogPane().getButtonTypes().addAll(
                    new ButtonType("✅ Ajouter", ButtonBar.ButtonData.OK_DONE),
                    new ButtonType("❌ Annuler", ButtonBar.ButtonData.CANCEL_CLOSE)
            );

            dialog.initOwner(categoriesTable.getScene().getWindow());

            Optional<ButtonType> result = dialog.showAndWait();

            if (result.isPresent() && result.get().getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                if (!controller.validateForm()) {
                    return;
                }

                try {
                    categorieService.creer(controller.getLibelle(), controller.getDescription());

                    logger.info("✅ Catégorie créée: " + controller.getLibelle());
                    afficherSucces("Catégorie ajoutée avec succès!",
                            "Libellé: " + controller.getLibelle());

                    chargerCategories();
                } catch (RuntimeException e) {
                    afficherErreur("Erreur: " + e.getMessage());
                }
            }

        } catch (IOException e) {
            logger.error("❌ Erreur lors du chargement du dialogue", e);
            afficherErreur("Erreur lors de l'ouverture du formulaire");
        } catch (Exception e) {
            logger.error("❌ Erreur lors de l'ajout de la catégorie", e);
            afficherErreur("Erreur: " + e.getMessage());
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
    private class ActionCell extends TableCell<Categorie, Void> {
        @Override
        protected void updateItem(Void item, boolean empty) {
            super.updateItem(item, empty);

            if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                setGraphic(null);
                return;
            }

            Categorie categorie = getTableRow().getItem();

            // Bouton Modifier
            Button modifierBtn = createActionButton("✏️ Modifier", "#3b82f6", "Modifier cette catégorie");
            modifierBtn.setOnAction(e -> modifierCategorie(categorie));

            // Bouton Supprimer
            Button supprimerBtn = createActionButton("🗑️ Supprimer", "#dc2626", "Supprimer cette catégorie");
            supprimerBtn.setOnAction(e -> supprimerCategorie(categorie));

            HBox hBox = new HBox(5);
            hBox.setStyle("-fx-alignment: CENTER_LEFT;");
            hBox.getChildren().addAll(modifierBtn, supprimerBtn);
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

            Tooltip tip = new Tooltip(tooltip);
            tip.setStyle("-fx-font-size: 10; -fx-text-fill: #ffffff; -fx-background-color: #1f2937; -fx-padding: 8;");
            Tooltip.install(btn, tip);

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
                case "#dc2626" -> "#ef4444";
                default -> color;
            };
        }
    }

    // ===== ACTIONS SUR LES CATÉGORIES =====
    private void modifierCategorie(Categorie categorie) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dialogs/AddCategoryDialog.fxml"));
            ScrollPane scrollPane = loader.load();
            AddCategoryDialogController controller = loader.getController();
            controller.setCategorie(categorie);

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.getDialogPane().setContent(scrollPane);
            dialog.setTitle("Modifier la catégorie");
            dialog.setWidth(720);
            dialog.setHeight(380);

            dialog.getDialogPane().getButtonTypes().addAll(
                    new ButtonType("✅ Enregistrer", ButtonBar.ButtonData.OK_DONE),
                    new ButtonType("❌ Annuler", ButtonBar.ButtonData.CANCEL_CLOSE)
            );

            dialog.initOwner(categoriesTable.getScene().getWindow());

            Optional<ButtonType> result = dialog.showAndWait();

            if (result.isPresent() && result.get().getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                if (!controller.validateForm()) {
                    return;
                }

                categorie.setLibelle(controller.getLibelle());
                categorie.setDescription(controller.getDescription());

                categorieService.update(categorie);

                logger.info("✅ Catégorie modifiée: " + categorie.getLibelle());
                afficherSucces("Succès", "Catégorie modifiée avec succès!");

                chargerCategories();
            }

        } catch (IOException e) {
            logger.error("❌ Erreur lors du chargement du dialogue", e);
            afficherErreur("Erreur lors de l'ouverture du formulaire");
        } catch (Exception e) {
            logger.error("❌ Erreur lors de la modification de la catégorie", e);
            afficherErreur("Erreur: " + e.getMessage());
        }
    }

    private void supprimerCategorie(Categorie categorie) {
        int nbLivres = categorie.getLivres() != null ? categorie.getLivres().size() : 0;

        if (nbLivres > 0) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Impossible de supprimer");
            alert.setHeaderText("Cette catégorie contient des livres");
            alert.setContentText("Vous ne pouvez pas supprimer une catégorie qui contient " +
                    nbLivres + " livre(s).\n\n" +
                    "Veuillez d'abord modifier ces livres pour changer leur catégorie.");
            alert.showAndWait();
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation de suppression");
        confirmation.setHeaderText("Supprimer la catégorie?");
        confirmation.setContentText("Êtes-vous sûr de vouloir supprimer:\n\n" +
                categorie.getLibelle() + "\n\n" +
                "⚠️ Cette action est irréversible!");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                categorieService.supprimer(categorie.getId());
                chargerCategories();
                logger.info("✅ Catégorie supprimée: " + categorie.getLibelle());
                afficherSucces("Succès", "Catégorie supprimée avec succès!");
            } catch (Exception e) {
                logger.error("❌ Erreur lors de la suppression", e);
                afficherErreur("Erreur: " + e.getMessage());
            }
        }
    }
}