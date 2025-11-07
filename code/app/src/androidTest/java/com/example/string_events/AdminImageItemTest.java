package com.example.string_events;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.not;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(AndroidJUnit4.class)
public class AdminImageItemTest {

    private ActivityScenario<UiHostActivity> scenario;

    private void launchRow() {
        Intent i = new Intent(
                androidx.test.core.app.ApplicationProvider.getApplicationContext(),
                UiHostActivity.class
        );
        i.putExtra(UiHostActivity.EXTRA_LAYOUT_RES_ID, R.layout.item_event_image);
        scenario = ActivityScenario.launch(i);
    }

    @After
    public void tearDown() {
        if (scenario != null) scenario.close();
    }

    @Test
    public void row_coreWidgets_areVisible() {
        launchRow();

        onView(withId(R.id.img_event)).check(matches(isDisplayed()));

        onView(withId(R.id.tv_event_title))
                .check(matches(isDisplayed()))
                .check(matches(anyOf(
                        withText("Event Title"),
                        not(withText(""))
                )));
    }

    @Test
    public void clickImage_doesNotCrash() {
        launchRow();
        onView(withId(R.id.img_event)).perform(click());
    }
}
