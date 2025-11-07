package com.example.string_events;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

    @Test
    public void showsKnownUI() {
        Intent i = new Intent(
                androidx.test.core.app.ApplicationProvider.getApplicationContext(),
                UiHostActivity.class
        );
        i.putExtra(UiHostActivity.EXTRA_LAYOUT_RES_ID, R.layout.welcome_screen);

        try (ActivityScenario<UiHostActivity> sc = ActivityScenario.launch(i)) {
            onView(withId(R.id.btnSignIn)).check(matches(isDisplayed()));
            onView(withId(R.id.btnRegister)).check(matches(isDisplayed()));
        }
    }
}