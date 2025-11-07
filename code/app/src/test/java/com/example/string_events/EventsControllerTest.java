package com.example.string_events;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;

public class EventsControllerTest {

    @Test
    public void updateTitle_persists() {
        InMemoryRepo repo = new InMemoryRepo();
        Event e = TestFactory.event("Old");
        repo.save(e);

        EventEditor editor = new EventEditor(repo);
        editor.updateTitle(e.getEventId(), "New");

        assertEquals("New", repo.getById(e.getEventId()).getTitle());
    }

    // Minimal controller + repo for unit test
    static final class EventEditor {
        private final InMemoryRepo repo;
        EventEditor(InMemoryRepo repo) { this.repo = repo; }
        void updateTitle(String id, String title) {
            Event e = repo.getById(id);
            e.setTitle(title);
            repo.save(e);
        }
    }

    static final class InMemoryRepo {
        private final Map<String, Event> store = new HashMap<>();
        Event getById(String id) { return store.get(id); }
        void save(Event e) { store.put(e.getEventId(), e); }
    }

    static final class TestFactory {
        static Event event(String title) {
            java.time.ZonedDateTime now = java.time.ZonedDateTime.now();
            return new Event(
                    "creator", title, null, "desc", new ArrayList<>(),
                    now.plusDays(1), now.plusDays(1).plusHours(1), "Loc",
                    now.minusDays(1), now.plusDays(1),
                    10, 100, false, true
            );
        }
    }
}
