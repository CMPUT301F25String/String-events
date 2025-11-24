package com.example.string_events;

import android.net.Uri;

import androidx.annotation.NonNull;

public class Notification {
    boolean selectedStatus;
    Uri eventPhoto; // eventPhoto needs to be of type Uri because it is a user uploaded image
    String eventName;

    public Notification(boolean selectedStatus, Uri eventPhoto, String eventName) {
        this.selectedStatus = selectedStatus;
        this.eventPhoto = eventPhoto;
        this.eventName = eventName;
    }

    public boolean getSelectedStatus() {
        return selectedStatus;
    }

    public void setSelectedStatus(boolean selectedStatus) {
        this.selectedStatus = selectedStatus;
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
