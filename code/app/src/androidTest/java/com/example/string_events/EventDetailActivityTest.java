package com.example.string_events;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.action.GeneralLocation;
import androidx.test.espresso.action.GeneralSwipeAction;
import androidx.test.espresso.action.Press;
import androidx.test.espresso.action.Swipe;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.firebase.FirebaseApp;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class EventDetailActivityTest {

    private static Intent makeIntent() {
        Context ctx = ApplicationProvider.getApplicationContext();
        return new Intent(ctx, EventDetailActivity.class)
                .putExtra("fromTest", true)
                .putExtra("event_id", "test-event"); // 与 Activity 一致
    }

    @Before
    public void setup() {
        Context ctx = ApplicationProvider.getApplicationContext();
        if (FirebaseApp.getApps(ctx).isEmpty()) FirebaseApp.initializeApp(ctx);
        SharedPreferences sp = ctx.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        sp.edit().putString("user", "ui-tester").apply();
    }

    // 等待窗口获得焦点 & 稍作 idle
    private static ViewAction waitForWindowFocus(final long timeoutMs) {
        return new ViewAction() {
            @Override public Matcher<View> getConstraints() { return isRoot(); }
            @Override public String getDescription() { return "wait for window focus"; }
            @Override public void perform(UiController ui, View view) {
                long end = System.currentTimeMillis() + timeoutMs;
                while (!view.getRootView().hasWindowFocus() && System.currentTimeMillis() < end) {
                    ui.loopMainThreadForAtLeast(50);
                }
            }
        };
    }
    private static ViewAction waitForIdle(final long ms) {
        return new ViewAction() {
            @Override public Matcher<View> getConstraints() { return isRoot(); }
            @Override public String getDescription() { return "wait for idle"; }
            @Override public void perform(UiController ui, View view) {
                ui.loopMainThreadForAtLeast(ms);
            }
        };
    }
    private void ensureReady() {
        onView(isRoot()).perform(waitForWindowFocus(4000));
        onView(isRoot()).perform(waitForIdle(200));
    }

    private void swipeUp() {
        onView(isRoot()).perform(new GeneralSwipeAction(
                Swipe.SLOW, GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER, Press.FINGER));
    }

    @Test
    public void showsCoreWidgets() {
        try (ActivityScenario<EventDetailActivity> sc = ActivityScenario.launch(makeIntent())) {
            ensureReady();
            onView(withId(R.id.back_button)).check(matches(isDisplayed()));
            onView(withId(R.id.tvEventTitle)).check(matches(isDisplayed()));
            onView(withId(R.id.tvLocation)).check(matches(isDisplayed()));
            onView(withId(R.id.ivEventImage)).check(matches(isDisplayed()));
            onView(withId(R.id.spots_taken)).check(matches(isDisplayed()));
            onView(withId(R.id.waiting_list)).check(matches(isDisplayed()));
            swipeUp();
            onView(withId(R.id.tvDateLine)).check(matches(isDisplayed()));
            onView(withId(R.id.tvTimeLine)).check(matches(isDisplayed()));
            onView(withId(R.id.tvAddress)).check(matches(isDisplayed()));
            swipeUp();
            onView(withId(R.id.tvDescription)).check(matches(isDisplayed()));
            onView(withId(R.id.apply_button)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void backButton_doesNotCrash() {
        try (ActivityScenario<EventDetailActivity> sc = ActivityScenario.launch(makeIntent())) {
            ensureReady();
            onView(withId(R.id.back_button)).perform(click());
        }
    }

    @Test
    public void applyButton_doesNotCrash() {
        try (ActivityScenario<EventDetailActivity> sc = ActivityScenario.launch(makeIntent())) {
            ensureReady();
            swipeUp();
            onView(withId(R.id.apply_button)).perform(click());
            // 点击后不给 Toast，窗口始终保持焦点；再稍微等一下稳定
            onView(isRoot()).perform(waitForIdle(200));
        }
    }
}
