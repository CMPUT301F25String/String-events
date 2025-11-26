package com.example.string_events;

public class Entrant {
    private String name;
    private String email;
    private String uid;

    public Entrant(String name, String email, String uid) {
        this.name = name;
        this.email = email;
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getUid() {
        return uid;
    }
}
