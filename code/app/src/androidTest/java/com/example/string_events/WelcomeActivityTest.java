package com.example.string_events;

import static android.content.Context.MODE_PRIVATE;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBackUnconditionally;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(AndroidJUnit4.class)
public class WelcomeActivityTest {
    Context ctx;
    SharedPreferences sharedPreferences;
    boolean remember;
    boolean changed = false;

    private Intent makeIntent() {
        ctx = ApplicationProvider.getApplicationContext();
        sharedPreferences = ctx.getSharedPreferences("userInfo", MODE_PRIVATE);
        remember = sharedPreferences.getBoolean("remember", false);
        if (remember) {
            sharedPreferences.edit().putBoolean("remember", false).apply();
            changed = true;
        }
        return new Intent(ctx, WelcomeActivity.class)
                .putExtra("fromTest", true);
    }

    @Test
    public void showsCoreWidgets() {
        try (ActivityScenario<WelcomeActivity> sc = ActivityScenario.launch(makeIntent())) {
            onView(withId(R.id.title)).check(matches(isDisplayed()));
            onView(withId(R.id.btnSignIn)).check(matches(isDisplayed()));
            onView(withId(R.id.btnRegister)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void clickSignIn_doesNotCrash() {
        try (ActivityScenario<WelcomeActivity> sc = ActivityScenario.launch(makeIntent())) {
            onView(withId(R.id.btnSignIn)).perform(click());
        }
    }

    @Test
    public void clickRegister_doesNotCrash() {
        try (ActivityScenario<WelcomeActivity> sc = ActivityScenario.launch(makeIntent())) {
            onView(withId(R.id.btnRegister)).perform(click());
        }
    }

    @Test
    public void back_doesNotCrash() {
        try (ActivityScenario<WelcomeActivity> sc = ActivityScenario.launch(makeIntent())) {
            pressBackUnconditionally();
        }
    }

    @After
    public void restoreSharedPrefs() {
        if (changed) {
            Context ctx = ApplicationProvider.getApplicationContext();
            SharedPreferences sharedPreferences = ctx.getSharedPreferences("userInfo", MODE_PRIVATE);
            sharedPreferences.edit().putBoolean("remember", !remember).apply();
        }
    }
}

