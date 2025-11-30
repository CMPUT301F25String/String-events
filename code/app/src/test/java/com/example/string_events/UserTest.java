package com.example.string_events;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class UserTest {

    @Test
    public void defaultConstructor_initializesFieldsToNullOrZero() {
        User user = new User();

        assertNull(user.getUid());
        assertNull(user.getName());
        assertNull(user.getEmail());
        assertNull(user.getPhone());
        assertNull(user.getRole());
        assertEquals(0L, user.getCreatedAt());
    }

    @Test
    public void setters_updateAllFieldsCorrectly() {
        User user = new User();

        String uid = "uid-123";
        String name = "Alice";
        String email = "alice@example.com";
        String phone = "123-456-7890";
        String role = "admin";
        long createdAt = 1700000000000L;

        user.setUid(uid);
        user.setName(name);
        user.setEmail(email);
        user.setPhone(phone);
        user.setRole(role);
        user.setCreatedAt(createdAt);

        assertEquals(uid, user.getUid());
        assertEquals(name, user.getName());
        assertEquals(email, user.getEmail());
        assertEquals(phone, user.getPhone());
        assertEquals(role, user.getRole());
        assertEquals(createdAt, user.getCreatedAt());
    }
}
