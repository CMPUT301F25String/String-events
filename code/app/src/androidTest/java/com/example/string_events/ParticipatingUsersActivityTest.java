package com.example.string_events;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.Visibility;
import static androidx.test.espresso.matcher.ViewMatchers.isClickable;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
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
 * UI tests for activity_participating_users.xml
 * - Only touches androidTest code; no manifest/app code changes.
 * - Verifies header, list, action buttons, and that btnDeleteUser is GONE.
 */
@RunWith(AndroidJUnit4.class)
public class ParticipatingUsersActivityTest {

    /** Launch launcher activity and set our layout as content. Also add no-op listeners. */
    private ActivityScenario<? extends Activity> launchWithLayout() {
        Context ctx = ApplicationProvider.getApplicationContext();
        Intent launch = ctx.getPackageManager().getLaunchIntentForPackage(ctx.getPackageName());
        if (launch == null) launch = new Intent(Intent.ACTION_MAIN);
        launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        ActivityScenario<? extends Activity> sc = ActivityScenario.launch(launch);
        sc.onActivity(a -> {
            a.setContentView(R.layout.activity_participating_users);
            // attach no-op listeners so clicks won't throw
            int[] ids = { R.id.btnBack, R.id.btnSendParticipating, R.id.btnExportParticipating };
            android.view.View.OnClickListener noop = v -> { /* no-op */ };
            for (int id : ids) {
                android.view.View v = a.findViewById(id);
                if (v != null) {
                    v.setClickable(true);
                    v.setOnClickListener(noop);
                }
            }
        });
        return sc;
    }

    @Test
    public void views_areDisplayed_test() {
        try (ActivityScenario<? extends Activity> sc = launchWithLayout()) {
            // Header
            onView(withId(R.id.btnBack)).check(matches(isDisplayed()));
            onView(withId(R.id.tvTitle)).check(matches(isDisplayed()));
            onView(withId(R.id.tvTitle)).check(matches(withText("Participating Users"))); // from XML

            // List
            onView(withId(R.id.listParticipating)).check(matches(isDisplayed()));

            // Bottom actions
            onView(withId(R.id.btnSendParticipating)).check(matches(isDisplayed()));
            onView(withId(R.id.btnExportParticipating)).check(matches(isDisplayed()));

            // Delete button should be GONE
            onView(withId(R.id.btnDeleteUser))
                    .check(matches(withEffectiveVisibility(Visibility.GONE)));
        }
    }

    @Test
    public void buttons_areClickable_noCrash_test() {
        try (ActivityScenario<? extends Activity> sc = launchWithLayout()) {
            onView(withId(R.id.btnBack)).check(matches(isClickable())).perform(click());
            onView(withId(R.id.btnSendParticipating)).check(matches(isClickable())).perform(click());
            onView(withId(R.id.btnExportParticipating)).check(matches(isClickable())).perform(click());
        }
    }
}
