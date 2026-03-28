package com.example.SenBibliotheque.controller.modules;

import com.example.SenBibliotheque.entity.Categorie;
import com.example.SenBibliotheque.entity.Emprunt;
import com.example.SenBibliotheque.service.AdherentService;
import com.example.SenBibliotheque.service.LivreService;
import com.example.SenBibliotheque.service.EmpruntService;
import com.example.SenBibliotheque.service.CategorieService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class DashboardModuleController {

    private static final Logger logger = LoggerFactory.getLogger(DashboardModuleController.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter DATE_ONLY = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ===== LABELS =====
    @FXML
    private Label totalBooksLabel, booksDetailsLabel;
    @FXML
    private Label totalMembersLabel, membersDetailsLabel;
    @FXML
    private Label activeBorrowsLabel, borrowsDetailsLabel;
    @FXML
    private Label overdueLabel, overdueDetailsLabel;

    // ===== GRAPHIQUES =====
    @FXML
    private LineChart<String, Number> borrowsLineChart;
    @FXML
    private PieChart categoriesPieChart;
    @FXML
    private BarChart<String, Number> topBooksBarChart;
    @FXML
    private PieChart availabilityPieChart;

    // ===== TABLEAU =====
    @FXML
    private TableView<Emprunt> overdueTable;
    @FXML
    private Label overdueTableStatus;

    // ===== SERVICES =====
    private LivreService livreService;
    private AdherentService adherentService;
    private EmpruntService empruntService;
    private CategorieService categorieService;

    private ObservableList<Emprunt> overdue;

    @FXML
    public void initialize() {
        try {
            livreService = new LivreService();
            adherentService = new AdherentService();
            empruntService = new EmpruntService();
            categorieService = new CategorieService();

            overdue = FXCollections.observableArrayList();

            // Configurer le tableau
            configurerTableauRetards();

            // Charger les données
            chargerStatistiques();
            chargerGraphiques();

            logger.info("✅ DashboardModuleController initialisé");
        } catch (Exception e) {
            logger.error("❌ Erreur lors de l'initialisation du dashboard", e);
        }
    }

    // ===== CONFIGURATION DU TABLEAU =====
    private void configurerTableauRetards() {
        // Colonne Adhérent
        TableColumn<Emprunt, String> adherentCol = new TableColumn<>("Adhérent");
        adherentCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getAdherent().getPrenom() + " " + cellData.getValue().getAdherent().getNom()));
        adherentCol.setPrefWidth(120);

        // Colonne Livre
        TableColumn<Emprunt, String> livreCol = new TableColumn<>("Livre");
        livreCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getLivre().getTitre()));
        livreCol.setPrefWidth(150);

        // Colonne Retour prévu
        TableColumn<Emprunt, String> dateRetourCol = new TableColumn<>("Retour prévu");
        dateRetourCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getDateRetourPrevue().format(DATE_ONLY)));
        dateRetourCol.setPrefWidth(120);

        // Colonne Jours de retard
        TableColumn<Emprunt, String> jourRetardCol = new TableColumn<>("Retard (j)");
        jourRetardCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(String.valueOf(cellData.getValue().getJoursRetard())));
        jourRetardCol.setPrefWidth(80);

        // Colonne Pénalité
        TableColumn<Emprunt, String> penaliteCol = new TableColumn<>("Pénalité (FCFA)");
        penaliteCol.setCellValueFactory(cellData -> {
            cellData.getValue().calculerPenalite();
            return new javafx.beans.property.SimpleStringProperty(
                    String.format("%.2f", cellData.getValue().getPenalite()));
        });
        penaliteCol.setPrefWidth(120);

        overdueTable.getColumns().clear();
        overdueTable.getColumns().addAll(adherentCol, livreCol, dateRetourCol, jourRetardCol, penaliteCol);
        overdueTable.setItems(overdue);
    }

    // ===== CHARGEMENT DES STATISTIQUES =====
    private void chargerStatistiques() {
        try {
            // Livres
            long totalLivres = livreService.compterTous();
            long livresDisponibles = livreService.compterDisponibles();

            totalBooksLabel.setText(String.valueOf(totalLivres));
            booksDetailsLabel.setText(livresDisponibles + " disponible(s)");

            // Adhérents
            long totalAdherents = adherentService.compterTous();
            long adherentsActifs = adherentService.compterActifs();

            totalMembersLabel.setText(String.valueOf(totalAdherents));
            membersDetailsLabel.setText(adherentsActifs + " actif(s)");

            // Emprunts
            List<Emprunt> empruntsEnCours = empruntService.obtenirEnCours();
            activeBorrowsLabel.setText(String.valueOf(empruntsEnCours.size()));

            // Retards
            List<Emprunt> empruntsEnRetard = empruntService.obtenirEnRetard();
            overdue.clear();
            overdue.addAll(empruntsEnRetard);
            overdueTableStatus.setText(empruntsEnRetard.size() + " retard(s)");

            overdueLabel.setText(String.valueOf(empruntsEnRetard.size()));
            if (empruntsEnRetard.isEmpty()) {
                overdueDetailsLabel.setText("✅ Aucun retard");
                overdueDetailsLabel.setStyle("-fx-text-fill: #10b981;");
            } else {
                overdueDetailsLabel.setText("⚠️ À traiter");
                overdueDetailsLabel.setStyle("-fx-text-fill: #ef5350;");
            }

            logger.info("✅ Statistiques chargées");

        } catch (Exception e) {
            logger.error("❌ Erreur lors du chargement des statistiques", e);
        }
    }

    // ===== CHARGEMENT DES GRAPHIQUES =====
    private void chargerGraphiques() {
        try {
            chargerGraphiqueEmpruntsParMois();
            chargerGraphiqueDistributionCategories();
            chargerGraphiqueTopLivres();
            chargerGraphiqueDisponibilite();

            logger.info("✅ Graphiques chargés");
        } catch (Exception e) {
            logger.error("❌ Erreur lors du chargement des graphiques", e);
        }
    }

    // ===== GRAPHIQUE 1 : Emprunts par mois =====
    private void chargerGraphiqueEmpruntsParMois() {
        try {
            List<Emprunt> tousLesEmprunts = empruntService.obtenirTous();

            // Grouper par mois (12 derniers mois)
            Map<YearMonth, Long> empruntsByMonth = tousLesEmprunts.stream()
                    .collect(Collectors.groupingBy(
                            e -> YearMonth.from(e.getDateEmprunt()),
                            Collectors.counting()
                    ));

            // Créer la série de données
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Emprunts");

            // Ajouter les 12 derniers mois
            YearMonth now = YearMonth.now();
            for (int i = 11; i >= 0; i--) {
                YearMonth month = now.minusMonths(i);
                String label = month.getMonth().toString().substring(0, 3) + " " + month.getYear();
                long count = empruntsByMonth.getOrDefault(month, 0L);
                series.getData().add(new XYChart.Data<>(label, count));
            }

            borrowsLineChart.getData().clear();
            borrowsLineChart.getData().add(series);

            logger.info("✅ Graphique emprunts par mois généré");
        } catch (Exception e) {
            logger.error("❌ Erreur lors du chargement du graphique emprunts", e);
        }
    }

    // ===== GRAPHIQUE 2 : Distribution par catégorie =====
    private void chargerGraphiqueDistributionCategories() {
        try {
            List<Categorie> categories = categorieService.obtenirTous();
            ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();

            for (Categorie cat : categories) {
                long count = cat.getLivres().size();
                if (count > 0) {
                    pieData.add(new PieChart.Data(cat.getLibelle(), count));
                }
            }

            categoriesPieChart.setData(pieData);
            categoriesPieChart.setStyle("-fx-pie-label-visible: true;");

            logger.info("✅ Graphique distribution catégories généré");
        } catch (Exception e) {
            logger.error("❌ Erreur lors du chargement du graphique catégories", e);
        }
    }

    // ===== GRAPHIQUE 3 : Top 5 livres empruntés =====
    // ===== GRAPHIQUE 3 : Top 5 livres empruntés =====
    private void chargerGraphiqueTopLivres() {
        try {
            List<Emprunt> tousLesEmprunts = empruntService.obtenirTous();

            // Grouper par livre et compter
            Map<String, Long> empruntsByBook = tousLesEmprunts.stream()
                    .collect(Collectors.groupingBy(
                            e -> e.getLivre().getTitre(),
                            Collectors.counting()
                    ));

            // Trier et limiter aux 5 premiers
            List<Map.Entry<String, Long>> topLivres = empruntsByBook.entrySet().stream()
                    .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                    .limit(5)
                    .collect(Collectors.toList());

            // Créer la série de données
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Emprunts");

            for (Map.Entry<String, Long> entry : topLivres) {
                String titre = entry.getKey();
                Long count = entry.getValue();
                String shortTitle = titre.length() > 20 ? titre.substring(0, 17) + "..." : titre;
                series.getData().add(new XYChart.Data<>(shortTitle, count));
            }

            topBooksBarChart.getData().clear();
            topBooksBarChart.getData().add(series);

            logger.info("✅ Graphique top livres généré");
        } catch (Exception e) {
            logger.error("❌ Erreur lors du chargement du graphique top livres", e);
        }
    }
    // ===== GRAPHIQUE 4 : Disponibilité des livres =====
    private void chargerGraphiqueDisponibilite() {
        try {
            long totalLivres = livreService.compterTous();
            long livresDisponibles = livreService.compterDisponibles();
            long livresEmpruntes = totalLivres - livresDisponibles;

            ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
            pieData.add(new PieChart.Data("Disponibles", livresDisponibles));
            pieData.add(new PieChart.Data("Empruntés", livresEmpruntes));

            availabilityPieChart.setData(pieData);
            availabilityPieChart.setStyle("-fx-pie-label-visible: true;");

            logger.info("✅ Graphique disponibilité généré");
        } catch (Exception e) {
            logger.error("❌ Erreur lors du chargement du graphique disponibilité", e);
        }
    }
}