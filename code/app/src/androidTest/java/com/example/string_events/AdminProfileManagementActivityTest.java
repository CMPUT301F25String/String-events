package com.example.string_events;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.swipeDown;
import static androidx.test.espresso.action.ViewActions.swipeUp;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.content.pm.ActivityInfo;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class AdminProfileManagementActivityTest {

    @Rule
    public ActivityScenarioRule<AdminProfileManagementActivity> rule =
            new ActivityScenarioRule<>(AdminProfileManagementActivity.class);

    @Test
    public void initialRender_displaysCoreViews() {
        onView(withId(R.id.top_bar_layout)).check(matches(isDisplayed()));
        onView(withId(R.id.btnBack)).check(matches(isDisplayed()));
        onView(withText("Profile Management")).check(matches(isDisplayed()));
        onView(withId(R.id.recyclerView)).check(matches(isDisplayed()));
    }

    @Test
    public void recycler_canScroll_noCrash() {
        onView(withId(R.id.recyclerView)).perform(swipeUp());
        onView(withId(R.id.recyclerView)).perform(swipeDown());
        onView(withId(R.id.recyclerView)).check(matches(isDisplayed()));
    }

    @Test
    public void backButton_clickable() {
        onView(withId(R.id.btnBack)).perform(click());
    }

    @Test
    public void orientationChange_persistsUi() {
        ActivityScenario<AdminProfileManagementActivity> scenario = rule.getScenario();

        scenario.onActivity(a ->
                a.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE));
        onView(withText("Profile Management")).check(matches(isDisplayed()));
        onView(withId(R.id.recyclerView)).check(matches(isDisplayed()));

        scenario.onActivity(a ->
                a.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT));
        onView(withText("Profile Management")).check(matches(isDisplayed()));
        onView(withId(R.id.recyclerView)).check(matches(isDisplayed()));
    }
}
