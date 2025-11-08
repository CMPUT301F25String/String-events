package com.example.string_events;

/**
 * Simple immutable view-model for an admin-facing event card.
 * <p>
 * Holds display-only fields such as title, time, location, organizer, status,
 * and drawable resource IDs for cover and location logo.
 *
 * @since 1.0
 */
public class AdminEvent {

    /**
     * Event lifecycle status used for admin display.
     */
    public enum Status { IN_PROGRESS, SCHEDULED, FINISHED }

    private final String title;
    private final String time;
    private final String location;
    private final String organizer;
    private final Status status;
    private final int coverResId;
    private final int locationLogoResId;

    /**
     * Creates an admin event card model.
     *
     * @param title            event title (non-null)
     * @param time             human-readable time string
     * @param location         venue/location text
     * @param organizer        organizer label
     * @param status           lifecycle status to show
     * @param coverResId       drawable resource id for the cover image
     * @param locationLogoResId drawable resource id for a small location/logo icon
     */
    public AdminEvent(String title,
                      String time,
                      String location,
                      String organizer,
                      Status status,
                      int coverResId,
                      int locationLogoResId) {
        this.title = title;
        this.time = time;
        this.location = location;
        this.organizer = organizer;
        this.status = status;
        this.coverResId = coverResId;
        this.locationLogoResId = locationLogoResId;
    }

    /** @return event title */
    public String getTitle() { return title; }

    /** @return formatted time string */
    public String getTime() { return time; }

    /** @return location text */
    public String getLocation() { return location; }

    /** @return organizer label */
    public String getOrganizer() { return organizer; }

    /** @return lifecycle status */
    public Status getStatus() { return status; }

    /** @return cover image drawable resource id */
    public int getCoverResId() { return coverResId; }

    /** @return location/logo drawable resource id */
    public int getLocationLogoResId() { return locationLogoResId; }
}
