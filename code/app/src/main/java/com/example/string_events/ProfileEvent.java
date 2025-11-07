package com.example.string_events;

import android.net.Uri;

import java.time.ZonedDateTime;

public class ProfileEvent {
    String profileEventPhotoUrl;
    String profileEventName;
    ZonedDateTime profileEventStartDateTime;
    ZonedDateTime profileEventEndDateTime;
    String profileEventLocation;

    public ProfileEvent(String profileEventPhotoUrl, String profileEventName,
                        ZonedDateTime profileEventStartDateTime, ZonedDateTime profileEventEndDateTime,
                        String location) {
        this.profileEventPhotoUrl = profileEventPhotoUrl;
        this.profileEventName = profileEventName;
        this.profileEventStartDateTime = profileEventStartDateTime;
        this.profileEventEndDateTime = profileEventEndDateTime;
        this.profileEventLocation = location;
    }

    public String getProfileEventPhotoUrl() {
        return profileEventPhotoUrl;
    }

    public void setProfileEventPhotoUrl(String profileEventPhotoUrl) {
        this.profileEventPhotoUrl = profileEventPhotoUrl;
    }

    public String getProfileEventName() {
        return profileEventName;
    }

    public void setProfileEventName(String profileEventName) {
        this.profileEventName = profileEventName;
    }

    public ZonedDateTime getProfileEventStartDateTime() {
        return profileEventStartDateTime;
    }

    public void setProfileEventStartDateTime(ZonedDateTime profileEventStartDateTime) {
        this.profileEventStartDateTime = profileEventStartDateTime;
    }

    public ZonedDateTime getProfileEventEndDateTime() {
        return profileEventEndDateTime;
    }

    public void setProfileEventEndDateTime(ZonedDateTime profileEventEndDateTime) {
        this.profileEventEndDateTime = profileEventEndDateTime;
    }

    public String getProfileEventLocation() {
        return profileEventLocation;
    }

    public void setProfileEventLocation(String profileEventLocation) {
        this.profileEventLocation = profileEventLocation;
    }
}
