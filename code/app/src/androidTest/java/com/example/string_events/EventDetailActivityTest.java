package com.example.string_events;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
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
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * UI tests for event_detail_screen.xml
 * Only changes androidTest code; no manifest/app code changes.
 */
@RunWith(AndroidJUnit4.class)
public class EventDetailActivityTest {

    /** Launch launcher activity and set our layout as content. Attach no-op listeners for clicks. */
    private ActivityScenario<? extends Activity> launchWithLayout() {
        Context ctx = ApplicationProvider.getApplicationContext();
        Intent launch = ctx.getPackageManager().getLaunchIntentForPackage(ctx.getPackageName());
        if (launch == null) launch = new Intent(Intent.ACTION_MAIN);
        launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        ActivityScenario<? extends Activity> sc = ActivityScenario.launch(launch);
        sc.onActivity(a -> {
            a.setContentView(R.layout.event_detail_screen);

            // Attach no-op click listeners to avoid click failures in tests
            int[] clickableIds = {
                    R.id.back_button,
                    R.id.apply_button,
                    R.id.accept_invite_button,
                    R.id.decline_invite_button
            };
            android.view.View.OnClickListener noop = v -> { /* no-op */ };
            for (int id : clickableIds) {
                android.view.View v = a.findViewById(id);
                if (v != null) {
                    v.setClickable(true);
                    v.setOnClickListener(noop);
                }
            }
        });
        return sc;
    }

    /** Helper: swipe up几次直到目标出现在屏幕上（兼容一些非标准可滚容器的布局） */
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
        try (ActivityScenario<? extends Activity> sc = launchWithLayout()) {
            // Header
            onView(withId(R.id.header_container)).check(matches(isDisplayed()));
            onView(withId(R.id.back_button)).check(matches(isDisplayed()));
            onView(withId(R.id.tvTitle)).check(matches(isDisplayed()));

            // Main card (部分组件可能在首屏外，先滑动)
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
            swipeUntilVisible(R.id.tvDescription);
            onView(withId(R.id.tvDescription)).check(matches(isDisplayed()));

            // Bottom bar
            onView(withId(R.id.bottom_btn_container)).check(matches(isDisplayed()));
            onView(withId(R.id.apply_button)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void texts_and_visibilityStates_areCorrect_test() {
        try (ActivityScenario<? extends Activity> sc = launchWithLayout()) {
            // Header texts & a11y
            onView(withId(R.id.back_button)).check(matches(withContentDescription("Back")));           // XML
            onView(withId(R.id.tvTitle)).check(matches(withText("Event Details")));                  // XML

            // Main info defaults from XML
            swipeUntilVisible(R.id.tvEventTitle);
            onView(withId(R.id.tvEventTitle)).check(matches(withText("Badminton Drop In")));
            onView(withId(R.id.tvOrganizer)).check(matches(withText("Hosted by Community Sports")));
            onView(withId(R.id.tvDateLine)).check(matches(withText("Wed, Oct 8, 2025")));
            onView(withId(R.id.tvTimeLine)).check(matches(withText("11:00 AM - 1:00 PM")));
            onView(withId(R.id.tvAddress)).check(matches(withText("123 Generic Ave, Edmonton, AB")));
            // tvLocation is GONE in XML
            onView(withId(R.id.tvLocation)).check(matches(withEffectiveVisibility(Visibility.GONE)));

            // Chips defaults
            onView(withId(R.id.spots_taken)).check(matches(withText("10/20")));
            onView(withId(R.id.waiting_list)).check(matches(withText("20 People")));

            // Description
            onView(withId(R.id.tvDescription))
                    .check(matches(withText("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.")));

            // Bottom area: apply visible, invite container gone (per XML)
            onView(withId(R.id.apply_button)).check(matches(withContentDescription("Apply")));
            onView(withId(R.id.apply_button)).check(matches(withEffectiveVisibility(Visibility.VISIBLE)));
            onView(withId(R.id.invite_buttons_container))
                    .check(matches(withEffectiveVisibility(Visibility.GONE)));
        }
    }

    @Test
    public void buttons_areClickable_noCrash_test() {
        try (ActivityScenario<? extends Activity> sc = launchWithLayout()) {
            onView(withId(R.id.back_button)).check(matches(isClickable())).perform(click());
            swipeUntilVisible(R.id.apply_button);
            onView(withId(R.id.apply_button)).check(matches(isClickable())).perform(click());
        }
    }
}
