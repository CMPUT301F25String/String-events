
package com.example.string_events;

public class User {
    private String uid;
    private String name;
    private String email;
    private String phone;
    private String role;
    private long createdAt;

    public User() {}

    public User(String uid, String name, String email, String phone, String role, long createdAt) {
        this.uid = uid; this.name = name; this.email = email; this.phone = phone;
        this.role = role; this.createdAt = createdAt;
    }

    public String getUid() { return uid; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getRole() { return role; }
    public long getCreatedAt() { return createdAt; }

    public void setUid(String uid) { this.uid = uid; }
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setRole(String role) { this.role = role; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
