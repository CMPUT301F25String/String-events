package com.example.string_events;

import com.google.firebase.Timestamp;

public class OrganizerEvent {
    public String id;
    public String title;
    public String location;
    public Timestamp startAt;
    public Timestamp endAt; // Added this field
    public int maxAttendees;
    public int attendeesCount;
    public String imageUrl;
    public String creator;
}