package com.example.string_events;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBackUnconditionally;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.Visibility;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.content.Intent;
import android.view.View;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.PerformException;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.hamcrest.Matcher;
import static org.hamcrest.Matchers.anyOf;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class LotteryBeforeRollLayoutTest {

    @Rule
    public ActivityScenarioRule<UiHostActivity> rule =
            new ActivityScenarioRule<>(
                    new Intent(ApplicationProvider.getApplicationContext(), UiHostActivity.class)
                            .putExtra(UiHostActivity.EXTRA_LAYOUT_RES_ID, R.layout.lottery_before_roll )
            );

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
    private static ViewAction idle(long ms) {
        return new ViewAction() {
            @Override public Matcher<View> getConstraints() { return isRoot(); }
            @Override public String getDescription() { return "idle " + ms + " ms"; }
            @Override public void perform(UiController ui, View v) { ui.loopMainThreadForAtLeast(ms); }
        };
    }
    private static Matcher<View> shown() {
        return anyOf(isDisplayed(), withEffectiveVisibility(Visibility.VISIBLE));
    }

    @Test
    public void showsCoreWidgets() {
        onView(isRoot()).perform(idle(120));

        onView(withId(R.id.btnBack)).check(matches(shown()));
        onView(withId(R.id.tvTitle)).check(matches(shown()));
        onView(withId(R.id.cardBanner)).check(matches(shown()));
        onView(withId(R.id.imgBanner)).check(matches(shown()));

        onView(withId(R.id.tvEventName)).perform(safeScrollTo()).check(matches(shown()));
        onView(withId(R.id.ivClock)).perform(safeScrollTo()).check(matches(shown()));
        onView(withId(R.id.tvTime)).perform(safeScrollTo()).check(matches(shown()));
        onView(withId(R.id.ivLoc)).perform(safeScrollTo()).check(matches(shown()));
        onView(withId(R.id.tvLocation)).perform(safeScrollTo()).check(matches(shown()));

        onView(withId(R.id.cardStatus)).perform(safeScrollTo()).check(matches(shown()));
        onView(withId(R.id.tvStatusLine1)).perform(safeScrollTo()).check(matches(shown()));
        onView(withId(R.id.tvStatusLine2)).perform(safeScrollTo()).check(matches(shown()));

        onView(withId(R.id.btnRoll)).perform(safeScrollTo()).check(matches(shown()));
    }

    @Test
    public void clicks_doNotCrash() {
        onView(withId(R.id.btnRoll)).perform(safeScrollTo(), click());

        pressBackUnconditionally();
    }
}
