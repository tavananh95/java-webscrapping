package fxmodels;

public class EventItem {
    private final String eventName;
    private final String eventDate;
    private final String eventDescription;
    private final String eventImageUrl;
    private final String eventLink;

    public EventItem(String eventName, String eventDate, String eventDescription, String eventImageUrl, String eventLink) {
        this.eventName = eventName;
        this.eventDate = eventDate;
        this.eventDescription = eventDescription;
        this.eventImageUrl = eventImageUrl;
        this.eventLink = eventLink;
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

    public String getEventLink() { return eventLink; }

    public String toFormattedString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Nom de l'événement : ").append(eventName).append("\n");
        sb.append("Date : ").append(eventDate).append("\n\n");
        sb.append("Description : ").append(eventDescription).append("\n\n");
        sb.append("Lien : ").append(eventLink);
        return sb.toString();
    }
}
