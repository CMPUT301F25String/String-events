package com.example.string_events;

import android.view.View;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertNotNull;

@RunWith(RobolectricTestRunner.class)
public class RegisterScreenLayoutTest {

    @Test public void register_inflates_and_core_ids_exist() {
        View root = TestViews.inflate(R.layout.register_screen);
        assertNotNull(root);
        TestViews.assertHas(
                root,
                R.id.toolbar,
                R.id.etFullName, R.id.etEmail, R.id.etPassword, R.id.etPhone,
                R.id.btnRegister, R.id.btnSignIn
        );
    }
}
