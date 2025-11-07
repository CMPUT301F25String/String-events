package com.example.string_events;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.IdRes;
import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.espresso.NoMatchingViewException;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.action.GeneralLocation;
import androidx.test.espresso.action.GeneralSwipeAction;
import androidx.test.espresso.action.Press;
import androidx.test.espresso.action.Swipe;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.hamcrest.Matcher;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class EventDetailFragmentTest {

    private static ViewAction waitForIdle(long ms) {
        return new ViewAction() {
            @Override public Matcher<View> getConstraints() { return isRoot(); }
            @Override public String getDescription() { return "wait for idle " + ms + "ms"; }
            @Override public void perform(UiController ui, View view) {
                ui.loopMainThreadForAtLeast(ms);
            }
        };
    }

    private static void ensureReady() {
        onView(isRoot()).perform(waitForIdle(150));
    }

    private static void swipeUp() {
        onView(isRoot()).perform(new GeneralSwipeAction(
                Swipe.SLOW,
                GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER,
                Press.FINGER
        ));
    }

    private static void swipeToReveal(@IdRes int viewId, int maxSwipes) {
        for (int i = 0; i < maxSwipes; i++) {
            try {
                onView(withId(viewId)).check(matches(isDisplayed()));
                return;
            } catch (AssertionError | NoMatchingViewException ignored) {
                swipeUp();
                onView(isRoot()).perform(waitForIdle(100));
            }
        }
        onView(withId(viewId)).check(matches(isDisplayed()));
    }
    // ---------------------------------------------------

    private FragmentScenario<EventDetailFragment> launch() {
        Bundle args = new Bundle();
        args.putBoolean("fromTest", true);
        args.putString("event_id", "test-event");
        return FragmentScenario.launchInContainer(
                EventDetailFragment.class,
                args,
                android.R.style.Theme_Material_Light_NoActionBar
        );
    }

    @Test
    public void showsCoreWidgets() {
        try (FragmentScenario<EventDetailFragment> ignored = launch()) {
            ensureReady();

            onView(withId(R.id.back_button)).check(matches(isDisplayed()));
            onView(withId(R.id.tvEventTitle)).check(matches(isDisplayed()));
            onView(withId(R.id.tvLocation)).check(matches(isDisplayed()));
            onView(withId(R.id.ivEventImage)).check(matches(isDisplayed()));
            onView(withId(R.id.spots_taken)).check(matches(isDisplayed()));
            onView(withId(R.id.waiting_list)).check(matches(isDisplayed()));

            swipeToReveal(R.id.tvDateLine, 3);
            swipeToReveal(R.id.tvTimeLine, 3);
            swipeToReveal(R.id.tvAddress,  3);
            swipeToReveal(R.id.tvDescription, 3);
            swipeToReveal(R.id.apply_button, 3);
        }
    }

    @Test
    public void back_doesNotCrash() {
        try (FragmentScenario<EventDetailFragment> ignored = launch()) {
            ensureReady();
            onView(withId(R.id.back_button)).perform(click());
            onView(isRoot()).perform(waitForIdle(120));
        }
    }

    @Test
    public void apply_doesNotCrash() {
        try (FragmentScenario<EventDetailFragment> ignored = launch()) {
            ensureReady();
            swipeToReveal(R.id.apply_button, 4); // 把按钮“滚”到屏幕中
            onView(withId(R.id.apply_button)).perform(click());
            onView(isRoot()).perform(waitForIdle(150));
        }
    }
}
