package com.example.eventspage;

public class Event {
    private String title;
    private String time;
    private String spots;
    private String place;
    private String status;

    public Event(String title, String time, String spots, String place, String status) {
        this.title = title;
        this.time = time;
        this.spots = spots;
        this.place = place;
        this.status = status;
    }

    // Getters and Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public String getSpots() { return spots; }
    public void setSpots(String spots) { this.spots = spots; }

    public String getPlace() { return place; }
    public void setPlace(String place) { this.place = place; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}