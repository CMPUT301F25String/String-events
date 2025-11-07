package com.example.string_events;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBackUnconditionally;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(AndroidJUnit4.class)
public class OrganizerEventDetailsLayoutTest {

    private ActivityScenario<UiHostActivity> scenario;

    @Before
    public void launch() {
        Intent i = new Intent(
                androidx.test.core.app.ApplicationProvider.getApplicationContext(),
                UiHostActivity.class
        );
        i.putExtra(UiHostActivity.EXTRA_LAYOUT_RES_ID, R.layout.org_event_details);
        scenario = ActivityScenario.launch(i);
    }

    @After
    public void close() {
        if (scenario != null) {
            scenario.close();
        }
    }

    @Test
    public void showsCoreWidgets() {
        onView(withId(R.id.btnBack)).check(matches(isDisplayed()));
        onView(withId(R.id.tvTitle)).check(matches(isDisplayed()));

        onView(withId(R.id.imgBanner)).check(matches(isDisplayed()));
        onView(withId(R.id.tvEventName)).check(matches(isDisplayed()));
        onView(withId(R.id.tvTime)).check(matches(isDisplayed()));
        onView(withId(R.id.tvLocation)).check(matches(isDisplayed()));

        onView(withId(R.id.btnRoll)).check(matches(isDisplayed()));
        onView(withId(R.id.btnCanceled)).check(matches(isDisplayed()));
        onView(withId(R.id.btnParticipating)).check(matches(isDisplayed()));
        onView(withId(R.id.btnWaitlist)).check(matches(isDisplayed()));
        onView(withId(R.id.btnWaitlistMap)).check(matches(isDisplayed()));
    }

    @Test
    public void clicks_doNotCrash() {
        onView(withId(R.id.btnRoll)).perform(click());
        onView(withId(R.id.btnCanceled)).perform(click());
        onView(withId(R.id.btnParticipating)).perform(click());
        onView(withId(R.id.btnWaitlist)).perform(click());
        onView(withId(R.id.btnWaitlistMap)).perform(click());

        pressBackUnconditionally();
    }

    @Test
    public void back_doesNotCrash() {
        onView(withId(R.id.btnBack)).perform(click());
    }
}

