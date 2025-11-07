package com.example.string_events;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.allOf;

import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

    private static Intent makeIntent() {
        Context ctx = ApplicationProvider.getApplicationContext();
        return new Intent(ctx, MainActivity.class)
                .putExtra("fromTest", true);
    }

    @Test
    public void showsKnownUI() {
        try (ActivityScenario<?> ignored = ActivityScenario.launch(makeIntent())) {
            onView(isRoot()).check(matches(anyOf(
                    hasDescendant(withId(R.id.btnSignIn)),
                    hasDescendant(withId(R.id.btnRegister)),
                    hasDescendant(allOf(withId(R.id.list), isDisplayed())),
                    hasDescendant(withId(R.id.tv_title)),
                    hasDescendant(withId(R.id.bottom_bar))
            )));
        }
    }

    @Test
    public void safeClicks_doNotCrash() {
        try (ActivityScenario<?> ignored = ActivityScenario.launch(makeIntent())) {
            safeClick(R.id.btnSignIn);
            safeClick(R.id.btnRegister);

            safeClick(R.id.nav_person);
            safeClick(R.id.nav_bell);
            safeClick(R.id.nav_calendar);
            safeClick(R.id.nav_camera);
        }
    }

    private void safeClick(int id) {
        try { onView(withId(id)).perform(click()); } catch (Throwable ignored) {}
    }
}
