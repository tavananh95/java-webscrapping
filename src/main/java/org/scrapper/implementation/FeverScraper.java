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

public class FeverScraper implements ScraperStrategy {

    @Override
    public ObservableList<EventItem> scrape(String url) throws IOException {
        ObservableList<EventItem> eventList = FXCollections.observableArrayList();

        Document doc = Jsoup.connect(url)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/133.0.0.0 Safari/537.36")
//                    .header("Referer", "https://www.ticketmaster.fr/")
                .header("Accept-Language", "en-US,en;q=0.9,fr-FR;q=0.8,fr;q=0.7")
                .header("sec-fetch-site", "same-origin")
                .header("sec-fetch-mode", "navigate")
                .header("sec-fetch-user", "?1")
                .header("upgrade-insecure-requests", "1")
                .get();

        Elements eventElements = doc.select("li[data-testid=fv-wpf-plan-grid__list-item]");

        for (Element eventElement : eventElements) {
            Element titleEl = eventElement.selectFirst("h3[data-testid=fv-plan-card-title]");
            String name = titleEl != null ? titleEl.text() : "N/A";

            Element dateEl = eventElement.selectFirst("div[data-testid=fv-plan-card-v2__date-range]");
            String date = dateEl != null ? dateEl.text() : "N/A";

            String description = "";

            // Element imgEl = eventElement.selectFirst("figure img");
            // String imageUrl = imgEl != null ? imgEl.attr("src") : "N/A";
            String imageUrl = "https://upload.wikimedia.org/wikipedia/commons/a/a3/Image-not-found.png?20210521171500";
            eventList.add(new EventItem(name, date, description, imageUrl));
        }

        return eventList;
    }
}
