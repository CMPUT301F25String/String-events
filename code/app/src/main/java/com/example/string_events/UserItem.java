package com.example.string_events;

/**
 * Lightweight row model representing a user with an optional participation status.
 */
public class UserItem {

    /**
     * Participation/status values used for list badges.
     */
    public enum Status {
        CANCELED, PARTICIPATING, WAITLIST, INVITED, NONE
    }

    private final String name;
    private final String email;
    private final Status status;

    /**
     * Creates a new {@code UserItem}.
     *
     * @param name   display name
     * @param email  email address
     * @param status participation status (defaults to {@link Status#NONE} if {@code null})
     */
    public UserItem(String name, String email, Status status) {
        this.name = name;
        this.email = email;
        this.status = status == null ? Status.NONE : status;
    }

    /** @return display name */
    public String getName() { return name; }

    /** @return email address */
    public String getEmail() { return email; }

    /** @return participation status */
    public Status getStatus() { return status; }
}
