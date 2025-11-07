package com.example.string_events;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.firebase.FirebaseApp;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class EventDetailActivityTest {

    private static Intent makeIntent() {
        Context ctx = ApplicationProvider.getApplicationContext();
        return new Intent(ctx, EventDetailActivity.class)
                .putExtra("fromTest", true)
                .putExtra("event_id", "test-event");
    }

    @Before
    public void setup() {
        Context ctx = ApplicationProvider.getApplicationContext();
        if (FirebaseApp.getApps(ctx).isEmpty()) FirebaseApp.initializeApp(ctx);
        SharedPreferences sp = ctx.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        sp.edit().putString("user", "ui-tester").apply();
    }

    private static ViewAction waitForWindowFocus() {
        return new ViewAction() {
            @Override public Matcher<View> getConstraints() { return isRoot(); }
            @Override public String getDescription() { return "wait for window focus"; }
            @Override public void perform(UiController ui, View view) {
                long end = System.currentTimeMillis() + (long) 4000;
                while (!view.getRootView().hasWindowFocus() && System.currentTimeMillis() < end) {
                    ui.loopMainThreadForAtLeast(50);
                }
            }
        };
    }
    private static ViewAction waitForIdle() {
        return new ViewAction() {
            @Override public Matcher<View> getConstraints() { return isRoot(); }
            @Override public String getDescription() { return "wait for idle"; }
            @Override public void perform(UiController ui, View view) {
                ui.loopMainThreadForAtLeast(200);
            }
        };
    }
    private void ensureReady() {
        onView(isRoot()).perform(waitForWindowFocus());
        onView(isRoot()).perform(waitForIdle());
    }

    @Test
    public void showsCoreWidgets() {
        try (ActivityScenario<EventDetailActivity> sc = ActivityScenario.launch(makeIntent())) {
            ensureReady();
            onView(withId(R.id.back_button)).check(matches(isDisplayed()));
            onView(withId(R.id.tvEventTitle)).check(matches(isDisplayed()));
            onView(withId(R.id.tvLocation)).check(matches(isDisplayed()));
            onView(withId(R.id.ivEventImage)).check(matches(isDisplayed()));
            onView(withId(R.id.spots_taken)).check(matches(isDisplayed()));
            onView(withId(R.id.waiting_list)).check(matches(isDisplayed()));
            onView(withId(R.id.tvDateLine)).check(matches(isDisplayed()));
            onView(withId(R.id.tvTimeLine)).check(matches(isDisplayed()));
            onView(withId(R.id.tvAddress)).check(matches(isDisplayed()));
            onView(withId(R.id.tvDescription)).check(matches(isDisplayed()));
            onView(withId(R.id.apply_button)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void backButton_doesNotCrash() {
        try (ActivityScenario<EventDetailActivity> sc = ActivityScenario.launch(makeIntent())) {
            ensureReady();
            onView(withId(R.id.back_button)).perform(click());
        }
    }

    @Test
    public void applyButton_doesNotCrash() {
        try (ActivityScenario<EventDetailActivity> sc = ActivityScenario.launch(makeIntent())) {
            ensureReady();
            onView(withId(R.id.apply_button)).perform(click());
            onView(isRoot()).perform(waitForIdle());
        }
    }
}
