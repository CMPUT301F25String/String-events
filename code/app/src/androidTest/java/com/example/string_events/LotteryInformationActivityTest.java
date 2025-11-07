package com.example.string_events;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.NoActivityResumedException;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.hamcrest.Matcher;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class LotteryInformationActivityTest {

    private static Intent makeIntent() {
        Context ctx = ApplicationProvider.getApplicationContext();
        return new Intent(ctx, LotteryInformationActivity.class)
                .putExtra("fromTest", true);
    }

    private static ViewAction waitMs(long ms) {
        return new ViewAction() {
            @Override public Matcher<View> getConstraints() { return isRoot(); }
            @Override public String getDescription() { return "wait " + ms + " ms"; }
            @Override public void perform(UiController ui, View v) { ui.loopMainThreadForAtLeast(ms); }
        };
    }
    private static void idle() { onView(isRoot()).perform(waitMs(120)); }

    @Test
    public void showsCoreWidgets() {
        try (ActivityScenario<LotteryInformationActivity> sc = ActivityScenario.launch(makeIntent())) {
            idle();
            onView(withId(R.id.btn_back)).check(matches(isDisplayed()));
            onView(withId(R.id.tv_title)).check(matches(isDisplayed()));
            onView(withId(R.id.tv_guidelines_title)).check(matches(isDisplayed()));
            onView(withId(R.id.tv_guidelines_content)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void back_doNotCrash() {
        try (ActivityScenario<LotteryInformationActivity> sc = ActivityScenario.launch(makeIntent())) {
            idle();
            try {
                onView(withId(R.id.btn_back)).perform(click());
                idle();
            } catch (NoActivityResumedException ignored) {
            }
        }
    }
}
