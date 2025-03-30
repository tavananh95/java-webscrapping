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
import javafx.stage.Stage;
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import org.scrapper.fxmodels.EventItem;
import org.scrapper.interfaces.ScraperStrategy;
import org.scrapper.utils.ScrapperLinkUtils;

import java.io.IOException;
import java.util.List;

public class MainScrapper extends Application {

    @FXML
    private ComboBox<String> comboVilles;
    @FXML
    private ComboBox<String> comboEvents;
    @FXML
    private Button startScraping;
    @FXML
    private ListView<EventItem> eventListView;



    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/MainScrapper.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root);
        primaryStage.setTitle("Event Scraper");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @FXML
    public void initialize() {
        comboVilles.setItems(FXCollections.observableArrayList("Paris", "Lyon", "Marseille"));
        comboVilles.getSelectionModel().selectFirst();

        comboEvents.setItems(FXCollections.observableArrayList("Theatre", "Expositions", "Musique"));
        comboEvents.getSelectionModel().selectFirst();

        eventListView.setCellFactory(param -> new ListCell<>() {
            private final ImageView imageView = new ImageView();
            private final Label eventNameLabel = new Label();
            private final Label eventDateLabel = new Label();
            private final Label eventDescriptionLabel = new Label();
            private final VBox content = new VBox(eventNameLabel, eventDateLabel, eventDescriptionLabel, imageView);

            {
                imageView.setFitHeight(100);
                imageView.setFitWidth(100);
                eventNameLabel.setStyle("-fx-font-weight: bold;");
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

    public static void main(String[] args) {
        launch(args);
    }
}
