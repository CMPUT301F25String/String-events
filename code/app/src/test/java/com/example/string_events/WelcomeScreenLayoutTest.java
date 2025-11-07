package com.example.string_events;

import android.view.View;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertNotNull;

@RunWith(RobolectricTestRunner.class)
public class WelcomeScreenLayoutTest {

    @Test public void welcome_inflates_and_core_ids_exist() {
        View root = TestViews.inflate(R.layout.welcome_screen);
        assertNotNull(root);
        TestViews.assertHas(root, R.id.title, R.id.btnSignIn, R.id.btnRegister);
    }
}
