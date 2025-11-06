package com.example.string_events;

public class AdminEvent {
    public enum Status { IN_PROGRESS, SCHEDULED, FINISHED }

    private final String title;
    private final String time;
    private final String location;
    private final String organizer;
    private final Status status;
    private final int coverResId;
    private final int locationLogoResId;

    public AdminEvent(String title,
                      String time,
                      String location,
                      String organizer,
                      Status status,
                      int coverResId,
                      int locationLogoResId) {
        this.title = title;
        this.time = time;
        this.location = location;
        this.organizer = organizer;
        this.status = status;
        this.coverResId = coverResId;
        this.locationLogoResId = locationLogoResId;
    }

    public String getTitle() { return title; }
    public String getTime() { return time; }
    public String getLocation() { return location; }
    public String getOrganizer() { return organizer; }
    public Status getStatus() { return status; }
    public int getCoverResId() { return coverResId; }
    public int getLocationLogoResId() { return locationLogoResId; }
}
