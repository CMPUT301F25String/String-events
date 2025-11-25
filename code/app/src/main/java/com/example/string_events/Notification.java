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
    String eventPhoto; // eventPhoto needs to be of type Uri because it is a user uploaded image
    String eventName;

    boolean isMessage;
    String messageText;

    /**
     * Creates a notification instance.
     *
     * @param username       recipient username
     * @param selectedStatus whether the user was selected/invited
     * @param eventId        associated event ID
     * @param eventPhoto     event photo {@link Uri}
     * @param eventName      event display name
     */
    public Notification(String username, boolean selectedStatus, String eventId, String eventPhoto, String eventName) {
        this.username = username;
        this.selectedStatus = selectedStatus;
        this.eventId = eventId;
        this.eventPhoto = eventPhoto;
        this.eventName = eventName;
        this.isMessage = false;
        this.messageText = null;
    }

    public Notification(String username, String eventId, String eventName, String eventPhoto, boolean isMessage, String messageText) {
        this.username = username;
        this.eventId = eventId;
        this.eventName = eventName;
        this.eventPhoto = eventPhoto;
        this.isMessage = isMessage;
        this.messageText = messageText;
        this.selectedStatus = false;
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
    public String getEventPhoto() {
        return eventPhoto;
    }

    /**
     * Updates the event photo.
     * @param eventPhoto new photo {@link Uri}
     */
    public void setEventPhoto(String eventPhoto) {
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

    public boolean isMessage() {
        return isMessage;
    }

    public void setMessage(boolean message) {
        isMessage = message;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
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
