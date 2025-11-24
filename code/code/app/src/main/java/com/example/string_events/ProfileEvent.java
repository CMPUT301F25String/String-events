package com.example.string_events;

import android.widget.TextView;

public class ProfileEvent {
    int profileImage;
    TextView profileName;
    TextView profileEmail;
    boolean getNotifications;

    public ProfileEvent(int profileImage, TextView profileName, TextView profileEmail, boolean getNotifications) {
        this.profileImage = profileImage;
        this.profileName = profileName;
        this.profileEmail = profileEmail;
        this.getNotifications = getNotifications;
    }

    public int getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(int profileImage) {
        this.profileImage = profileImage;
    }

    public TextView getProfileName() {
        return profileName;
    }

    public void setProfileName(TextView profileName) {
        this.profileName = profileName;
    }

    public TextView getProfileEmail() {
        return profileEmail;
    }

    public void setProfileEmail(TextView profileEmail) {
        this.profileEmail = profileEmail;
    }

    public boolean isGetNotifications() {
        return getNotifications;
    }

    public void setNotifications(boolean getNotifications) {
        this.getNotifications = getNotifications;
    }
}
