package org.scrapper;

import org.scrapper.implementation.FeverScraper;
import org.scrapper.implementation.FranceBilletScraper;
import org.scrapper.interfaces.ScraperStrategy;

public class ScraperFactory {
    public static ScraperStrategy getScraper(String url) {
        if (url.contains("francebillet.com")) {
            return new FranceBilletScraper();
        } else if (url.contains("feverup.com")) {
            return new FeverScraper();
        } else {
            throw new IllegalArgumentException("No scraper available for this URL.");
        }
    }
}
