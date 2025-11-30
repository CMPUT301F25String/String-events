package com.example.string_events;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class EventsScreenTest {

    private Intent makeIntent() {
        Intent i = new Intent(
                InstrumentationRegistry.getInstrumentation().getTargetContext(),
                UiHostActivity.class);
        i.putExtra(UiHostActivity.EXTRA_LAYOUT_RES_ID, R.layout.events_screen);
        return i;
    }

    @Test
    public void launch_showsCoreViews() {
        try (ActivityScenario<UiHostActivity> sc = ActivityScenario.launch(makeIntent())) {
            onView(withId(R.id.header_container)).check(matches(isDisplayed()));
            onView(withId(R.id.tv_title)).check(matches(isDisplayed()));
            onView(withId(R.id.tv_subtitle)).check(matches(isDisplayed()));
            onView(withId(R.id.layout_filters)).check(matches(isDisplayed()));
            onView(withId(R.id.list)).check(matches(isDisplayed()));
            onView(withId(R.id.bottomBar)).check(matches(isDisplayed()));
        }
    }
}
