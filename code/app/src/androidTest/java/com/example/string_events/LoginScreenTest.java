package com.example.string_events;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.firebase.FirebaseApp;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class LoginScreenTest {

    private static Intent makeIntent() {
        Context ctx = ApplicationProvider.getApplicationContext();
        return new Intent(ctx, LoginScreen.class)
                .putExtra("fromTest", true);
    }

    @Before
    public void init() {
        Context ctx = ApplicationProvider.getApplicationContext();
        if (FirebaseApp.getApps(ctx).isEmpty()) {
            FirebaseApp.initializeApp(ctx);
        }
    }

    private static ViewAction waitForIdle(long ms) {
        return new ViewAction() {
            @Override public Matcher<View> getConstraints() { return isRoot(); }
            @Override public String getDescription() { return "idle " + ms + " ms"; }
            @Override public void perform(UiController ui, View v) { ui.loopMainThreadForAtLeast(ms); }
        };
    }
    private static void ensureReady() { onView(isRoot()).perform(waitForIdle(120)); }

    @Test
    public void showsCoreWidgets() {
        try (ActivityScenario<LoginScreen> sc = ActivityScenario.launch(makeIntent())) {
            ensureReady();
            onView(withId(R.id.toolbar)).check(matches(isDisplayed()));
            onView(withId(R.id.btnUser)).check(matches(isDisplayed()));
            onView(withId(R.id.btnAdmin)).check(matches(isDisplayed()));
            onView(withId(R.id.etEmail)).check(matches(isDisplayed()));
            onView(withId(R.id.etPassword)).check(matches(isDisplayed()));
            onView(withId(R.id.btnSignIn)).check(matches(isDisplayed()));
            onView(withId(R.id.btnSignUp)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void tapRoleButtons_doNotCrash() {
        try (ActivityScenario<LoginScreen> sc = ActivityScenario.launch(makeIntent())) {
            ensureReady();
            onView(withId(R.id.btnAdmin)).perform(click());
            onView(withId(R.id.btnUser)).perform(click());
            onView(isRoot()).perform(waitForIdle(80));
        }
    }

    @Test
    public void enterCredentials_and_signIn_doNotCrash() {
        try (ActivityScenario<LoginScreen> sc = ActivityScenario.launch(makeIntent())) {
            ensureReady();
            onView(withId(R.id.etEmail)).perform(replaceText("tester@example.com"));
            onView(withId(R.id.etPassword)).perform(replaceText("P@ssw0rd!"));
            onView(withId(R.id.btnSignIn)).perform(click());
            onView(isRoot()).perform(waitForIdle(150));
        }
    }

    @Test
    public void clickSignUp_doNotCrash() {
        try (ActivityScenario<LoginScreen> sc = ActivityScenario.launch(makeIntent())) {
            ensureReady();
            onView(withId(R.id.btnSignUp)).perform(click());
            onView(isRoot()).perform(waitForIdle(120));
        }
    }
}
