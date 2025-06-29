package org.scrapper;

import fxmodels.EventItem;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
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
import org.scrapper.interfaces.ScraperStrategy;
import org.scrapper.utils.PluginLoader;
import org.scrapper.utils.ScrapperLinkUtils;
import plugins.EventActionPlugin;
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
    @FXML
    private Label versionLabel;


    private List<Plugin> loadedPlugins = new ArrayList<>();
    private List<EventActionPlugin> loadedEventActionPlugins = new ArrayList<>();


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

        String version = getAppVersion();
        primaryStage.setTitle("Event Scraper version " + version);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @FXML
    public void initialize() {
        themeSelector.setItems(FXCollections.observableArrayList("Clair","Bleu"));
        themeSelector.getSelectionModel().selectFirst();

        versionLabel.setText("Version: " + getAppVersion());

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

                    // New button created for each item
                    Button moreButton = new Button("En savoir plus");
                    moreButton.setStyle("-fx-background-color: #337ab7; -fx-text-fill: white;");
                    moreButton.setOnAction(e -> {
                        String link = item.getEventLink();
                        if (link != null && !link.equals("N/A")) {
                            try {
                                java.awt.Desktop.getDesktop().browse(new java.net.URI(link));
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    });

                    VBox buttons = new VBox(5);
                    buttons.getChildren().add(moreButton);

                    for (EventActionPlugin plugin : loadedEventActionPlugins) {
                        Button pluginBtn = new Button(plugin.getActionLabel());
                        pluginBtn.setOnAction(e -> plugin.onActionTriggered(item));
                        buttons.getChildren().add(pluginBtn);
                    }

                    VBox fullContent = new VBox(10, eventNameLabel, eventDateLabel, eventDescriptionLabel, imageView, buttons);
                    fullContent.setPadding(new Insets(5));
                    setGraphic(fullContent);
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
        loadedEventActionPlugins.clear();
        pluginContainer.getChildren().clear();

        // Chargement des plugins UI / thème
        List<Plugin> uiPlugins = PluginLoader.loadPlugins("plugins", Plugin.class);
        Set<String> pluginNames = new HashSet<>();

        for (Plugin plugin : uiPlugins) {
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

        // Chargement des plugins d’action sur événement
        List<EventActionPlugin> eventPlugins =
                PluginLoader.loadPlugins("plugins", EventActionPlugin.class);

        loadedEventActionPlugins.addAll(eventPlugins);

        if (!eventPlugins.isEmpty()) {
            System.out.println("Plugins d’action trouvés : " + eventPlugins.size());
        }

        if (pluginNames.isEmpty() && eventPlugins.isEmpty()) {
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

    public static String getAppVersion() {
        Package pkg = MainScrapper.class.getPackage();
        if (pkg != null && pkg.getImplementationVersion() != null) {
            return pkg.getImplementationVersion();
        } else {
            return "dev";
        }
    }



    public static void main(String[] args) {
        launch(args);
    }
}
