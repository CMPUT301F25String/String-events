package com.example.string_events;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Field;
import java.time.ZonedDateTime;

import org.junit.Test;

public class ProfileEventModelTest {

    private Object getPrivateField(ProfileEvent event, String fieldName) {
        try {
            Field f = ProfileEvent.class.getDeclaredField(fieldName);
            f.setAccessible(true);
            return f.get(event);
        } catch (Exception e) {
            throw new AssertionError("Failed to access field: " + fieldName, e);
        }
    }

    @Test
    public void constructor_setsCoreFieldsCorrectly() {
        ZonedDateTime start = ZonedDateTime.parse("2025-01-01T10:00:00Z");
        ZonedDateTime end   = ZonedDateTime.parse("2025-01-01T12:30:00Z");

        ProfileEvent event = new ProfileEvent(
                "event-123",
                "https://example.com/photo.png",
                "My Test Event",
                start,
                end,
                "Edmonton, AB"
        );

        assertEquals("event-123", event.getEventId());
        assertEquals("Edmonton, AB", event.getProfileEventLocation());

        assertEquals("https://example.com/photo.png",
                getPrivateField(event, "profileEventPhotoUrl"));
        assertEquals("My Test Event",
                getPrivateField(event, "profileEventName"));
        assertEquals(start,
                getPrivateField(event, "profileEventStartDateTime"));
        assertEquals(end,
                getPrivateField(event, "profileEventEndDateTime"));
    }

    @Test
    public void setProfileEventLocation_updatesLocationField() {
        ZonedDateTime start = ZonedDateTime.parse("2025-02-01T09:00:00Z");
        ZonedDateTime end   = ZonedDateTime.parse("2025-02-01T11:00:00Z");

        ProfileEvent event = new ProfileEvent(
                "event-456",
                "https://example.com/other.png",
                "Another Event",
                start,
                end,
                "Calgary, AB"
        );

        event.setProfileEventLocation("Vancouver, BC");

        assertEquals("Vancouver, BC", event.getProfileEventLocation());
        assertEquals("Vancouver, BC",
                getPrivateField(event, "profileEventLocation"));
    }

    @Test
    public void getEventId_returnsUnderlyingProfileEventId() {
        ZonedDateTime start = ZonedDateTime.parse("2025-03-01T09:00:00Z");
        ZonedDateTime end   = ZonedDateTime.parse("2025-03-01T10:00:00Z");

        ProfileEvent event = new ProfileEvent(
                "event-789",
                "https://example.com/third.png",
                "Third Event",
                start,
                end,
                "Toronto, ON"
        );

        assertEquals("event-789", event.getEventId());
    }
}
