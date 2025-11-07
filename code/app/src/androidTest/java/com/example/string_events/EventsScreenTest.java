package com.example.string_events;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class EventsScreenTest {

    private Intent makeIntent() {
        Intent i = new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class);
        i.putExtra("role", "user");
        i.putExtra("user", "tester");
        i.putExtra("fullName", "Test User");
        return i;
    }

    @Test
    public void launch_showsCoreViews() {
        try (ActivityScenario<?> sc = ActivityScenario.launch(makeIntent())) {
            onView(withId(R.id.header_container)).check(matches(isDisplayed()));
            onView(withId(R.id.title_bg)).check(matches(isDisplayed()));
            onView(withId(R.id.tv_title)).check(matches(isDisplayed()));
            onView(withId(R.id.list)).check(matches(isDisplayed()));
            onView(withId(R.id.bottom_bar)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void navCalendar_click_noCrash() {
        try (ActivityScenario<?> sc = ActivityScenario.launch(makeIntent())) {
            onView(withId(R.id.nav_calendar)).perform(click());
        }
    }

    @Test
    public void navCamera_click_noCrash() {
        try (ActivityScenario<?> sc = ActivityScenario.launch(makeIntent())) {
            onView(withId(R.id.nav_camera)).perform(click());
        }
    }

    @Test
    public void navBell_click_noCrash() {
        try (ActivityScenario<?> sc = ActivityScenario.launch(makeIntent())) {
            onView(withId(R.id.nav_bell)).perform(click());
        }
    }

    @Test
    public void navPerson_click_noCrash() {
        try (ActivityScenario<?> sc = ActivityScenario.launch(makeIntent())) {
            onView(withId(R.id.nav_person)).perform(click());
        }
    }

    @Test
    public void logout_click_noCrash() {
        try (ActivityScenario<?> sc = ActivityScenario.launch(makeIntent())) {
            onView(withId(R.id.btnLogout)).check(matches(isDisplayed()));
            onView(withId(R.id.btnLogout)).perform(click());
        }
    }
}
