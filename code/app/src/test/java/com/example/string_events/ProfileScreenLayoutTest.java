package com.example.string_events;

import android.view.View;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertNotNull;

@RunWith(RobolectricTestRunner.class)
public class ProfileScreenLayoutTest {

    @Test public void profile_inflates_and_core_ids_exist() {
        View root = TestViews.inflate(R.layout.profile_screen);
        assertNotNull(root);
        TestViews.assertHas(
                root,
                R.id.notification_switch,
                R.id.profile_events_recyclerView,
                R.id.btnHome, R.id.btnNotification, R.id.btnProfile,
                R.id.delete_profile_button,
                R.id.name_textView, R.id.email_textView,
                R.id.switch_roles_button
        );
    }
}
