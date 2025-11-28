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
    private String profileImg;   // new field for profile image URL

    /**
     * No-arg constructor required by Firestore for deserialization.
     */
    public AdminProfiles() {
        // Required empty constructor for Firestore
    }

    /**
     * Creates a profile with the provided fields (without image).
     *
     * @param name    display name of the user
     * @param email   account email address
     * @param password stored password value
     * @param docId   Firestore document identifier
     */
    public AdminProfiles(String name, String email, String password, String docId) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.docId = docId;
        this.profileImg = null;
    }

    /**
     * Creates a profile with image URL.
     *
     * @param name       display name of the user
     * @param email      account email address
     * @param password   stored password value
     * @param docId      Firestore document identifier
     * @param profileImg profile image URL from Firestore ("profileimg")
     */
    public AdminProfiles(String name, String email, String password, String docId, String profileImg) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.docId = docId;
        this.profileImg = profileImg;
    }

    public String getName() { return name; }

    public String getEmail() { return email; }

    public String getPassword() { return password; }

    public String getDocId() { return docId; }

    public String getProfileImg() { return profileImg; }
}
