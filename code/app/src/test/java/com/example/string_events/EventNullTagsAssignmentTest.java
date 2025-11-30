package com.example.string_events;

import org.junit.Test;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import static org.junit.Assert.*;

public class EventNullTagsAssignmentTest {

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
    public void tags_canBeSetNull_thenReplacedWithNewList() {
        Event e = newEvent();
        // set to null (document current behavior)
        e.setTags(null);
        assertNull(e.getTags());

        // replace with a new list
        ArrayList<String> tags = new ArrayList<>(Arrays.asList("music","indoor"));
        e.setTags(tags);
        assertEquals(Arrays.asList("music","indoor"), e.getTags());
    }
}
