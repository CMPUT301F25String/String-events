package com.example.string_events;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isClickable;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withHint;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * UI tests for activity_event_filter.xml
 * - Only modifies test code, no manifest/app changes.
 * - Launches app's launcher Activity then replaces contentView with the layout under test.
 */
@RunWith(AndroidJUnit4.class)
public class EventFilterActivityTest {

    /** Launch launcher activity and set our layout as content. */
    private ActivityScenario<? extends Activity> launchWithLayout() {
        Context ctx = ApplicationProvider.getApplicationContext();
        Intent launch = ctx.getPackageManager().getLaunchIntentForPackage(ctx.getPackageName());
        if (launch == null) launch = new Intent(Intent.ACTION_MAIN);
        launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        ActivityScenario<? extends Activity> scenario = ActivityScenario.launch(launch);
        scenario.onActivity(a -> a.setContentView(R.layout.activity_event_filter));
        return scenario;
    }

    @Test
    public void views_areDisplayed_test() {
        try (ActivityScenario<? extends Activity> sc = launchWithLayout()) {
            // Header
            onView(withId(R.id.header_container)).check(matches(isDisplayed()));
            onView(withId(R.id.btnBack)).check(matches(isDisplayed()));
            onView(withId(R.id.tvTitle)).check(matches(isDisplayed()));

            // Interests chips (they are TextViews in this layout)
            onView(withId(R.id.chip_badminton_f)).check(matches(isDisplayed()));
            onView(withId(R.id.chip_games_f)).check(matches(isDisplayed()));
            onView(withId(R.id.chip_arts_f)).check(matches(isDisplayed()));
            onView(withId(R.id.chip_learning_f)).check(matches(isDisplayed()));

            // Time card + inputs
            onView(withId(R.id.time_filter_card)).perform(scrollTo()).check(matches(isDisplayed()));
            onView(withId(R.id.et_start_time)).check(matches(isDisplayed()));
            onView(withId(R.id.et_end_time)).check(matches(isDisplayed()));

            // Bottom action bar + buttons
            onView(withId(R.id.bottom_action_bar)).check(matches(isDisplayed()));
            onView(withId(R.id.btn_clear)).check(matches(isDisplayed()));
            onView(withId(R.id.btn_apply)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void texts_areCorrect_test() {
        try (ActivityScenario<? extends Activity> sc = launchWithLayout()) {
            onView(withId(R.id.tvTitle)).check(matches(withText("Filters")));
            onView(withId(R.id.chip_badminton_f)).check(matches(withText("Badminton")));
            onView(withId(R.id.chip_games_f)).check(matches(withText("Games")));
            onView(withId(R.id.chip_arts_f)).check(matches(withText("Arts")));
            onView(withId(R.id.chip_learning_f)).check(matches(withText("Learning")));
            onView(withId(R.id.et_start_time)).check(matches(withHint("Select Date  Time")));
            onView(withId(R.id.et_end_time)).check(matches(withHint("Select Date  Time")));
            onView(withId(R.id.btn_clear)).check(matches(withText("Clear")));
            onView(withId(R.id.btn_apply)).check(matches(withText("Apply")));
        }
    }

    @Test
    public void buttons_and_inputs_areClickable_test() {
        try (ActivityScenario<? extends Activity> sc = launchWithLayout()) {
            onView(withId(R.id.btnBack)).check(matches(isClickable()));
            onView(withId(R.id.btn_clear)).check(matches(isClickable()));
            onView(withId(R.id.btn_apply)).check(matches(isClickable()));

            // EditTexts are clickable (focusable=false in XML), clicking should not crash
            onView(withId(R.id.et_start_time)).perform(click());
            onView(withId(R.id.et_end_time)).perform(click());
        }
    }

    @Test
    public void scrollTo_timeCard_and_click_apply_noCrash_test() {
        try (ActivityScenario<? extends Activity> sc = launchWithLayout()) {
            onView(withId(R.id.time_filter_card)).perform(scrollTo());
            onView(withId(R.id.btn_apply)).perform(click());
            // No explicit assertion needed; success == no crash
        }
    }
}

