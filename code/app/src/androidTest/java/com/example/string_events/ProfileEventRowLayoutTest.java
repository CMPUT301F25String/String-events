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

import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(AndroidJUnit4.class)
public class ProfileEventRowLayoutTest {

    private ActivityScenario<UiHostActivity> launchRowLayout() {
        Context ctx = ApplicationProvider.getApplicationContext();
        Intent it = new Intent(ctx, UiHostActivity.class)
                .putExtra(UiHostActivity.EXTRA_LAYOUT_RES_ID, R.layout.item_profile_event);
        return ActivityScenario.launch(it);
    }

    @Test
    public void rowCoreWidgets_areVisible() {
        try (ActivityScenario<UiHostActivity> sc = launchRowLayout()) {
            onView(withId(R.id.profile_background)).check(matches(isDisplayed()));
            onView(withId(R.id.event_image)).check(matches(isDisplayed()));

            onView(withId(R.id.event_name)).check(matches(isDisplayed()));

            onView(withId(R.id.date_image)).check(matches(isDisplayed()));
            onView(withId(R.id.event_date)).check(matches(isDisplayed()));
            onView(withId(R.id.event_time)).check(matches(isDisplayed()));

            onView(withId(R.id.community_centre_image)).check(matches(isDisplayed()));
            onView(withId(R.id.event_location)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void clickImage_andRow_doNotCrash() {
        try (ActivityScenario<UiHostActivity> sc = launchRowLayout()) {
            onView(withId(R.id.event_image)).perform(click());
            onView(withId(R.id.profile_background)).perform(click());
        }
    }
}
