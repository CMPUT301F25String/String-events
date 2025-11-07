package com.example.string_events;

import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class EventsControllerTest {
    // all passed
    @Test
    public void updateTitle_persists_to_repository() {
        InMemoryRepo repo = new InMemoryRepo();
        Event e = TestFactory.event("Old");
        repo.save(e);

        SimpleEventEditor editor = new SimpleEventEditor(repo);
        editor.updateTitle(e.getEventId(), "New");

        Event saved = repo.getById(e.getEventId());
        assertNotNull(saved);
        assertEquals("New", saved.getTitle());
    }

    //Failure path: unknown id
    @Test(expected = NullPointerException.class)
    public void updateTitle_withUnknownId_throws() {
        InMemoryRepo repo = new InMemoryRepo();
        SimpleEventEditor editor = new SimpleEventEditor(repo);
        editor.updateTitle("missing-id", "New");
    }

    //Edge cases: invalid args OR unknown id
    @Test
    public void updateTitle_withInvalidArgs_orUnknownId_returnsFalse_andNoChange() {
        InMemoryRepo repo = new InMemoryRepo();
        Event e = TestFactory.event("Old");
        repo.save(e);

        SafeEventEditor editor = new SafeEventEditor(repo);

        assertFalse(editor.updateTitle(null, "X"));
        assertFalse(editor.updateTitle(e.getEventId(), null));
        assertFalse(editor.updateTitle(e.getEventId(), ""));
        assertFalse(editor.updateTitle("missing", "X"));

        assertEquals("Old", repo.getById(e.getEventId()).getTitle());
    }

    //JVM tests
    static final class InMemoryRepo {
        private final Map<String, Event> store = new HashMap<>();
        Event getById(String id) { return store.get(id); }
        void save(Event e) { store.put(e.getEventId(), e); }
    }

    static final class SimpleEventEditor {
        private final InMemoryRepo repo;
        SimpleEventEditor(InMemoryRepo repo) { this.repo = repo; }
        void updateTitle(String id, String newTitle) {
            Event e = repo.getById(id);
            e.setTitle(newTitle);
            repo.save(e);
        }
    }

    static final class SafeEventEditor {
        private final InMemoryRepo repo;
        SafeEventEditor(InMemoryRepo repo) { this.repo = repo; }
        boolean updateTitle(String id, String newTitle) {
            if (id == null || newTitle == null || newTitle.isEmpty()) return false;
            Event e = repo.getById(id);
            if (e == null) return false;
            e.setTitle(newTitle);
            repo.save(e);
            return true;
        }
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
