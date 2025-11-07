package com.example.string_events;

import org.junit.Test;
import static org.junit.Assert.*;

public class NotificationModelTest {

    @Test
    public void ctor_and_getters_setters_work() {
        // (username, selectedStatus, eventId, eventPhoto, eventName)
        Notification n = new Notification("alice", false, "N1", null, "Event A");

        assertFalse(n.getSelectedStatus());
        assertNull(n.getEventPhoto());
        assertEquals("Event A", n.getEventName());

        n.setSelectedStatus(true);
        n.setEventName("Event B");

        assertTrue(n.getSelectedStatus());
        assertEquals("Event B", n.getEventName());
    }

    @Test
    public void toString_contains_key_fields() {
        Notification n = new Notification("bob", true, "N2", null, "Event Z");
        String s = n.toString();
        assertTrue(s.contains("selectedStatus"));
        assertTrue(s.contains("Event Z"));
    }
}

