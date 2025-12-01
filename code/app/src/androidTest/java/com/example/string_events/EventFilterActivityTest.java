package com.example.string_events;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.swipeUp;
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
import androidx.test.espresso.PerformException;
import androidx.test.espresso.action.ViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
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

    public static Intent newIntent() {
        // Using ApplicationProvider.getApplicationContext() is the standard way
        // to get a context in an instrumented test.
        Context ctx = ApplicationProvider.getApplicationContext();
        Intent intent = new Intent(ctx, EventFilterActivity.class);

        intent.putExtra("event_id", "e299ee34-3977-491e-8f2b-0ce30bd7e447");
        intent.putExtra("action", "filter");
        return intent;
    }

    @Rule
    public ActivityScenarioRule<EventFilterActivity> scenario = new ActivityScenarioRule<>(newIntent());

    @Test
    public void views_areDisplayed_test() {
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
//        }
    }

    @Test
    public void texts_areCorrect_test() {
            onView(withId(R.id.tvTitle)).check(matches(withText("Filters")));
            onView(withId(R.id.chip_badminton_f)).check(matches(withText("Badminton")));
            onView(withId(R.id.chip_games_f)).check(matches(withText("Games")));
            onView(withId(R.id.chip_arts_f)).check(matches(withText("Arts")));
            onView(withId(R.id.chip_learning_f)).check(matches(withText("Learning")));
            onView(withId(R.id.et_start_time)).check(matches(withHint("Select Date and Time")));
            onView(withId(R.id.et_end_time)).check(matches(withHint("Select Date and Time")));
            onView(withId(R.id.btn_clear)).check(matches(withText("Clear")));
            onView(withId(R.id.btn_apply)).check(matches(withText("Apply")));
//        }
    }

    @Test
    public void buttons_and_inputs_areClickable_test() {
            onView(withId(R.id.btnBack)).check(matches(isClickable()));
            onView(withId(R.id.btn_clear)).check(matches(isClickable()));
            onView(withId(R.id.btn_apply)).check(matches(isClickable()));
            onView(withId(R.id.et_start_time)).perform(click());
    }

    @Test
    public void scrollTo_timeCard_and_click_apply_noCrash_test() {
            onView(withId(R.id.time_filter_card)).perform(scrollTo());
            onView(withId(R.id.btn_apply)).perform(click());
            // No explicit assertion needed; success == no crash
    }
}

