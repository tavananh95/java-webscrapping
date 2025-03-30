package org.scrapper.fxmodels;

public class EventItem {
    private final String eventName;
    private final String eventDate;
    private final String eventDescription;
    private final String eventImageUrl;

    public EventItem(String eventName, String eventDate, String eventDescription, String eventImageUrl) {
        this.eventName = eventName;
        this.eventDate = eventDate;
        this.eventDescription = eventDescription;
        this.eventImageUrl = eventImageUrl;
    }

    public String getEventName() {
        return eventName;
    }

    public String getEventDate() {
        return eventDate;
    }

    public String getEventDescription() {
        return eventDescription;
    }

    public String getEventImageUrl() {
        return eventImageUrl;
    }
}
