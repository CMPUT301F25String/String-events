package com.example.string_events;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isClickable;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.CoreMatchers.not;

import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.function.Predicate;

/**
 * UI tests for admin_event_details_screen.xml
 * Only modifies androidTest code; no manifest/app changes.
 * Strategy: launch app's launcher Activity, then setContentView to the layout under test.
 */
@RunWith(AndroidJUnit4.class)
public class AdminEventDetailsScreenTest {

    public static Intent newIntent() {
        // Using ApplicationProvider.getApplicationContext() is the standard way
        // to get a context in an instrumented test.
        Context ctx = ApplicationProvider.getApplicationContext();
        Intent intent = new Intent(ctx, AdminEventDetailActivity.class);

        // Provide a dummy (but valid-looking) event ID.
        // This doesn't have to exist in the database for UI rendering tests.
        intent.putExtra("event_id", "e299ee34-3977-491e-8f2b-0ce30bd7e447");
        return intent;
    }

    /** Launch the launcher Activity and swap contentView to admin_event_details_screen. */
    @Rule
    public ActivityScenarioRule<AdminEventDetailActivity> scenario = new ActivityScenarioRule<>(newIntent());

    @Test
    public void views_areDisplayed_test() {
//        try (ActivityScenario<? extends Activity> ignored = launchWithLayout().getScenario()) {
            // Top bar
            onView(withId(R.id.top_bar)).check(matches(isDisplayed()));
            onView(withId(R.id.btnBack)).check(matches(isDisplayed()));

            // Image + main info card
            onView(withId(R.id.cv_image_container)).check(matches(isDisplayed()));
            onView(withId(R.id.imgEvent)).check(matches(isDisplayed()));
            onView(withId(R.id.card_main_info)).check(matches(isDisplayed()));
            onView(withId(R.id.cv_status)).check(matches(isDisplayed()));
            onView(withId(R.id.tvStatus)).check(matches(isDisplayed()));
            onView(withId(R.id.tvEventName)).check(matches(isDisplayed()));
            onView(withId(R.id.tvOrganizer)).check(matches(isDisplayed()));
            onView(withId(R.id.ic_date)).check(matches(isDisplayed()));
            onView(withId(R.id.tvEventDates)).check(matches(isDisplayed()));
            onView(withId(R.id.ic_location)).check(matches(isDisplayed()));
            onView(withId(R.id.tvLocation)).check(matches(isDisplayed()));

            // Registration card
            onView(withId(R.id.card_details)).check(matches(isDisplayed()));
            onView(withId(R.id.tvRegStart)).check(matches(isDisplayed()));
            onView(withId(R.id.tvRegEnd)).check(matches(isDisplayed()));
            onView(withId(R.id.tvAttendees)).check(matches(isDisplayed()));
            onView(withId(R.id.tvWaitlist)).check(matches(isDisplayed()));

            // Description card
            onView(withId(R.id.card_desc)).check(matches(isDisplayed()));
            onView(withId(R.id.tvDescription)).check(matches(isDisplayed()));

            // Action area (may be offscreen, use scroll)
            onView(withId(R.id.layout_actions)).perform(scrollTo()).check(matches(isDisplayed()));
            onView(withId(R.id.btnQRCode)).check(matches(isDisplayed()));
            onView(withId(R.id.btnDelete)).perform(scrollTo()).check(matches(isDisplayed()));
//        }
    }

    @Test
    public void texts_and_accessibility_areCorrect_test() {
//        try (ActivityScenario<? extends Activity> ignored = launchWithLayout().getScenario()) {
            // Top bar title & back button a11y
            onView(withContentDescription("Back")).check(matches(isDisplayed()));
            onView(withText("Event Details")).check(matches(isDisplayed()));

            // Main info texts
            onView(withId(R.id.tvStatus)).check(matches(not(withText(""))));
            onView(withId(R.id.tvEventName)).check(matches(not(withText(""))));
            onView(withId(R.id.tvOrganizer)).check(matches(not(withText(""))));
            onView(withId(R.id.tvEventDates)).check(matches(not(withText(""))));
            onView(withId(R.id.tvLocation)).check(matches(not(withText(""))));

            // Registration section
            onView(withId(R.id.tvRegStart)).check(matches(not(withText(""))));
            onView(withId(R.id.tvRegEnd)).check(matches(not(withText(""))));
            onView(withId(R.id.tvAttendees)).check(matches(not(withText(""))));
            onView(withId(R.id.tvWaitlist)).check(matches(not(withText(""))));

            // Action button text
            onView(withId(R.id.btnQRCode)).perform(scrollTo()).check(matches(withText("QR Code")));
//        }
    }

    @Test
    public void buttons_areClickable_noCrash_test() {
//        try (ActivityScenario<? extends Activity> ignored = launchWithLayout().getScenario()) {
            // Clickables
            onView(withId(R.id.btnBack)).check(matches(isClickable()));
            onView(withId(R.id.btnQRCode)).perform(scrollTo()).check(matches(isClickable()));
            onView(withId(R.id.btnDelete)).perform(scrollTo()).check(matches(isClickable()));

            // Light interactions; success criterion: no crash
            onView(withId(R.id.btnQRCode)).perform(click());
//        }
    }
}
