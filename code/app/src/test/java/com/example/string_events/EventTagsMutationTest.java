package com.example.string_events;

import org.junit.Test;
import java.util.ArrayList;
import java.util.Arrays;
import java.time.ZonedDateTime;
import static org.junit.Assert.*;

public class EventTagsMutationTest {

    private static Event newEvent() {
        ZonedDateTime now = ZonedDateTime.now();
        return new Event(null,
                "creator","T",null,"d", new ArrayList<>(),
                now.plusDays(1), now.plusDays(2),
                "Loc",
                now.minusDays(1), now.plusDays(1),
                10, 100, false, true
        );
    }

    @Test
    public void setTags_replaces_and_reflects_updates() {
        Event e = newEvent();

        ArrayList<String> tags = new ArrayList<>(Arrays.asList("sports","outdoor"));
        e.setTags(tags);
        assertEquals(Arrays.asList("sports","outdoor"), e.getTags());

        // Document current behavior: external list mutation is reflected.
        tags.add("fun");
        assertEquals(Arrays.asList("sports","outdoor","fun"), e.getTags());
    }
}
