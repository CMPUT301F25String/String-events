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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * UI tests for admin_event_details_screen.xml
 * Only modifies androidTest code; no manifest/app changes.
 * Strategy: launch app's launcher Activity, then setContentView to the layout under test.
 */
@RunWith(AndroidJUnit4.class)
public class AdminEventDetailsScreenTest {

    /** Launch the launcher Activity and swap contentView to admin_event_details_screen. */
    private ActivityScenario<? extends Activity> launchWithLayout() {
        Context ctx = ApplicationProvider.getApplicationContext();
        Intent launch = ctx.getPackageManager().getLaunchIntentForPackage(ctx.getPackageName());
        if (launch == null) launch = new Intent(Intent.ACTION_MAIN);
        launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        ActivityScenario<? extends Activity> sc = ActivityScenario.launch(launch);
        sc.onActivity(a -> a.setContentView(R.layout.admin_event_details_screen));
        return sc;
    }

    @Test
    public void views_areDisplayed_test() {
        try (ActivityScenario<? extends Activity> sc = launchWithLayout()) {
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
        }
    }

    @Test
    public void texts_and_accessibility_areCorrect_test() {
        try (ActivityScenario<? extends Activity> sc = launchWithLayout()) {
            // Top bar title & back button a11y
            onView(withContentDescription("Back")).check(matches(isDisplayed()));
            onView(withText("Event Details")).check(matches(isDisplayed()));

            // Main info texts
            onView(withId(R.id.tvStatus)).check(matches(withText("Scheduled")));
            onView(withId(R.id.tvEventName)).check(matches(withText("Event Name")));
            onView(withId(R.id.tvOrganizer)).check(matches(withText("Organizer: XYZ")));
            onView(withId(R.id.tvEventDates)).check(matches(withText("Date and Time")));
            onView(withId(R.id.tvLocation)).check(matches(withText("Location Name")));

            // Registration section
            onView(withId(R.id.tvRegStart)).check(matches(withText("Start: -")));
            onView(withId(R.id.tvRegEnd)).check(matches(withText("End: -")));
            onView(withId(R.id.tvAttendees)).check(matches(withText("Attendees: 0")));
            onView(withId(R.id.tvWaitlist)).check(matches(withText("Waitlist: 0")));

            // Action button text
            onView(withId(R.id.btnQRCode)).perform(scrollTo()).check(matches(withText("QR Code")));
        }
    }

    @Test
    public void buttons_areClickable_noCrash_test() {
        try (ActivityScenario<? extends Activity> sc = launchWithLayout()) {
            // Clickables
            onView(withId(R.id.btnBack)).check(matches(isClickable()));
            onView(withId(R.id.btnQRCode)).perform(scrollTo()).check(matches(isClickable()));
            onView(withId(R.id.btnDelete)).perform(scrollTo()).check(matches(isClickable()));

            // Light interactions; success criterion: no crash
            onView(withId(R.id.btnQRCode)).perform(click());
            onView(withId(R.id.btnDelete)).perform(click());
            onView(withId(R.id.btnBack)).perform(click());
        }
    }
}
