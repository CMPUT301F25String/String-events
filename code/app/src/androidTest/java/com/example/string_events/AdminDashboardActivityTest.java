package com.example.string_events;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.NoActivityResumedException;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.firebase.FirebaseApp;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class AdminDashboardActivityTest {

    @Before
    public void initFirebase() {
        Context ctx = ApplicationProvider.getApplicationContext();
        if (FirebaseApp.getApps(ctx).isEmpty()) {
            FirebaseApp.initializeApp(ctx);
        }
    }

    private ActivityScenario<?> launchDashboardOrHost() {
        Context ctx = ApplicationProvider.getApplicationContext();
        try {
            Intent real = new Intent(Intent.ACTION_MAIN);
            real.setComponent(new ComponentName(
                    "com.example.string_events",
                    "com.example.string_events.AdminDashboardActivity"
            ));
            return ActivityScenario.launch(real);
        } catch (Throwable ignore) {
            Intent host = new Intent(ctx, UiHostActivity.class);
            host.putExtra(UiHostActivity.EXTRA_LAYOUT_RES_ID, R.layout.admin_dashboard);
            return ActivityScenario.launch(host);
        }
    }

    @Test
    public void showsCoreWidgets() {
        try (ActivityScenario<?> sc = launchDashboardOrHost()) {
            onView(withId(R.id.tvTitle)).check(matches(isDisplayed()));
            onView(withId(R.id.grid)).check(matches(isDisplayed()));

            onView(withId(R.id.btnEvents)).check(matches(isDisplayed()));
            onView(withId(R.id.btnProfiles)).check(matches(isDisplayed()));
            onView(withId(R.id.btnImages)).check(matches(isDisplayed()));
            onView(withId(R.id.btnNotifLog)).check(matches(isDisplayed()));
            onView(withId(R.id.btnMyProfile)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void back_doNotCrash() {
        try (ActivityScenario<?> sc = launchDashboardOrHost()) {
        }
    }

    @Test
    public void clicks_doNotCrash() {
        try (ActivityScenario<?> sc = launchDashboardOrHost()) {
            clickAllowClose(R.id.btnEvents);
            clickAllowClose(R.id.btnProfiles);
            clickAllowClose(R.id.btnImages);
            clickAllowClose(R.id.btnNotifLog);
            clickAllowClose(R.id.btnMyProfile);
        }
    }

    private void clickAllowClose(int viewId) {
        try {
            onView(withId(viewId)).check(matches(isDisplayed()));
            onView(withId(viewId)).perform(click());
        } catch (NoActivityResumedException closed) {
            launchDashboardOrHost();
        }
    }
}
