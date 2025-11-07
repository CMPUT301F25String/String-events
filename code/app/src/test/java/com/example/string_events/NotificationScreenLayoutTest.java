package com.example.string_events;

import android.view.View;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertNotNull;

@RunWith(RobolectricTestRunner.class)
public class NotificationScreenLayoutTest {

    @Test public void notification_inflates_and_recycler_exists() {
        View root = TestViews.inflate(R.layout.notification_screen);
        assertNotNull(root);
        TestViews.assertHas(root, R.id.notifications_recyclerView);
    }
}
