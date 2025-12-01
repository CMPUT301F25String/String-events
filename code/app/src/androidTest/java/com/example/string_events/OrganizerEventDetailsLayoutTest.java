package com.example.string_events;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBackUnconditionally;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.content.Intent;
import android.view.View;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.PerformException;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(AndroidJUnit4.class)
public class OrganizerEventDetailsLayoutTest {

    private ActivityScenario<UiHostActivity> scenario;

    private static ViewAction safeScrollTo() {
        return new ViewAction() {
            @Override public Matcher<View> getConstraints() { return isDisplayed(); }
            @Override public String getDescription() { return "safeScrollTo (ignore PerformException)"; }
            @Override public void perform(UiController ui, View v) {
                try { scrollTo().perform(ui, v); } catch (PerformException ignore) {}
                ui.loopMainThreadForAtLeast(50);
            }
        };
    }

    @Before
    public void launch() {
        Intent i = new Intent(
                androidx.test.core.app.ApplicationProvider.getApplicationContext(),
                UiHostActivity.class
        );
        i.putExtra(UiHostActivity.EXTRA_LAYOUT_RES_ID, R.layout.org_event_detail_screen);
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
        onView(withId(R.id.btnWaitlistMap)).perform(safeScrollTo());
        onView(withId(R.id.btnWaitlistMap)).perform(click());

        pressBackUnconditionally();
    }

    @Test
    public void back_doesNotCrash() {
        onView(withId(R.id.btnBack)).perform(click());
    }
}

