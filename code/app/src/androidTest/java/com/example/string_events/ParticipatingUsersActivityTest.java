package com.example.string_events;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.firebase.FirebaseApp;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(AndroidJUnit4.class)
public class ParticipatingUsersActivityTest {

    private static Intent makeIntent() {
        Context ctx = ApplicationProvider.getApplicationContext();
        return new Intent(ctx, ParticipatingUsersActivity.class)
                .putExtra("fromTest", true)
                .putExtra("eventId", "test-event")
                .putExtra("organizerId", "org-123");
    }

    @Before
    public void initFirebase() {
        Context ctx = ApplicationProvider.getApplicationContext();
        if (FirebaseApp.getApps(ctx).isEmpty()) {
            FirebaseApp.initializeApp(ctx);
        }
    }

    @Test
    public void showsCoreWidgets() {
        try (ActivityScenario<ParticipatingUsersActivity> sc =
                     ActivityScenario.launch(makeIntent())) {

            onView(withId(R.id.btnBack)).check(matches(isDisplayed()));
            onView(withId(R.id.tvTitle)).check(matches(isDisplayed()));
            onView(withId(R.id.listParticipating)).check(matches(isDisplayed()));
            onView(withId(R.id.btnSendParticipating)).check(matches(isDisplayed()));
            onView(withId(R.id.btnExportParticipating)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void clicks_doNotCrash() {
        try (ActivityScenario<ParticipatingUsersActivity> sc =
                     ActivityScenario.launch(makeIntent())) {

            onView(withId(R.id.btnSendParticipating)).perform(click());
            onView(withId(R.id.btnExportParticipating)).perform(click());
            onView(withId(R.id.btnBack)).perform(click());
        }
    }
}
