package com.example.string_events;

/**
 * Simple user profile model with basic contact info and role metadata.
 */
public class User {
    private String uid;
    private String name;
    private String email;
    private String phone;
    private String role;
    private long createdAt;

    /** Required empty constructor (e.g., for Firestore/serialization). */
    public User() {}

    /**
     * Creates a new user profile.
     *
     * @param uid        unique user identifier
     * @param name       full name
     * @param email      email address
     * @param phone      phone number
     * @param role       user role (e.g., "user", "admin")
     * @param createdAt  creation timestamp (epoch millis)
     */
    public User(String uid, String name, String email, String phone, String role, long createdAt) {
        this.uid = uid; this.name = name; this.email = email; this.phone = phone;
        this.role = role; this.createdAt = createdAt;
    }

    /** @return unique user identifier */
    public String getUid() { return uid; }

    /** @return full name */
    public String getName() { return name; }

    /** @return email address */
    public String getEmail() { return email; }

    /** @return phone number */
    public String getPhone() { return phone; }

    /** @return user role */
    public String getRole() { return role; }

    /** @return creation time in epoch millis */
    public long getCreatedAt() { return createdAt; }

    /** @param uid unique user identifier */
    public void setUid(String uid) { this.uid = uid; }

    /** @param name full name */
    public void setName(String name) { this.name = name; }

    /** @param email email address */
    public void setEmail(String email) { this.email = email; }

    /** @param phone phone number */
    public void setPhone(String phone) { this.phone = phone; }

    /** @param role user role (e.g., "user", "admin") */
    public void setRole(String role) { this.role = role; }

    /** @param createdAt creation time (epoch millis) */
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
