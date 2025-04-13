package org.playground;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public class ScrapTest {
    public static void main(String[] args) {
        try {
            String url = "https://feverup.com/fr/paris/theatre";

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

                Element imgEl = eventElement.selectFirst("figure a img");

                String imageUrl = "N/A";
                if (imgEl != null) {
                    // Try to fetch image from data-src if available
                    if (imgEl.hasAttr("data-src") && !imgEl.attr("data-src").isEmpty()) {
                        imageUrl = imgEl.attr("data-src");
                    } else {
                        imageUrl = imgEl.attr("src");
                        // If the src is a placeholder (base64 data) and data-src exists, use it instead
                        if (imageUrl.startsWith("data:") && imgEl.hasAttr("data-src")) {
                            imageUrl = imgEl.attr("data-src");
                        }
                    }
                }
                // Print results
                System.out.println("Event Name: " + name);
                System.out.println("Event Date: " + date);
                System.out.println("Event Description: " + description);
                System.out.println("Event Image: " + imageUrl);
                System.out.println("--------------------------------");
            }



        }
        catch (IOException e) {
            e.printStackTrace();
        }

    }
}