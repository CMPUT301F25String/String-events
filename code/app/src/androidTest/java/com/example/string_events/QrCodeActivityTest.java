package com.example.string_events;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
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
 * UI tests for activity_qr_code.xml
 * Only modifies androidTest code (no manifest/app code changes).
 */
@RunWith(AndroidJUnit4.class)
public class QrCodeActivityTest {

    /** Launch the app's launcher Activity and replace its content with the layout under test. */
    private ActivityScenario<UiHostActivity> launchWithLayout() {
        Context ctx = ApplicationProvider.getApplicationContext();
        Intent it = new Intent(ctx, UiHostActivity.class)
                .putExtra(UiHostActivity.EXTRA_LAYOUT_RES_ID, R.layout.activity_qr_code);
        return ActivityScenario.launch(it);
    }

    @Test
    public void views_areDisplayed_test() {
        try (ActivityScenario<UiHostActivity> sc = launchWithLayout()) {
            onView(withId(R.id.btn_back)).check(matches(isDisplayed()));
            onView(withId(R.id.tv_title_qr)).check(matches(isDisplayed()));
            onView(withId(R.id.img_qr)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void texts_and_accessibility_areCorrect_test() {
        try (ActivityScenario<UiHostActivity> sc = launchWithLayout()) {
            onView(withId(R.id.tv_title_qr)).check(matches(withText("QR Code"))); // from XML
            onView(withId(R.id.btn_back)).check(matches(withContentDescription("Back"))); // from XML
        }
    }

    @Test
    public void buttons_areClickable_noCrash_test() {
        try (ActivityScenario<UiHostActivity> sc = launchWithLayout()) {
            onView(withId(R.id.btn_back)).check(matches(isClickable()));
            onView(withId(R.id.btn_back)).perform(click()); // no-op; success = no crash
        }
    }
}

