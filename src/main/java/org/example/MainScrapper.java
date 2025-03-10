package org.example;

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
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public class MainScrapper extends Application {

    @FXML
    private ComboBox<String> comboVilles;
    @FXML
    private ComboBox<String> comboEvents;
    @FXML
    private Button startScraping;
    @FXML
    private ListView<EventItem> eventListView;

    private String baseUrl = "https://www.francebillet.com/city/";
    private String selectedUrl = baseUrl; // Updated dynamically

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
        comboEvents.setItems(FXCollections.observableArrayList("Theatre", "Concert", "Sport"));

        comboVilles.getSelectionModel().selectFirst();
        comboEvents.getSelectionModel().selectFirst();

        // Set custom cell factory to display event details with images
        eventListView.setCellFactory(param -> new ListCell<EventItem>() {
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

                    // Load and display image
                    if (!item.getEventImageUrl().equals("N/A")) {
                        imageView.setImage(new Image(item.getEventImageUrl(), true));
                    } else {
                        imageView.setImage(null);
                    }

                    setGraphic(content);
                }
            }
        });
    }

    @FXML
    private void handleScrape() {
        String selectedVille = comboVilles.getValue();
        String selectedEvent = comboEvents.getValue();

        if (selectedVille != null && selectedEvent != null) {
            selectedUrl = baseUrl + selectedVille.toLowerCase() + "-369/" + selectedEvent.toLowerCase() + "-188/";

            System.out.println("Scraping URL: " + selectedUrl);
            scrapeEvents(selectedUrl);
        }
    }

    private void scrapeEvents(String url) {
        ObservableList<EventItem> eventList = FXCollections.observableArrayList();

        try {
            Document doc
                    = Jsoup
                    .connect(url)
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/133.0.0.0 Safari/537.36")
//                    .header("Referer", "https://www.ticketmaster.fr/")
                    .header("Accept-Language", "en-US,en;q=0.9,fr-FR;q=0.8,fr;q=0.7")
                    .header("sec-fetch-site", "same-origin")
                    .header("sec-fetch-mode", "navigate")
                    .header("sec-fetch-user", "?1")
                    .header("upgrade-insecure-requests", "1")
                    .get();

            Elements productGroups = doc.select("product-group-item");

            for (Element productGroup : productGroups) {
                Element eventNameElement = productGroup.selectFirst("div.listing-details span");
                String eventName = eventNameElement != null ? eventNameElement.text() : "N/A";

                Element eventDateElement = productGroup.selectFirst("span.listing-data span");
                String eventDate = eventDateElement != null ? eventDateElement.text() : "N/A";

                Element eventDescriptionElement = productGroup.selectFirst("div.listing-description span");
                String eventDescription = eventDescriptionElement != null ? eventDescriptionElement.text() : "N/A";

                Element eventImageElement = productGroup.selectFirst("div.listing-image-wrapper img");
                String eventImageUrl = eventImageElement != null ? eventImageElement.attr("data-src") : "N/A";

                eventList.add(new EventItem(eventName, eventDate, eventDescription, eventImageUrl));
            }

            eventListView.setItems(eventList);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
