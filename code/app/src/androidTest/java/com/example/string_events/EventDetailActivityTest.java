package com.example.string_events;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.swipeUp;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.Visibility;
import static androidx.test.espresso.matcher.ViewMatchers.isClickable;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.not;

/**
 * UI tests for event_detail_screen.xml
 * Only changes androidTest code; no manifest/app code changes.
 */
@RunWith(AndroidJUnit4.class)
public class EventDetailActivityTest {

    /** Launch launcher activity and set our layout as content. Attach no-op listeners for clicks. */

    public static Intent newIntent() {
        // Using ApplicationProvider.getApplicationContext() is the standard way
        // to get a context in an instrumented test.
        Context ctx = ApplicationProvider.getApplicationContext();
        Intent intent = new Intent(ctx, EventDetailActivity.class);

        intent.putExtra("event_id", "e299ee34-3977-491e-8f2b-0ce30bd7e447");
        return intent;
    }

    @Rule
    public ActivityScenarioRule<EventDetailActivity> scenario = new ActivityScenarioRule<>(newIntent());

    private static void swipeUntilVisible(int viewId) {
        for (int i = 0; i < 6; i++) {
            try {
                onView(withId(viewId)).check(matches(isDisplayed()));
                return;
            } catch (AssertionError | androidx.test.espresso.NoMatchingViewException e) {
                onView(withId(android.R.id.content)).perform(swipeUp());
            }
        }
        onView(withId(viewId)).check(matches(isDisplayed()));
    }

    @Test
    public void views_areDisplayed_test() {
            // Header
            onView(withId(R.id.header_container)).check(matches(isDisplayed()));
            onView(withId(R.id.back_button)).check(matches(isDisplayed()));
            onView(withId(R.id.tvTitle)).check(matches(isDisplayed()));

            // Main card
            swipeUntilVisible(R.id.ivEventImage);
            onView(withId(R.id.ivEventImage)).check(matches(isDisplayed()));
            onView(withId(R.id.tvEventTitle)).check(matches(isDisplayed()));
            onView(withId(R.id.tvOrganizer)).check(matches(isDisplayed()));
            onView(withId(R.id.tvDateLine)).check(matches(isDisplayed()));
            onView(withId(R.id.tvTimeLine)).check(matches(isDisplayed()));
            onView(withId(R.id.tvAddress)).check(matches(isDisplayed()));

            // Chips
            onView(withId(R.id.spots_taken)).check(matches(isDisplayed()));
            onView(withId(R.id.waiting_list)).check(matches(isDisplayed()));

            // Description card
            onView(withId(R.id.tvDescription)).perform(scrollTo()).check(matches(isDisplayed()));
            onView(withId(R.id.tvDescription)).check(matches(isDisplayed()));

            // Bottom bar
            onView(withId(R.id.bottom_btn_container)).check(matches(isDisplayed()));
            onView(withId(R.id.apply_button)).check(matches(isDisplayed()));
    }

    @Test
    public void texts_and_visibilityStates_areCorrect_test() {
            // Header texts & a11y
            onView(withId(R.id.back_button)).check(matches(withContentDescription("Back")));
            onView(withId(R.id.tvTitle)).check(matches(withText("Event Details")));


            // Main info is populated from the database, so check for non-empty text
            swipeUntilVisible(R.id.tvEventTitle);
            onView(withId(R.id.tvEventTitle)).check(matches(not(withText(""))));
            onView(withId(R.id.tvOrganizer)).check(matches(not(withText(""))));
            onView(withId(R.id.tvDateLine)).check(matches(not(withText(""))));
            onView(withId(R.id.tvTimeLine)).check(matches(not(withText(""))));
            onView(withId(R.id.tvAddress)).check(matches(not(withText(""))));

            // tvLocation is GONE in XML
            onView(withId(R.id.tvLocation)).check(matches(withEffectiveVisibility(Visibility.GONE)));

            // Chips defaults
            onView(withId(R.id.spots_taken)).check(matches(not(withText(""))));
            onView(withId(R.id.waiting_list)).check(matches(not(withText(""))));

            // Description
            onView(withId(R.id.tvDescription))
                    .check(matches(not(withText(""))));

            // Bottom area: apply visible, invite container gone (per XML)
            onView(withId(R.id.apply_button)).check(matches(withContentDescription("Apply")));
            onView(withId(R.id.apply_button)).check(matches(withEffectiveVisibility(Visibility.VISIBLE)));
            onView(withId(R.id.invite_buttons_container))
                    .check(matches(withEffectiveVisibility(Visibility.GONE)));
    }

    @Test
    public void buttons_areClickable_noCrash_test() {
            swipeUntilVisible(R.id.apply_button);
            onView(withId(R.id.apply_button)).check(matches(isClickable())).perform(click());
            onView(withId(R.id.back_button)).check(matches(isClickable())).perform(click());
    }
}
