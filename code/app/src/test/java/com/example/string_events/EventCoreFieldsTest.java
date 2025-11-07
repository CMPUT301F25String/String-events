package com.example.string_events;

import org.junit.Test;

import java.time.ZonedDateTime;
import java.util.ArrayList;

import static org.junit.Assert.*;

public class EventCoreFieldsTest {

    private Event newEvent() {
        ZonedDateTime now = ZonedDateTime.now();
        return new Event(
                "creator-1",
                "Community Run",
                null, // photo Uri not required for this unit test
                "desc",
                new ArrayList<>(),
                now.plusDays(1),
                now.plusDays(1).plusHours(2),
                "Kinsmen Park",
                now.minusDays(1),
                now.plusDays(1),
                50,
                200,
                true,
                true
        );
    }

    @Test
    public void ctor_generatesNonEmptyId_andInitializesWaitlist() {
        Event e = newEvent();
        assertNotNull(e.getEventId());
        assertTrue(e.getEventId().length() > 0);
        assertNotNull(e.getWaitlist());
        assertEquals(0, e.getWaitlist().size());
    }

    @Test
    public void getters_and_setters_work() {
        Event e = newEvent();

        assertEquals("Community Run", e.getTitle());
        e.setTitle("New Title");
        assertEquals("New Title", e.getTitle());

        assertEquals(50, e.getMaxAttendees());
        e.setMaxAttendees(20);
        assertEquals(20, e.getMaxAttendees());

        assertTrue(e.getGeolocationRequirement());
        e.setGeolocationRequirement(false);
        assertFalse(e.getGeolocationRequirement());

        assertTrue(e.getEventVisibility());
        e.setEventVisibility(false);
        assertFalse(e.getEventVisibility());

        assertEquals("Kinsmen Park", e.getLocation());
        e.setLocation("Quad");
        assertEquals("Quad", e.getLocation());
    }
}
