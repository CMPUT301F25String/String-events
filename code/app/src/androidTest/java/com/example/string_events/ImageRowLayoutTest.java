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

import com.example.string_events.UiHostActivity;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ImageRowLayoutTest {

    private static Intent hostIntentFor(int layoutResId) {
        Context ctx = ApplicationProvider.getApplicationContext();
        Intent i = new Intent(ctx, UiHostActivity.class);
        i.putExtra(UiHostActivity.EXTRA_LAYOUT_RES_ID, layoutResId);
        return i;
    }

    @Test
    public void image_isVisible() {
        try (ActivityScenario<UiHostActivity> sc =
                     ActivityScenario.launch(hostIntentFor(R.layout.item_image_row))) {
            onView(withId(R.id.img_event)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void image_click_doesNotCrash() {
        try (ActivityScenario<UiHostActivity> sc =
                     ActivityScenario.launch(hostIntentFor(R.layout.item_image_row))) {
            onView(withId(R.id.img_event)).perform(click());
        }
    }
}
