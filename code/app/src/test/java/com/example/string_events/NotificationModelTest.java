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

    @Test
    public void messageConstructor_setsMessageFieldsAndDefaultsSelection() {
        // (username, eventId, eventName, eventPhoto, isMessage, messageText)
        Notification n = new Notification(
                "alice",
                "E1",
                "Event A",
                "https://example.com/img.png",
                true,
                "Hello, this is a test message."
        );

        // basic fields
        assertEquals("alice", n.getUsername());
        assertEquals("E1", n.getEventId());
        assertEquals("Event A", n.getEventName());
        assertEquals("https://example.com/img.png", n.getEventPhoto());

        // message-specific fields
        assertTrue(n.isMessage());
        assertEquals("Hello, this is a test message.", n.getMessageText());

        assertFalse(n.getSelectedStatus());
    }

    @Test
    public void messageFlags_canBeUpdatedViaSetters() {
        Notification n = new Notification(
                "bob",
                "E2",
                "Event B",
                null,
                false,
                null
        );

        assertFalse(n.isMessage());
        assertNull(n.getMessageText());

        n.setMessage(true);
        n.setMessageText("Updated message");

        assertTrue(n.isMessage());
        assertEquals("Updated message", n.getMessageText());
    }

}
