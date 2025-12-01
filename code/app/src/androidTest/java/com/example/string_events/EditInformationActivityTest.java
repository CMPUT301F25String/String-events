package com.example.string_events;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBackUnconditionally;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class EditInformationActivityTest {

    private ActivityScenario<UiHostActivity> sc;

    private static Intent hostIntentFor(int layoutRes) {
        Context ctx = ApplicationProvider.getApplicationContext();
        Intent i = new Intent(ctx, UiHostActivity.class);
        i.putExtra(UiHostActivity.EXTRA_LAYOUT_RES_ID, layoutRes);
        return i;
    }

    @Before
    public void setUp() {
        sc = ActivityScenario.launch(hostIntentFor(R.layout.edit_information_screen));
        SystemClock.sleep(150);
    }

    @After
    public void tearDown() {
        if (sc != null) sc.close();
    }

    @Test
    public void showsCoreWidgets() {
        onView(withId(R.id.backButton)).check(matches(isDisplayed()));
        onView(withId(R.id.et_new_name)).check(matches(isDisplayed()));

        onView(withId(R.id.et_new_email)).check(matches(isDisplayed()));

        onView(withId(R.id.et_new_phone)).check(matches(isDisplayed()));

        onView(withId(R.id.et_new_password)).check(matches(isDisplayed()));

        onView(withId(R.id.btn_done)).check(matches(isDisplayed()));
    }

    @Test
    public void backButton_doesNotCrash() {
        onView(withId(R.id.backButton)).perform(click());
        pressBackUnconditionally();
        SystemClock.sleep(120);
    }

    @Test
    public void fillForm_and_clickDone_noCrash() {
        onView(withId(R.id.et_new_name))
                .perform(clearText(), replaceText("user"), closeSoftKeyboard());
        onView(withId(R.id.et_new_email))
                .perform(clearText(), replaceText("user@example.com"), closeSoftKeyboard());
        onView(withId(R.id.et_new_phone))
                .perform(clearText(), replaceText("1234567890"), closeSoftKeyboard());
        onView(withId(R.id.et_new_password))
                .perform(clearText(), replaceText("P@ssw0rd!"), closeSoftKeyboard());

        onView(withId(R.id.btn_done)).perform(click());
        SystemClock.sleep(200);
    }
}

