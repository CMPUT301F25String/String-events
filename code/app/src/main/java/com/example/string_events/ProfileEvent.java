package com.example.string_events;

import java.util.Date;

/**
 * Model representing a user's event shown on the profile screen,
 * including cover image URL, name, schedule, and location.
 */
public class ProfileEvent {
    String profileEventId;
    String profileEventPhotoUrl;
    String profileEventName;
    Date profileEventStartDateTime;
    Date profileEventEndDateTime;
    String profileEventLocation;

    /**
     * Creates a profile event entry.
     *
     * @param profileEventId             the unique ID of the event
     * @param profileEventPhotoUrl       URL to the event cover image
     * @param profileEventName           display name of the event
     * @param profileEventStartDateTime  event start time
     * @param profileEventEndDateTime    event end time
     * @param location                   event location text
     */
    public ProfileEvent(String profileEventId, String profileEventPhotoUrl, String profileEventName,
                        Date profileEventStartDateTime, Date profileEventEndDateTime,
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

    /** @return start time */
    public Date getProfileEventStartDateTime() {
        return profileEventStartDateTime;
    }

    /** @param profileEventStartDateTime new start time */
    public void setProfileEventStartDateTime(Date profileEventStartDateTime) {
        this.profileEventStartDateTime = profileEventStartDateTime;
    }

    /** @return end time */
    public Date getProfileEventEndDateTime() {
        return profileEventEndDateTime;
    }

    /** @param profileEventEndDateTime new end time */
    public void setProfileEventEndDateTime(Date profileEventEndDateTime) {
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