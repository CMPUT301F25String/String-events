package com.example.string_events;

import android.net.Uri;

import androidx.annotation.NonNull;

/**
 * Model representing a notification for a user about an event,
 * including selection status and basic event metadata.
 */
public class Notification {
    String username;
    boolean selectedStatus;
    String eventId;
    Uri eventPhoto; // eventPhoto needs to be of type Uri because it is a user uploaded image
    String eventName;

    /**
     * Creates a notification instance.
     *
     * @param username       recipient username
     * @param selectedStatus whether the user was selected/invited
     * @param eventId        associated event ID
     * @param eventPhoto     event photo {@link Uri}
     * @param eventName      event display name
     */
    public Notification(String username, boolean selectedStatus, String eventId, Uri eventPhoto, String eventName) {
        this.username = username;
        this.selectedStatus = selectedStatus;
        this.eventId = eventId;
        this.eventPhoto = eventPhoto;
        this.eventName = eventName;
    }

    /**
     * @return recipient username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Updates the recipient username.
     * @param username new username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * @return whether the user is selected/invited
     */
    public boolean getSelectedStatus() {
        return selectedStatus;
    }

    /**
     * Sets the selection status.
     * @param selectedStatus {@code true} if selected
     */
    public void setSelectedStatus(boolean selectedStatus) {
        this.selectedStatus = selectedStatus;
    }

    /**
     * @return associated event ID
     */
    public String getEventId() {
        return eventId;
    }

    /**
     * Updates the associated event ID.
     * @param eventId new event ID
     */
    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    /**
     * @return event photo {@link Uri}
     */
    public Uri getEventPhoto() {
        return eventPhoto;
    }

    /**
     * Updates the event photo.
     * @param eventPhoto new photo {@link Uri}
     */
    public void setEventPhoto(Uri eventPhoto) {
        this.eventPhoto = eventPhoto;
    }

    /**
     * @return event display name
     */
    public String getEventName() {
        return eventName;
    }

    /**
     * Updates the event display name.
     * @param eventName new event name
     */
    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    /**
     * @return concise debug string
     */
    @NonNull
    @Override
    public String toString() {
        return "Notification{" +
                "selectedStatus=" + selectedStatus +
                ", eventPhoto=" + eventPhoto +
                ", eventName='" + eventName + '\'' +
                '}';
    }
}
