package org.scrapper.implementation;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.scrapper.fxmodels.EventItem;
import org.scrapper.interfaces.ScraperStrategy;

import java.io.IOException;

public class FranceBilletScraper implements ScraperStrategy {
    @Override
    public ObservableList<EventItem> scrape(String url) throws IOException {
        ObservableList<EventItem> eventList = FXCollections.observableArrayList();
        Document doc = Jsoup
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
            String name = productGroup.selectFirst("div.listing-details span") != null ? productGroup.selectFirst("div.listing-details span").text() : "N/A";
            String date = productGroup.selectFirst("span.listing-data span") != null ? productGroup.selectFirst("span.listing-data span").text() : "N/A";
            String description = productGroup.selectFirst("div.listing-description span") != null ? productGroup.selectFirst("div.listing-description span").text() : "N/A";
            String imageUrl = productGroup.selectFirst("div.listing-image-wrapper img") != null ? productGroup.selectFirst("div.listing-image-wrapper img").attr("data-src") : "N/A";

            eventList.add(new EventItem(name, date, description, imageUrl));
        }
        return eventList;
    }
}
