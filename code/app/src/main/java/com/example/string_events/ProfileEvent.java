package com.example.string_events;

import android.net.Uri;

import java.time.ZonedDateTime;

/**
 * Model representing a user's event shown on the profile screen,
 * including cover image URL, name, schedule, and location.
 */
public class ProfileEvent {
    String profileEventId;
    String profileEventPhotoUrl;
    String profileEventName;
    ZonedDateTime profileEventStartDateTime;
    ZonedDateTime profileEventEndDateTime;
    String profileEventLocation;

    /**
     * Creates a profile event entry.
     *
     * @param profileEventPhotoUrl       URL to the event cover image
     * @param profileEventName           display name of the event
     * @param profileEventStartDateTime  event start time (with zone)
     * @param profileEventEndDateTime    event end time (with zone)
     * @param location                   event location text
     */
    public ProfileEvent(String profileEventId, String profileEventPhotoUrl, String profileEventName,
                        ZonedDateTime profileEventStartDateTime, ZonedDateTime profileEventEndDateTime,
                        String location) {
        this.profileEventId = profileEventId;
        this.profileEventPhotoUrl = profileEventPhotoUrl;
        this.profileEventName = profileEventName;
        this.profileEventStartDateTime = profileEventStartDateTime;
        this.profileEventEndDateTime = profileEventEndDateTime;
        this.profileEventLocation = location;
    }

    /** @return cover image URL */
    public String getProfileEventPhotoUrl() {
        return profileEventPhotoUrl;
    }

    /** @param profileEventPhotoUrl new cover image URL */
    public void setProfileEventPhotoUrl(String profileEventPhotoUrl) {
        this.profileEventPhotoUrl = profileEventPhotoUrl;
    }

    /** @return event display name */
    public String getProfileEventName() {
        return profileEventName;
    }

    /** @param profileEventName new event name */
    public void setProfileEventName(String profileEventName) {
        this.profileEventName = profileEventName;
    }

    /** @return start time (zoned) */
    public ZonedDateTime getProfileEventStartDateTime() {
        return profileEventStartDateTime;
    }

    /** @param profileEventStartDateTime new start time (zoned) */
    public void setProfileEventStartDateTime(ZonedDateTime profileEventStartDateTime) {
        this.profileEventStartDateTime = profileEventStartDateTime;
    }

    /** @return end time (zoned) */
    public ZonedDateTime getProfileEventEndDateTime() {
        return profileEventEndDateTime;
    }

    /** @param profileEventEndDateTime new end time (zoned) */
    public void setProfileEventEndDateTime(ZonedDateTime profileEventEndDateTime) {
        this.profileEventEndDateTime = profileEventEndDateTime;
    }

    /** @return location text */
    public String getProfileEventLocation() {
        return profileEventLocation;
    }

    /** @param profileEventLocation new location text */
    public void setProfileEventLocation(String profileEventLocation) {
        this.profileEventLocation = profileEventLocation;
    }

    public String getEventId() {
        return this.profileEventId;
    }
}
