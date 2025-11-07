package com.example.string_events;

import org.junit.Test;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import static org.junit.Assert.*;

public class EventTimeFieldsTest {

    private static Event newEvent() {
        ZonedDateTime now = ZonedDateTime.now();
        return new Event(
                "creator","T",null,"d", new ArrayList<>(),
                now.plusDays(1), now.plusDays(2),
                "Loc",
                now.minusDays(1), now.plusDays(1),
                10, 100, false, true
        );
    }

    @Test
    public void start_end_and_registration_times_roundtrip() {
        Event e = newEvent();

        ZonedDateTime s = ZonedDateTime.now().plusDays(3);
        ZonedDateTime eTime = s.plusHours(4);
        ZonedDateTime rStart = ZonedDateTime.now().minusDays(2);
        ZonedDateTime rEnd = ZonedDateTime.now().plusDays(5);

        e.setStartDateTime(s);
        e.setEndDateTime(eTime);
        e.setRegistrationStartDateTime(rStart);
        e.setRegistrationEndDateTime(rEnd);

        assertEquals(s, e.getStartDateTime());
        assertEquals(eTime, e.getEndDateTime());
        assertEquals(rStart, e.getRegistrationStartDateTime());
        assertEquals(rEnd, e.getRegistrationEndDateTime());
    }
}
