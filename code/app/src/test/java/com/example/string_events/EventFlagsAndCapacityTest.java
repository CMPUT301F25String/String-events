package com.example.string_events;

import org.junit.Test;
import java.util.ArrayList;
import java.time.ZonedDateTime;
import static org.junit.Assert.*;

public class EventFlagsAndCapacityTest {

    private static Event newEvent() {
        ZonedDateTime now = ZonedDateTime.now();
        return new Event(
                "creator","T",null,"d", new ArrayList<>(),
                now.plusDays(1), now.plusDays(2),
                "Loc",
                now.minusDays(1), now.plusDays(1),
                50, 200, true, true
        );
    }

    @Test
    public void visibility_and_geolocation_flags_toggle() {
        Event e = newEvent();
        assertTrue(e.getEventVisibility());
        assertTrue(e.getGeolocationRequirement());

        e.setEventVisibility(false);
        e.setGeolocationRequirement(false);

        assertFalse(e.getEventVisibility());
        assertFalse(e.getGeolocationRequirement());
    }

    @Test
    public void capacity_and_waitlist_limit_update() {
        Event e = newEvent();

        assertEquals(50, e.getMaxAttendees());
        assertEquals(200, e.getWaitlistLimit());

        e.setMaxAttendees(30);
        e.setWaitlistLimit(120);

        assertEquals(30, e.getMaxAttendees());
        assertEquals(120, e.getWaitlistLimit());
    }
}
