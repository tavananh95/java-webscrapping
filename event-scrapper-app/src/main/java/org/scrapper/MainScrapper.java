package org.scrapper;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.scrapper.fxmodels.EventItem;
import org.scrapper.interfaces.ScraperStrategy;
import org.scrapper.utils.PluginLoader;
import org.scrapper.utils.ScrapperLinkUtils;
import plugins.Plugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

public class MainScrapper extends Application {

    @FXML
    private ComboBox<String> comboVilles;
    @FXML
    private ComboBox<String> comboEvents;
    @FXML
    private Button startScraping;
    @FXML
    private ListView<EventItem> eventListView;
    @FXML
    private Button exportButton;
    @FXML private ComboBox<String> themeSelector;
    private Scene currentScene;
    @FXML
    private VBox pluginContainer;

    private List<Plugin> loadedPlugins = new ArrayList<>();

    public void setCurrentScene(Scene scene) {
        this.currentScene = scene;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/MainScrapper.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root);

        MainScrapper controller = loader.getController();
        controller.setCurrentScene(scene);

        primaryStage.setTitle("Event Scraper");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @FXML
    public void initialize() {
        themeSelector.setItems(FXCollections.observableArrayList("Clair","Bleu"));
        themeSelector.getSelectionModel().selectFirst();

        comboVilles.setItems(FXCollections.observableArrayList("Paris", "Lyon", "Marseille"));
        comboVilles.getSelectionModel().selectFirst();

        comboEvents.setItems(FXCollections.observableArrayList("Theatre", "Expositions", "Musique"));
        comboEvents.getSelectionModel().selectFirst();

        eventListView.setCellFactory(param -> new ListCell<>() {
            private final ImageView imageView = new ImageView();
            private final Label eventNameLabel = new Label();
            private final Label eventDateLabel = new Label();
            private final Label eventDescriptionLabel = new Label();
            private final Button moreButton = new Button("En savoir plus");
            private final VBox content = new VBox(eventNameLabel, eventDateLabel, eventDescriptionLabel, imageView, moreButton);

            {
                imageView.setFitHeight(100);
                imageView.setFitWidth(100);
                eventNameLabel.setStyle("-fx-font-weight: bold;");
                moreButton.setStyle("-fx-background-color: #337ab7; -fx-text-fill: white;");
            }

            @Override
            protected void updateItem(EventItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    eventNameLabel.setText(item.getEventName());
                    eventDateLabel.setText("Date: " + item.getEventDate());
                    eventDescriptionLabel.setText(item.getEventDescription());

                    loadImage(item.getEventImageUrl());

                    // Bouton "En savoir plus"
                    moreButton.setOnAction(e -> {
                        if (item.getEventLink() != null && !item.getEventLink().equals("N/A")) {
                            try {
                                java.awt.Desktop.getDesktop().browse(new java.net.URI(item.getEventLink()));
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    });
                    setGraphic(content);
                }
            }

            private void loadImage(String imageUrl) {
                if (imageUrl != null && !imageUrl.equals("N/A")) {
                    try {
                        Image image = new Image(imageUrl, true);
                        imageView.setImage(image);
                    } catch (Exception e) {
                        System.err.println("Error loading image: " + imageUrl);
                        imageView.setImage(null);
                    }
                } else {
                    imageView.setImage(null);
                }
            }
        });
    }


    @FXML
    private void handleScrape() {
        String ville = comboVilles.getValue();
        String event = comboEvents.getValue();

        if (ville == null || event == null) return;

        ObservableList<EventItem> aggregatedResults = FXCollections.observableArrayList();

        List<String> urls = List.of(
                ScrapperLinkUtils.buildFranceBilletUrl(ville, event),
                ScrapperLinkUtils.buildFeverUrl(ville, event)
        );


        for (String url : urls) {
            try {
                System.out.println(url);
                ScraperStrategy scraper = ScraperFactory.getScraper(url);
                ObservableList<EventItem> result = scraper.scrape(url);
                aggregatedResults.addAll(result);
            } catch (IOException | IllegalArgumentException e) {
                System.err.println("Scraping failed for URL: " + url);
                e.printStackTrace();
            }
        }
        eventListView.getItems().clear();
        eventListView.setItems(aggregatedResults);
    }

    @FXML
    private void handleExport() {
        ObservableList<EventItem> items = eventListView.getItems();
        if (items == null || items.isEmpty()) {
            System.out.println("Aucun résultat à exporter.");
            return;
        }

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Événements");

        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Nom");
        header.createCell(1).setCellValue("Date");
        header.createCell(2).setCellValue("Description");
        header.createCell(3).setCellValue("Image");
        header.createCell(4).setCellValue("Lien");

        int rowIdx = 1;
        for (EventItem item : items) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(item.getEventName());
            row.createCell(1).setCellValue(item.getEventDate());
            row.createCell(2).setCellValue(item.getEventDescription());
            row.createCell(3).setCellValue(item.getEventLink());
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exporter vers Excel");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichier Excel", "*.xlsx"));
        fileChooser.setInitialFileName("resultats_evenements.xlsx");

        File file = fileChooser.showSaveDialog(exportButton.getScene().getWindow());

        if (file != null) {
            try (FileOutputStream fos = new FileOutputStream(file)) {
                workbook.write(fos);
                System.out.println("Fichier exporté : " + file.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleThemeChange() {
        String selected = themeSelector.getValue().toLowerCase();
        Scene scene = themeSelector.getScene();

        if (scene == null) return;

        scene.getStylesheets().clear();

        switch (selected) {
            case "bleu":
                scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/themes/theme-blue.css")).toExternalForm());
                break;
            default:
                scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/themes/theme-light.css")).toExternalForm());
        }
    }

    @FXML
    private void handleDownloadPlugin() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir un plugin .jar");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers JAR", "*.jar"));
        File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            String originalName = selectedFile.getName().replace(".jar", "");
            String timestampedName = originalName + "-" + System.currentTimeMillis() + ".jar";
            Path target = Paths.get("plugins", timestampedName);

            try {
                Files.createDirectories(target.getParent());
                Files.copy(selectedFile.toPath(), target, StandardCopyOption.REPLACE_EXISTING);
                showAlert("Plugin copié", "Le plugin a été copié avec succès :\n" + target.getFileName());

                // Recharge les plugins avec le nouveau fichier
                loadPluginsDynamically();

            } catch (IOException e) {
                showAlert("Erreur", "Échec de la copie du plugin :\n" + e.getMessage());
            }
        }
    }


    public void loadPluginsDynamically() {
        loadedPlugins.clear();
        pluginContainer.getChildren().clear();

        List<Plugin> plugins = PluginLoader.loadPlugins("plugins");
        System.out.println("Plugins trouvés : " + plugins.size());

        // Filtrage des doublons : on ne garde qu’un plugin par nom
        Set<String> pluginNames = new HashSet<>();

        for (Plugin plugin : plugins) {
            String name = plugin.getName();

            if (pluginNames.contains(name)) {
                System.out.println("Doublon ignoré : " + name);
                continue;
            }

            pluginNames.add(name);
            loadedPlugins.add(plugin);

            Button activate = new Button("Activer : " + name);
            Button deactivate = new Button("Désactiver : " + name);

            activate.setOnAction(e -> plugin.onLoad(currentScene));
            deactivate.setOnAction(e -> plugin.onUnload(currentScene));

            pluginContainer.getChildren().addAll(activate, deactivate);
        }

        if (pluginNames.isEmpty()) {
            showAlert("Plugins", "Aucun plugin chargé.");
        } else {
            System.out.println("Plugins affichés : " + pluginNames.size());
        }
    }


    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
