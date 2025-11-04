package com.example.string_events;

import android.net.Uri;

import androidx.annotation.NonNull;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class Event {
    String title;
    Uri photo; // event photo is stored as an Uri because it is uploaded by the user
    String description;
    ArrayList<String> tags;
    LocalDateTime startDateTime;
    LocalDateTime endDateTime;
    String location;
    LocalDateTime registrationStartDateTime;
    LocalDateTime registrationEndDateTime;
    int numOfAttendants;
    int waitlistLimit;
    boolean geolocationRequirement;
    boolean visibility;

    public Event(String title, Uri photo, String description, ArrayList<String> tags,
                 LocalDateTime startDateTime, LocalDateTime endDateTime, String location,
                 LocalDateTime registrationStartDateTime, LocalDateTime registrationEndDateTime,
                 int numOfAttendants, int waitlistLimit, boolean geolocationRequirement, boolean visibility) {
        this.title = title;
        this.photo = photo;
        this.description = description;
        this.tags = tags;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.location = location;
        this.registrationStartDateTime = registrationStartDateTime;
        this.registrationEndDateTime = registrationEndDateTime;
        this.numOfAttendants = numOfAttendants;
        this.waitlistLimit = waitlistLimit;
        this.geolocationRequirement = geolocationRequirement;
        this.visibility = visibility;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Uri getPhoto() {
        return photo;
    }

    public void setPhoto(Uri photo) {
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

    public LocalDateTime getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(LocalDateTime startDateTime) {
        this.startDateTime = startDateTime;
    }

    public LocalDateTime getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(LocalDateTime endDateTime) {
        this.endDateTime = endDateTime;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public LocalDateTime getRegistrationStartDateTime() {
        return registrationStartDateTime;
    }

    public void setRegistrationStartDateTime(LocalDateTime registrationStartDateTime) {
        this.registrationStartDateTime = registrationStartDateTime;
    }

    public LocalDateTime getRegistrationEndDateTime() {
        return registrationEndDateTime;
    }

    public void setRegistrationEndDateTime(LocalDateTime registrationEndDateTime) {
        this.registrationEndDateTime = registrationEndDateTime;
    }

    public int getNumOfAttendants() {
        return numOfAttendants;
    }

    public void setNumOfAttendants(int numOfAttendants) {
        this.numOfAttendants = numOfAttendants;
    }

    public int getWaitlistLimit() {
        return waitlistLimit;
    }

    public void setWaitlistLimit(int waitlistLimit) {
        this.waitlistLimit = waitlistLimit;
    }

    public boolean isGeolocationRequirement() {
        return geolocationRequirement;
    }

    public void setGeolocationRequirement(boolean geolocationRequirement) {
        this.geolocationRequirement = geolocationRequirement;
    }

    public boolean isEventVisible() {
        return visibility;
    }

    public void setEventVisible(boolean eventVisible) {
        this.visibility = eventVisible;
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
                ", numOfAttendants=" + numOfAttendants +
                ", waitlistLimit=" + waitlistLimit +
                ", geolocationRequirement=" + geolocationRequirement +
                ", eventVisible=" + visibility +
                '}';
    }
}
