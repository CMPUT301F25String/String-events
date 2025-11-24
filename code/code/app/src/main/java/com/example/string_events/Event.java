package com.example.string_events;

import android.net.Uri;

import androidx.annotation.NonNull;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.UUID;

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

    ArrayList<User> waitlist;

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
        this.waitlist = new ArrayList<>(); // when creating a new event, an empty waitlist is created
    }

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

    public String getEventId() {
        return eventId;
    }

    public String getEventCreator() {
        return eventCreator;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Uri getPhotoUri() {
        return photo;
    }

    public void setPhotoUri(Uri photo) {
        this.photo = photo;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ArrayList<String> getTags() {
        return tags;
    }

    public void setTags(ArrayList<String> tags) {
        this.tags = tags;
    }

    public ZonedDateTime getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(ZonedDateTime startDateTime) {
        this.startDateTime = startDateTime;
    }

    public ZonedDateTime getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(ZonedDateTime endDateTime) {
        this.endDateTime = endDateTime;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public ZonedDateTime getRegistrationStartDateTime() {
        return registrationStartDateTime;
    }

    public void setRegistrationStartDateTime(ZonedDateTime registrationStartDateTime) {
        this.registrationStartDateTime = registrationStartDateTime;
    }

    public ZonedDateTime getRegistrationEndDateTime() {
        return registrationEndDateTime;
    }

    public void setRegistrationEndDateTime(ZonedDateTime registrationEndDateTime) {
        this.registrationEndDateTime = registrationEndDateTime;
    }

    public int getMaxAttendees() {
        return maxAttendees;
    }

    public void setMaxAttendees(int maxAttendees) {
        this.maxAttendees = maxAttendees;
    }

    public int getWaitlistLimit() {
        return waitlistLimit;
    }

    public void setWaitlistLimit(int waitlistLimit) {
        this.waitlistLimit = waitlistLimit;
    }

    public boolean getGeolocationRequirement() {
        return geolocationRequirement;
    }

    public void setGeolocationRequirement(boolean geolocationRequirement) {
        this.geolocationRequirement = geolocationRequirement;
    }

    public boolean getEventVisibility() {
        return visibility;
    }

    public void setEventVisibility(boolean eventVisible) {
        this.visibility = eventVisible;
    }

    public ArrayList<User> getWaitlist() {
        return waitlist;
    }

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
