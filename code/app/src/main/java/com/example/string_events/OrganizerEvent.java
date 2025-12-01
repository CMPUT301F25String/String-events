package com.example.string_events;

import com.google.firebase.Timestamp;

/**
 * Simple data model representing an event created by an organizer.
 * <p>
 * This class is typically used to:
 * <ul>
 *     <li>Store basic event metadata (title, location, times, etc.).</li>
 *     <li>Pass event data between activities and adapters.</li>
 *     <li>Map Firestore documents into Java objects.</li>
 * </ul>
 */
public class OrganizerEvent {

    /**
     * Unique identifier of the event (usually the Firestore document ID).
     */
    public String id;

    /**
     * Human-readable title of the event.
     */
    public String title;

    /**
     * Location where the event will take place.
     */
    public String location;

    /**
     * Timestamp for when the event starts.
     */
    public Timestamp startAt;

    /**
     * Timestamp for when the event ends.
     */
    public Timestamp endAt; // Added this field

    /**
     * Maximum number of attendees allowed to register for the event.
     */
    public int maxAttendees;

    /**
     * Current number of registered attendees.
     */
    public int attendeesCount;

    /**
     * URL string pointing to the cover image or thumbnail for this event.
     */
    public String imageUrl;

    /**
     * Username or identifier of the organizer who created this event.
     */
    public String creator;
}
