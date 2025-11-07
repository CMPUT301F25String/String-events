package com.example.string_events;

import android.view.View;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertNotNull;

@RunWith(RobolectricTestRunner.class)
public class OrganizerScreensLayoutTest {

    @Test public void org_event_details_inflates() {
        View root = TestViews.inflate(R.layout.org_event_details);
        assertNotNull(root);
    }

    @Test public void org_events_screen_inflates() {
        View root = TestViews.inflate(R.layout.org_events_screen);
        assertNotNull(root);
    }
}
