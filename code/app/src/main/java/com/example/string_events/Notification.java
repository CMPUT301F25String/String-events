package com.example.string_events;

import android.net.Uri;

import androidx.annotation.NonNull;

public class Notification {
    String username;
    boolean selectedStatus;
    String eventId;
    Uri eventPhoto; // eventPhoto needs to be of type Uri because it is a user uploaded image
    String eventName;

    public Notification(String username, boolean selectedStatus, String eventId, Uri eventPhoto, String eventName) {
        this.username = username;
        this.selectedStatus = selectedStatus;
        this.eventId = eventId;
        this.eventPhoto = eventPhoto;
        this.eventName = eventName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean getSelectedStatus() {
        return selectedStatus;
    }

    public void setSelectedStatus(boolean selectedStatus) {
        this.selectedStatus = selectedStatus;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public Uri getEventPhoto() {
        return eventPhoto;
    }

    public void setEventPhoto(Uri eventPhoto) {
        this.eventPhoto = eventPhoto;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

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
