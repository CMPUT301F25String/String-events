package com.example.string_events;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class AdminDashboardActivityTest {

    @Rule
    public ActivityScenarioRule<AdminDashboardActivity> rule =
            new ActivityScenarioRule<>(AdminDashboardActivity.class);

    @Test
    public void initialRender_displaysCoreViews() {
        onView(withId(R.id.header_container)).check(matches(isDisplayed()));
        onView(withId(R.id.tv_title)).check(matches(withText("Admin Dashboard")));
        onView(withId(R.id.tv_subtitle)).check(matches(isDisplayed()));

        onView(withId(R.id.btnEvents)).check(matches(isDisplayed()));
        onView(withId(R.id.btnProfiles)).check(matches(isDisplayed()));
        onView(withId(R.id.btnImages)).perform(scrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.btnNotifLog)).perform(scrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.btnMyProfile)).perform(scrollTo()).check(matches(isDisplayed()));
    }

    @Test
    public void scroll_reachesMyProfileCard() {
        onView(withId(R.id.btnMyProfile)).perform(scrollTo()).check(matches(isDisplayed()));
    }

    @Test
    public void subtitle_isVisible() {
        onView(withId(R.id.tv_subtitle)).check(matches(isDisplayed()));
    }
}
