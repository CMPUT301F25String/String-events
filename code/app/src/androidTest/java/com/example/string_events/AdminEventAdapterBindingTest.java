package com.example.string_events;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import android.widget.ListView;
import androidx.lifecycle.Lifecycle;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class AdminEventAdapterBindingTest {

    @Rule
    public ActivityScenarioRule<AdminEventManagementActivity> rule =
            new ActivityScenarioRule<>(AdminEventManagementActivity.class);

    @Test
    public void list_isDisplayed_and_firstItemHasNonEmptyFields() {
        // Assert there is a visible ListView on screen (no hardcoded id)
        onView(isAssignableFrom(ListView.class)).check(matches(isDisplayed()));

        onData(anything())
                .inAdapterView(isAssignableFrom(ListView.class))
                .atPosition(0)
                .onChildView(withId(R.id.tvTitle))
                .check(matches(not(withText(emptyOrNullString()))));

        onData(anything())
                .inAdapterView(isAssignableFrom(ListView.class))
                .atPosition(0)
                .onChildView(withId(R.id.tvTime))
                .check(matches(not(withText(emptyOrNullString()))));

        onData(anything())
                .inAdapterView(isAssignableFrom(ListView.class))
                .atPosition(0)
                .onChildView(withId(R.id.tvLocation))
                .check(matches(not(withText(emptyOrNullString()))));

        onData(anything())
                .inAdapterView(isAssignableFrom(ListView.class))
                .atPosition(0)
                .onChildView(withId(R.id.tvOrganizer))
                .check(matches(not(withText(emptyOrNullString()))));

        onData(anything())
                .inAdapterView(isAssignableFrom(ListView.class))
                .atPosition(0)
                .onChildView(withId(R.id.chipStatus))
                .check(matches(isDisplayed()));
    }

    @Test
    public void list_containsBadmintonDropIn_card() {
        onData(anything())
                .inAdapterView(isAssignableFrom(ListView.class))
                .atPosition(2)
                .onChildView(withId(R.id.tvTitle))
                .check(matches(withText("Badminton Drop In")));
    }

    @Test
    public void backButton_finishesActivity_withoutResumedActivity() {
        // We are RESUMED
        onView(isAssignableFrom(ListView.class)).check(matches(isDisplayed()));
        assertTrue(rule.getScenario().getState() == Lifecycle.State.RESUMED);

        onView(withId(R.id.btn_back)).perform(click());

        rule.getScenario().onActivity(activity -> assertTrue(activity.isFinishing()));
        assertNotEquals(Lifecycle.State.RESUMED, rule.getScenario().getState());
    }
}
