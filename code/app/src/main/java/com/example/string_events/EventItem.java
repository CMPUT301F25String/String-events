package com.example.string_events;

import com.google.firebase.Timestamp;

/**
 * Simple data holder for an event row displayed in the list view.
 */
public class EventItem {
    String imageUrl;
    String id;
    String title;
    String location;
    Timestamp startAt, endAt;
    int maxAttendees, attendeesCount;
}