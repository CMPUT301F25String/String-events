package com.example.string_events;

public class AdminProfiles {
    private String name;
    private String email;
    private String password;
    private String docId;

    public AdminProfiles() {
        // Required empty constructor for Firestore
    }

    public AdminProfiles(String name, String email, String password, String docId) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.docId = docId;
    }

    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getDocId() { return docId; }
}
