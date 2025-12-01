package com.example.string_events;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.swipeUp;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.content.pm.ActivityInfo;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.PerformException;
import androidx.test.espresso.action.ViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * UI smoke tests for "Event Message" screen based on activity_event_message.xml.
 * Notes:
 *  - Always bring views into viewport before asserting (safeScrollToId).
 *  - Avoid triggering business logic; verify visibility/enabled only.
 *  - Keep comments in English as requested.
 */
@RunWith(AndroidJUnit4.class)
public class EventMessageActivityTest {

    @Rule
    public ActivityScenarioRule<EventMessageActivity> rule =
            new ActivityScenarioRule<>(EventMessageActivity.class);

    // ---------- Helper ----------

    /**
     * Attempt to bring the target view on-screen without requiring device-wide animation toggles.
     * Strategy: try scrollTo(); if animations cause a PerformException, swipe the root upward and retry.
     */
    private void safeScrollToId(int viewId) {
        final int MAX_ATTEMPTS = 6;
        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            try {
                onView(withId(viewId)).perform(ViewActions.scrollTo());
                return;
            } catch (PerformException ignored) {
                onView(withId(android.R.id.content)).perform(swipeUp());
            } catch (Throwable ignored) {
                onView(withId(android.R.id.content)).perform(swipeUp());
            }
        }
        // Final attempt: let Espresso throw if it still can't scroll.
        onView(withId(viewId)).perform(ViewActions.scrollTo());
    }

    // ---------- Tests ----------

    /** Verify top bar and title are visible. */
    @Test
    public void initialRender_headerVisible() {
        onView(withId(R.id.btnBack)).check(matches(isDisplayed()));
        onView(withId(R.id.tvTitle)).check(matches(isDisplayed()));
    }

    /** Verify recipient buttons, message label/box, and send button are visible (after scroll). */
    @Test
    public void initialRender_bodyControlsVisible() {
        safeScrollToId(R.id.btnWaitingUsers);
        onView(withId(R.id.btnWaitingUsers)).check(matches(isDisplayed()));

        safeScrollToId(R.id.btnParticipatingUsers);
        onView(withId(R.id.btnParticipatingUsers)).check(matches(isDisplayed()));

        safeScrollToId(R.id.btnCanceledUsers);
        onView(withId(R.id.btnCanceledUsers)).check(matches(isDisplayed()));

        safeScrollToId(R.id.etMessage);
        onView(withId(R.id.etMessage)).check(matches(isDisplayed()));

        safeScrollToId(R.id.btnSendMessage);
        onView(withId(R.id.btnSendMessage)).check(matches(isDisplayed()));
    }

    /** Light-touch interactivity checks: buttons are enabled; we avoid asserting navigation or side effects. */
    @Test
    public void controls_enabled_noBusinessSideEffects() {
        safeScrollToId(R.id.btnWaitingUsers);
        onView(withId(R.id.btnWaitingUsers)).check(matches(isEnabled()));

        safeScrollToId(R.id.btnParticipatingUsers);
        onView(withId(R.id.btnParticipatingUsers)).check(matches(isEnabled()));

        safeScrollToId(R.id.btnCanceledUsers);
        onView(withId(R.id.btnCanceledUsers)).check(matches(isEnabled()));

        safeScrollToId(R.id.btnSendMessage);
        onView(withId(R.id.btnSendMessage)).check(matches(isEnabled()));
    }

    /** Back button is clickable (no navigation assertion). */
    @Test
    public void backButton_clickable() {
        onView(withId(R.id.btnBack)).check(matches(isDisplayed()));
        onView(withId(R.id.btnBack)).perform(click());
    }

    /** After orientation changes, core controls remain reachable and visible. */
    @Test
    public void orientationChange_persistsUi() {
        ActivityScenario<EventMessageActivity> scenario = rule.getScenario();

        scenario.onActivity(a -> a.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE));
        onView(withId(R.id.tvTitle)).check(matches(isDisplayed()));
        safeScrollToId(R.id.btnSendMessage);
        onView(withId(R.id.btnSendMessage)).check(matches(isDisplayed()));

        scenario.onActivity(a -> a.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT));
        onView(withId(R.id.tvTitle)).check(matches(isDisplayed()));
        safeScrollToId(R.id.etMessage);
        onView(withId(R.id.etMessage)).check(matches(isDisplayed()));
    }
}
