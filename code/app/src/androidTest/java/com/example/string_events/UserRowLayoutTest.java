package com.example.string_events;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class UserRowLayoutTest {

    private static Intent hostRowIntent() {
        Context ctx = ApplicationProvider.getApplicationContext();
        return new Intent(ctx, UiHostActivity.class)
                .putExtra(UiHostActivity.EXTRA_LAYOUT_RES_ID, R.layout.item_user_row);
    }

    @Test
    public void coreWidgets_areVisible_andBadgeGone() {
        try (ActivityScenario<UiHostActivity> sc = ActivityScenario.launch(hostRowIntent())) {
            onView(withId(R.id.tvName)).check(matches(isDisplayed()));
            onView(withId(R.id.tvEmail)).check(matches(isDisplayed()));
            onView(withId(R.id.tvBadge))
                    .check(matches(withEffectiveVisibility(
                            androidx.test.espresso.matcher.ViewMatchers.Visibility.GONE)));
        }
    }

    @Test
    public void tap_doesNotCrash() {
        try (ActivityScenario<UiHostActivity> sc = ActivityScenario.launch(hostRowIntent())) {
            onView(withId(R.id.tvName)).perform(click());
            onView(withId(R.id.tvEmail)).perform(click());
        }
    }
}
