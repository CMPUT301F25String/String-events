package com.example.string_events;

public class UserItem {
    public enum Status {
        CANCELED, PARTICIPATING, WAITLIST, INVITED, NONE
    }

    private final String name;
    private final String email;
    private final Status status;

    public UserItem(String name, String email, Status status) {
        this.name = name;
        this.email = email;
        this.status = status == null ? Status.NONE : status;
    }

    public String getName() { return name; }
    public String getEmail() { return email; }
    public Status getStatus() { return status; }
}


