package com.example.string_events;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.firebase.FirebaseApp;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(AndroidJUnit4.class)
public class RegisterScreenTest {

    private Intent makeIntent() {
        Context ctx = ApplicationProvider.getApplicationContext();
        return new Intent(ctx, RegisterScreen.class)
                .putExtra("fromTest", true);
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
        try (ActivityScenario<RegisterScreen> sc = ActivityScenario.launch(makeIntent())) {
            onView(withId(R.id.toolbar)).check(matches(isDisplayed()));
            onView(withId(R.id.etFullName)).check(matches(isDisplayed()));
            onView(withId(R.id.etEmail)).check(matches(isDisplayed()));
            onView(withId(R.id.etPassword)).check(matches(isDisplayed()));
            onView(withId(R.id.etPhone)).check(matches(isDisplayed()));
            onView(withId(R.id.btnRegister)).check(matches(isDisplayed()));
            onView(withId(R.id.btnSignIn)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void fillForm_and_clickRegister_doNotCrash() {
        try (ActivityScenario<RegisterScreen> sc = ActivityScenario.launch(makeIntent())) {
            onView(withId(R.id.etFullName)).perform(replaceText("Riley Tester"), closeSoftKeyboard());
            onView(withId(R.id.etEmail)).perform(replaceText("riley@test.com"), closeSoftKeyboard());
            onView(withId(R.id.etPassword)).perform(replaceText("P@ssw0rd!"), closeSoftKeyboard());
            onView(withId(R.id.etPhone)).perform(replaceText("1234567890"), closeSoftKeyboard());

            onView(withId(R.id.btnRegister)).perform(click());
        }
    }

    @Test
    public void clickSignIn_doNotCrash_andBackSafe() {
        try (ActivityScenario<RegisterScreen> sc = ActivityScenario.launch(makeIntent())) {
            onView(withId(R.id.btnSignIn)).perform(click());
            try { pressBack(); } catch (Throwable ignored) {}
        }
    }
}
