package com.example.string_events;

/**
 * Data model representing a user profile managed by admins.
 * <p>
 * Instances are typically populated from Firestore and exposed via getters.
 */
public class AdminProfiles {
    private String name;
    private String email;
    private String password;
    private String docId;

    /**
     * No-arg constructor required by Firestore for deserialization.
     */
    public AdminProfiles() {
        // Required empty constructor for Firestore
    }

    /**
     * Creates a profile with the provided fields.
     *
     * @param name   display name of the user
     * @param email  account email address
     * @param password stored password value
     * @param docId  Firestore document identifier
     */
    public AdminProfiles(String name, String email, String password, String docId) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.docId = docId;
    }

    /**
     * @return the display name
     */
    public String getName() { return name; }

    /**
     * @return the email address
     */
    public String getEmail() { return email; }

    /**
     * @return the stored password value
     */
    public String getPassword() { return password; }

    /**
     * @return the Firestore document ID
     */
    public String getDocId() { return docId; }
}
