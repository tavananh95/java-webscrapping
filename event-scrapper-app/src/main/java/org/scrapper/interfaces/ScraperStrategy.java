package org.scrapper.interfaces;

import javafx.collections.ObservableList;
import org.scrapper.fxmodels.EventItem;

import java.io.IOException;

public interface ScraperStrategy {
    ObservableList<EventItem> scrape(String url) throws IOException;
}
