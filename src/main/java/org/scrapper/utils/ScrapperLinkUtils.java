package org.scrapper.utils;

import java.util.Map;

public class ScrapperLinkUtils {

    public static String buildFranceBilletUrl(String ville, String eventType) {
        Map<String, String> villeIds = Map.of(
                "paris", "369",
                "lyon", "2022",
                "marseille", "2005"
        );

        // Event type to URL segment
        Map<String, String> eventPaths = Map.of(
                "theatre", "theatre-188",
                "expositions", "expositions-musees-185",
                "musique", "classique-danse-195/musique-classique-2105"
        );

        String villeKey = ville.toLowerCase();
        String villeId = villeIds.getOrDefault(villeKey, "369");
        String eventPath = eventPaths.getOrDefault(eventType.toLowerCase(), "theatre-188");

        return "https://www.francebillet.com/city/" + villeKey + "-" + villeId + "/" + eventPath + "/";
    }

    public static String buildFeverUrl(String ville, String eventType) {
        String url =  "https://feverup.com/fr/%s/%s";
        // "https://feverup.com/fr/" + ville.toLowerCase() + "/" + event.toLowerCase()

        if (eventType.equals("Musique")) {
            return String.format(url, ville.toLowerCase(), "evenements-musicaux");
        }

        return String.format(url, ville.toLowerCase(), eventType.toLowerCase());
    }
}