package com.example.string_events;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class UserItemTest {

    @Test
    public void constructor_setsFieldsCorrectly_whenStatusNotNull() {
        UserItem item = new UserItem(
                "bobUser",
                "Bob",
                "bob@example.com",
                UserItem.Status.WAITLIST
        );

        assertEquals("Bob", item.getName());
        assertEquals("bob@example.com", item.getEmail());
        assertEquals(UserItem.Status.WAITLIST, item.getStatus());
    }

    @Test
    public void constructor_nullStatus_defaultsToNone() {
        UserItem item = new UserItem(
                "charlieUser",
                "Charlie",
                "charlie@example.com",
                null
        );

        assertEquals("Charlie", item.getName());
        assertEquals("charlie@example.com", item.getEmail());
        assertEquals(UserItem.Status.NONE, item.getStatus());
    }

    @Test
    public void constructor_allowsEmptyStrings_withParticipatingStatus() {
        UserItem item = new UserItem(
                "",
                "",
                "",
                UserItem.Status.PARTICIPATING
        );

        assertEquals("", item.getName());
        assertEquals("", item.getEmail());
        assertEquals(UserItem.Status.PARTICIPATING, item.getStatus());
    }
}
