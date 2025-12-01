package com.example.string_events;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.Visibility;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * UI tests for item_event_image.xml
 * Only touches androidTest code; no manifest/app changes.
 */
@RunWith(AndroidJUnit4.class)
public class ItemEventImageLayoutTest {

    /** Launch launcher activity and replace its content with the layout under test. */
    private static Intent hostIntentFor(int layoutResId) {
        Context ctx = ApplicationProvider.getApplicationContext();
        Intent i = new Intent(ctx, UiHostActivity.class);
        i.putExtra(UiHostActivity.EXTRA_LAYOUT_RES_ID, layoutResId);
        return i;
    }

    @Test
    public void views_areDisplayed_test() {
        try (ActivityScenario<UiHostActivity> sc =
                     ActivityScenario.launch(hostIntentFor(R.layout.item_event_image))) {
            onView(withId(R.id.img_event)).check(matches(isDisplayed()));
            onView(withId(R.id.tv_event_title)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void defaults_areCorrect_test() {
        try (ActivityScenario<UiHostActivity> sc =
                     ActivityScenario.launch(hostIntentFor(R.layout.item_event_image))) {
            // title text & overlay default visibility from XML
            onView(withId(R.id.tv_event_title)).check(matches(withText("Event Title")));
            onView(withId(R.id.overlay_selected))
                    .check(matches(withEffectiveVisibility(Visibility.GONE)));
        }
    }

    @Test
    public void overlay_toggle_programmatic_test() {
        try (ActivityScenario<UiHostActivity> sc =
                     ActivityScenario.launch(hostIntentFor(R.layout.item_event_image))) {
            // Simulate adapter selection effect by toggling overlay visibility in test host
            sc.onActivity(a -> {
                View overlay = a.findViewById(R.id.overlay_selected);
                overlay.setVisibility(View.VISIBLE);
            });
            onView(withId(R.id.overlay_selected))
                    .check(matches(withEffectiveVisibility(Visibility.VISIBLE)));

            sc.onActivity(a -> a.findViewById(R.id.overlay_selected)
                    .setVisibility(View.GONE));
            onView(withId(R.id.overlay_selected))
                    .check(matches(withEffectiveVisibility(Visibility.GONE)));
        }
    }
}
