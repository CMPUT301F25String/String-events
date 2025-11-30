package com.example.string_events;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.google.firebase.Timestamp;

import org.junit.Test;

import java.util.Date;

public class OrganizerEventModelTest {

    @Test
    public void defaultConstructor_initializesFieldsToDefaults() {
        OrganizerEvent e = new OrganizerEvent();

        // String fields should default to null
        assertNull(e.id);
        assertNull(e.title);
        assertNull(e.location);
        assertNull(e.imageUrl);
        assertNull(e.creator);

        // Timestamp field should default to null
        assertNull(e.startAt);

        // int fields should default to 0
        assertEquals(0, e.maxAttendees);
        assertEquals(0, e.attendeesCount);
    }

    @Test
    public void fields_canBeMutatedDirectly() {
        OrganizerEvent e = new OrganizerEvent();

        Timestamp ts = new Timestamp(new Date());

        e.id = "event-xyz";
        e.title = "Organizer Test Event";
        e.location = "Edmonton, AB";
        e.startAt = ts;
        e.maxAttendees = 150;
        e.attendeesCount = 42;
        e.imageUrl = "https://example.com/cover.png";
        e.creator = "organizerUser";

        assertEquals("event-xyz", e.id);
        assertEquals("Organizer Test Event", e.title);
        assertEquals("Edmonton, AB", e.location);
        assertEquals(ts, e.startAt);
        assertEquals(150, e.maxAttendees);
        assertEquals(42, e.attendeesCount);
        assertEquals("https://example.com/cover.png", e.imageUrl);
        assertEquals("organizerUser", e.creator);
    }
}
