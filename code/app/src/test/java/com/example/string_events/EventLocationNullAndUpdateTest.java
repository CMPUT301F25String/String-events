package com.example.string_events;

import org.junit.Test;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import static org.junit.Assert.*;

public class EventLocationNullAndUpdateTest {

    private static Event newEvent() {
        ZonedDateTime now = ZonedDateTime.now();
        return new Event(null,
                "creator","T",null,"d", new ArrayList<>(),
                now.plusDays(1), now.plusDays(2),
                "Loc-A",
                now.minusDays(1), now.plusDays(1),
                10, 100, false, true
        );
    }

    @Test
    public void setLocation_allowsNull_thenUpdateToNonNull() {
        Event e = newEvent();
        assertEquals("Loc-A", e.getLocation());

        e.setLocation(null);
        assertNull(e.getLocation());

        e.setLocation("Loc-B");
        assertEquals("Loc-B", e.getLocation());
    }
}
