package com.example.string_events;


import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.NoActivityResumedException;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import static androidx.test.espresso.matcher.ViewMatchers.withResourceName;
import androidx.test.espresso.NoMatchingViewException;
import com.google.firebase.FirebaseApp;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class OrganizerEventDetailsTest {

    private static Intent makeIntent() {
        Context ctx = ApplicationProvider.getApplicationContext();
        return new Intent(ctx, OrganizerEventDetails.class)
                .putExtra("fromTest", true)
                .putExtra("eventId", "evt-test-001");
    }

    private static ViewAction waitMs(long ms) {
        return new ViewAction() {
            @Override
            public Matcher<android.view.View> getConstraints() {
                return isRoot();
            }

            @Override
            public String getDescription() {
                return "wait " + ms + "ms";
            }

            @Override
            public void perform(UiController ui, android.view.View v) {
                ui.loopMainThreadForAtLeast(ms);
            }
        };
    }

    private static void idle() {
        onView(isRoot()).perform(waitMs(120));
    }

    @Before
    public void ensureFirebase() {
        Context ctx = ApplicationProvider.getApplicationContext();
        if (FirebaseApp.getApps(ctx).isEmpty()) {
            FirebaseApp.initializeApp(ctx);
        }
    }


    @Test
    public void showsCoreWidgets() {
        try (ActivityScenario<OrganizerEventDetails> sc = ActivityScenario.launch(makeIntent())) {
            idle();
            onView(withId(R.id.btnBack)).check(matches(isDisplayed()));
            onView(withId(R.id.tvTitle)).check(matches(isDisplayed()));
            onView(withId(R.id.imgBanner)).check(matches(isDisplayed()));
            onView(withId(R.id.tvEventName)).check(matches(isDisplayed()));
            onView(withId(R.id.tvTime)).check(matches(isDisplayed()));
            onView(withId(R.id.tvLocation)).check(matches(isDisplayed()));

            onView(withId(R.id.btnRoll)).perform(scrollTo()).check(matches(isDisplayed()));
            onView(withId(R.id.btnCanceled)).perform(scrollTo()).check(matches(isDisplayed()));
            onView(withId(R.id.btnParticipating)).perform(scrollTo()).check(matches(isDisplayed()));
            onView(withId(R.id.btnWaitlist)).perform(scrollTo()).check(matches(isDisplayed()));
            onView(withId(R.id.btnWaitlistMap)).perform(scrollTo()).check(matches(isDisplayed()));
        }
    }


    @Test
    public void clickButtons_doNotCrash() {
        try (ActivityScenario<OrganizerEventDetails> sc =
                     ActivityScenario.launch(makeIntent())) {
            idle();

            // Roll
            clickIfExistsByIds(R.id.btnRoll);

            // Canceled / Cancel / Cancelled
            clickIfExistsByIdsOrNames(
                    new int[]{R.id.btnCanceled},
                    new String[]{"btnCancel", "btnCancelled"}
            );

            // Participating / Participants / Participate
            clickIfExistsByIdsOrNames(
                    new int[]{R.id.btnParticipating},
                    new String[]{"btnParticipants", "btnParticipate"}
            );

            // Waitlist / WaitingList
            clickIfExistsByIdsOrNames(
                    new int[]{R.id.btnWaitlist},
                    new String[]{"btnWaitingList"}
            );

            // WaitlistMap / WaitingMap / Map
            clickIfExistsByIdsOrNames(
                    new int[]{R.id.btnWaitlistMap},
                    new String[]{"btnWaitingMap", "btnMap"}
            );

            idle();
        }
    }

    private static void clickIfExistsByIds(int... ids) {
        for (int id : ids) {
            try {
                onView(withId(id)).perform(scrollTo(), click());
                return;
            } catch (NoMatchingViewException ignored) {  }
        }
    }


    private static void clickIfExistsByIdsOrNames(int[] ids, String[] names) {
        for (int id : ids) {
            try {
                onView(withId(id)).perform(scrollTo(), click());
                return;
            } catch (NoMatchingViewException ignored) {
            }
        }
        for (String n : names) {
            try {
                onView(withResourceName(n)).perform(scrollTo(), click());
                return;
            } catch (NoMatchingViewException ignored) {
            }
        }
    }
}
