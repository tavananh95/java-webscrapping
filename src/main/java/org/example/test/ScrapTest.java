package org.example.test;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public class ScrapTest {
    public static void main(String[] args) {
        try {
            String url = "https://www.francebillet.com/city/paris-369/theatre-188/";

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
            // Select all <product-group-item> elements
            Elements productGroups = doc.select("product-group-item");

            for (Element productGroup : productGroups) {
                // Extract event name
                Element eventNameElement = productGroup.selectFirst("div.listing-details span");
                String eventName = eventNameElement != null ? eventNameElement.text() : "N/A";

                // Extract event date
                Element eventDateElement = productGroup.selectFirst("span.listing-data span");
                String eventDate = eventDateElement != null ? eventDateElement.text() : "N/A";

                // Extract event description
                Element eventDescriptionElement = productGroup.selectFirst("div.listing-description span");
                String eventDescription = eventDescriptionElement != null ? eventDescriptionElement.text() : "N/A";


                // Extract event image
                Element eventImageElement = productGroup.selectFirst("div.listing-image-wrapper img");
                String eventImage = eventImageElement != null ? eventImageElement.attr("data-src") : "N/A";

                // Print results
                System.out.println("Event Name: " + eventName);
                System.out.println("Event Date: " + eventDate);
                System.out.println("Event Description: " + eventDescription);
                System.out.println("Event Image: " + eventImage);
                System.out.println("--------------------------------");
            }

        }
        catch (IOException e) {
            e.printStackTrace();
        }

    }
}