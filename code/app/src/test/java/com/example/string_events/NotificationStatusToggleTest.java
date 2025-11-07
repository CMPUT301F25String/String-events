package com.example.string_events;

import org.junit.Test;
import static org.junit.Assert.*;

public class NotificationStatusToggleTest {

    @Test
    public void selectedStatus_toggle_roundtrip() {
        Notification n = new Notification("alice", false, "N1", null, "Event A");
        assertFalse(n.getSelectedStatus());

        n.setSelectedStatus(true);
        assertTrue(n.getSelectedStatus());

        n.setSelectedStatus(false);
        assertFalse(n.getSelectedStatus());
    }
}
