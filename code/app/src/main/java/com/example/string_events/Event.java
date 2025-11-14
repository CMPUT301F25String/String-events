package com.example.string_events;

import android.net.Uri;

import androidx.annotation.NonNull;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Domain model representing an event with metadata, schedule, capacity, and visibility.
 * <p>
 * Times are stored as {@link ZonedDateTime}; the photo is a user-supplied {@link Uri}.
 * A unique {@code eventId} is generated on creation.
 */
public class Event {
    String eventId;
    String eventCreator;
    String title;
    Uri photo; // event photo is stored as an Uri because it is uploaded by the user
    String description;
    ArrayList<String> tags;
    ZonedDateTime startDateTime;
    ZonedDateTime endDateTime;
    String location;
    ZonedDateTime registrationStartDateTime;
    ZonedDateTime registrationEndDateTime;
    int maxAttendees;
    int waitlistLimit;
    boolean geolocationRequirement;
    boolean visibility;

    ArrayList<User> attendees;
    ArrayList<User> waitlist;

    /**
     * Creates a fully specified event and initializes empty attendee/waitlist collections.
     * A random {@code eventId} is generated.
     *
     * @param eventCreator           creator username or identifier
     * @param title                  event title
     * @param photo                  image {@link Uri} chosen by the user
     * @param description            short description
     * @param tags                   optional category tags
     * @param startDateTime          start date-time (with zone)
     * @param endDateTime            end date-time (with zone)
     * @param location               event location
     * @param registrationStartDateTime registration window start
     * @param registrationEndDateTime   registration window end
     * @param numOfAttendants        maximum number of attendees
     * @param waitlistLimit          maximum waitlist size
     * @param geolocationRequirement whether geolocation is required
     * @param visibility             {@code true} if public, {@code false} if private
     */
    public Event(String eventCreator, String title, Uri photo, String description, ArrayList<String> tags,
                 ZonedDateTime startDateTime, ZonedDateTime endDateTime, String location,
                 ZonedDateTime registrationStartDateTime, ZonedDateTime registrationEndDateTime,
                 int numOfAttendants, int waitlistLimit, boolean geolocationRequirement, boolean visibility) {
        this.eventCreator = eventCreator;
        this.title = title;
        this.photo = photo;
        this.description = description;
        this.tags = tags;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.location = location;
        this.registrationStartDateTime = registrationStartDateTime;
        this.registrationEndDateTime = registrationEndDateTime;
        this.maxAttendees = numOfAttendants;
        this.waitlistLimit = waitlistLimit;
        this.geolocationRequirement = geolocationRequirement;
        this.visibility = visibility;

        // generates a unique random id for the new event
        this.eventId = UUID.randomUUID().toString();
        this.attendees = new ArrayList<>(); // when creating a new event, an empty attendees list is created
        this.waitlist = new ArrayList<>(); // when creating a new event, an empty waitlist is created
    }

    /**
     * Convenience constructor for testing; generates a random {@code eventId}.
     *
     * @param title                   event title
     * @param photo                   image {@link Uri}
     * @param description             short description
     * @param tags                    optional tags
     * @param startDateTime           start date-time (with zone)
     * @param endDateTime             end date-time (with zone)
     * @param location                event location
     * @param registrationStartDateTime registration window start
     * @param registrationEndDateTime   registration window end
     */
    // testing constructor
    public Event(String title, Uri photo, String description, ArrayList<String> tags,
                 ZonedDateTime startDateTime, ZonedDateTime endDateTime, String location,
                 ZonedDateTime registrationStartDateTime, ZonedDateTime registrationEndDateTime) {
        this.eventId = UUID.randomUUID().toString();
        this.title = title;
        this.photo = photo;
        this.description = description;
        this.tags = tags;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.location = location;
        this.registrationStartDateTime = registrationStartDateTime;
        this.registrationEndDateTime = registrationEndDateTime;
    }

    /**
     * @return unique identifier of this event
     */
    public String getEventId() {
        return eventId;
    }

    /**
     * @return creator username or identifier
     */
    public String getEventCreator() {
        return eventCreator;
    }

    /**
     * @return event title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Updates the event title.
     * @param title new title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return photo {@link Uri}
     */
    public Uri getPhotoUri() {
        return photo;
    }

    /**
     * Updates the photo {@link Uri}.
     * @param photo new image uri
     */
    public void setPhotoUri(Uri photo) {
        this.photo = photo;
    }

    /**
     * @return event description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Updates the description.
     * @param description new description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return list of category tags
     */
    public ArrayList<String> getTags() {
        return tags;
    }

    /**
     * Replaces the tag list.
     * @param tags new tags
     */
    public void setTags(ArrayList<String> tags) {
        this.tags = tags;
    }

    /**
     * @return start date-time (with zone)
     */
    public ZonedDateTime getStartDateTime() {
        return startDateTime;
    }

    /**
     * Updates the start date-time.
     * @param startDateTime new start (with zone)
     */
    public void setStartDateTime(ZonedDateTime startDateTime) {
        this.startDateTime = startDateTime;
    }

    /**
     * @return end date-time (with zone)
     */
    public ZonedDateTime getEndDateTime() {
        return endDateTime;
    }

    /**
     * Updates the end date-time.
     * @param endDateTime new end (with zone)
     */
    public void setEndDateTime(ZonedDateTime endDateTime) {
        this.endDateTime = endDateTime;
    }

    /**
     * @return event location
     */
    public String getLocation() {
        return location;
    }

    /**
     * Updates the location.
     * @param location new location
     */
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * @return registration window start (with zone)
     */
    public ZonedDateTime getRegistrationStartDateTime() {
        return registrationStartDateTime;
    }

    /**
     * Updates the registration window start.
     * @param registrationStartDateTime new start (with zone)
     */
    public void setRegistrationStartDateTime(ZonedDateTime registrationStartDateTime) {
        this.registrationStartDateTime = registrationStartDateTime;
    }

    /**
     * @return registration window end (with zone)
     */
    public ZonedDateTime getRegistrationEndDateTime() {
        return registrationEndDateTime;
    }

    /**
     * Updates the registration window end.
     * @param registrationEndDateTime new end (with zone)
     */
    public void setRegistrationEndDateTime(ZonedDateTime registrationEndDateTime) {
        this.registrationEndDateTime = registrationEndDateTime;
    }

    /**
     * @return maximum number of attendees
     */
    public int getMaxAttendees() {
        return maxAttendees;
    }

    /**
     * Updates the attendee capacity.
     * @param maxAttendees new capacity
     */
    public void setMaxAttendees(int maxAttendees) {
        this.maxAttendees = maxAttendees;
    }

    /**
     * @return waitlist capacity
     */
    public int getWaitlistLimit() {
        return waitlistLimit;
    }

    /**
     * Updates the waitlist capacity.
     * @param waitlistLimit new limit
     */
    public void setWaitlistLimit(int waitlistLimit) {
        this.waitlistLimit = waitlistLimit;
    }

    /**
     * @return whether geolocation is required
     */
    public boolean getGeolocationRequirement() {
        return geolocationRequirement;
    }

    /**
     * Sets the geolocation requirement flag.
     * @param geolocationRequirement {@code true} to require geolocation
     */
    public void setGeolocationRequirement(boolean geolocationRequirement) {
        this.geolocationRequirement = geolocationRequirement;
    }

    /**
     * @return {@code true} if the event is public; {@code false} if private
     */
    public boolean getEventVisibility() {
        return visibility;
    }

    /**
     * Sets the event visibility.
     * @param eventVisible {@code true} for public, {@code false} for private
     */
    public void setEventVisibility(boolean eventVisible) {
        this.visibility = eventVisible;
    }

    /**
     * @return the mutable waitlist collection
     */
    public ArrayList<User> getWaitlist() {
        return waitlist;
    }

    /**
     * @return a concise debug representation of the event
     */
    @NonNull
    @Override
    public String toString() {
        return "Event{" +
                "title='" + title + '\'' +
                ", photo=" + photo +
                ", description='" + description + '\'' +
                ", tags=" + tags +
                ", startDateTime=" + startDateTime +
                ", endDateTime=" + endDateTime +
                ", location='" + location + '\'' +
                ", registrationStartDateTime=" + registrationStartDateTime +
                ", registrationEndDateTime=" + registrationEndDateTime +
                ", numOfAttendants=" + maxAttendees +
                ", waitlistLimit=" + waitlistLimit +
                ", geolocationRequirement=" + geolocationRequirement +
                ", eventVisible=" + visibility +
                '}';
    }
}
