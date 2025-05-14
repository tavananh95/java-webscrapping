package org.scrapper.interfaces;

import javafx.collections.ObservableList;
import fxmodels.EventItem;

import java.io.IOException;

public interface ScraperStrategy {
    ObservableList<EventItem> scrape(String url) throws IOException;
}
