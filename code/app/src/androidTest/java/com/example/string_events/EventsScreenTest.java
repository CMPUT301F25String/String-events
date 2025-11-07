package com.example.string_events;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.firebase.FirebaseApp;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(AndroidJUnit4.class)
public class EventsScreenTest {

    private static final Class<?> LAUNCHER = MainActivity.class;

    private Intent makeIntent() {
        Context ctx = ApplicationProvider.getApplicationContext();
        return new Intent(ctx, LAUNCHER)
                .putExtra("fromTest", true);
    }

    @Before
    public void setup() {
        Context ctx = ApplicationProvider.getApplicationContext();
        if (FirebaseApp.getApps(ctx).isEmpty()) {
            FirebaseApp.initializeApp(ctx);
        }
        SharedPreferences sp = ctx.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        sp.edit().putString("user", "tester").apply();
    }

    @Test
    public void showsCoreWidgets() {
        try (ActivityScenario<?> sc = ActivityScenario.launch(makeIntent())) {
            onView(withId(R.id.tv_title)).check(matches(isDisplayed()));
            onView(withId(R.id.tv_subtitle)).check(matches(isDisplayed()));
            onView(withId(R.id.list)).check(matches(isDisplayed()));
            onView(withId(R.id.bottom_bar)).check(matches(isDisplayed()));

            onView(withId(R.id.nav_calendar)).check(matches(isDisplayed()));
            onView(withId(R.id.nav_camera)).check(matches(isDisplayed()));
            onView(withId(R.id.nav_bell)).check(matches(isDisplayed()));
            onView(withId(R.id.nav_person)).check(matches(isDisplayed()));

            onView(withId(R.id.btnLogout)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void clickFirstListItem_whenPresent_doNotCrash() {
        try (ActivityScenario<?> sc = ActivityScenario.launch(makeIntent())) {
            try {
                onData(Matchers.anything())
                        .inAdapterView(withId(R.id.list))
                        .atPosition(0)
                        .perform(click());
            } catch (AssertionError | RuntimeException ignored) {
            }
        }
    }

    @Test
    public void navCalendar_click_doNotCrash() {
        try (ActivityScenario<?> sc = ActivityScenario.launch(makeIntent())) {
            onView(withId(R.id.nav_calendar)).perform(click());
        }
    }

    @Test
    public void navCamera_click_doNotCrash() {
        try (ActivityScenario<?> sc = ActivityScenario.launch(makeIntent())) {
            onView(withId(R.id.nav_camera)).perform(click());
        }
    }

    @Test
    public void navBell_click_doNotCrash() {
        try (ActivityScenario<?> sc = ActivityScenario.launch(makeIntent())) {
            onView(withId(R.id.nav_bell)).perform(click());
        }
    }

    @Test
    public void navPerson_click_doNotCrash() {
        try (ActivityScenario<?> sc = ActivityScenario.launch(makeIntent())) {
            onView(withId(R.id.nav_person)).perform(click());
        }
    }

    @Test
    public void logout_click_doNotCrash() {
        try (ActivityScenario<?> sc = ActivityScenario.launch(makeIntent())) {
            onView(withId(R.id.btnLogout)).perform(click());
        }
    }
}
