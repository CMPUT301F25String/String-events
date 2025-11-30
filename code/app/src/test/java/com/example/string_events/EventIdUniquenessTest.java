package com.example.string_events;

import org.junit.Test;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

public class EventIdUniquenessTest {

    // Ensures Event IDs are unique across many instances.
    private Event newEvent(int i) {
        ZonedDateTime now = ZonedDateTime.now();
        return new Event(null,
                "creator-" + i,
                "Title " + i,
                null,
                "desc",
                new ArrayList<>(),
                now.plusDays(1),
                now.plusDays(1).plusHours(1),
                "Loc",
                now.minusDays(1),
                now.plusDays(1),
                10,
                100,
                false,
                true
        );
    }

    @Test
    public void ids_are_unique_for_many_events() {
        Set<String> ids = new HashSet<>();
        for (int i = 0; i < 500; i++) {
            Event e = newEvent(i);
            assertTrue(ids.add(e.getEventId()));
        }
        assertEquals(500, ids.size());
    }
}
