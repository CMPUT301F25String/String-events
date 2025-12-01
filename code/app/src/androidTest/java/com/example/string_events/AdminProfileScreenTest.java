package com.example.string_events;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isClickable;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.ParcelFileDescriptor;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

/**
 * UI tests for admin_profile_screen.xml
 * Fix: disable system animations during this test to allow scrollTo().
 */
@RunWith(AndroidJUnit4.class)
public class AdminProfileScreenTest {

    // ---------- NEW: disable animations for stable scrollTo ----------
    @Rule
    public final DisableAnimationsRule disableAnimationsRule = new DisableAnimationsRule();

    /** JUnit Rule that disables system animations for the duration of a test class. */
    public static class DisableAnimationsRule implements org.junit.rules.TestRule {
        private String winOld = "1", tranOld = "1", animOld = "1";

        @Override
        public org.junit.runners.model.Statement apply(org.junit.runners.model.Statement base,
                                                       org.junit.runner.Description description) {
            return new org.junit.runners.model.Statement() {
                @Override public void evaluate() throws Throwable {
                    try {
                        winOld  = get("window_animation_scale");
                        tranOld = get("transition_animation_scale");
                        animOld = get("animator_duration_scale");
                        put("window_animation_scale", "0");
                        put("transition_animation_scale", "0");
                        put("animator_duration_scale", "0");
                    } catch (Throwable ignored) { /* best-effort */ }

                    try {
                        base.evaluate();
                    } finally {
                        try {
                            put("window_animation_scale", winOld);
                            put("transition_animation_scale", tranOld);
                            put("animator_duration_scale", animOld);
                        } catch (Throwable ignored) { /* best-effort */ }
                    }
                }
            };
        }

        private static String get(String key) throws Exception {
            ParcelFileDescriptor pfd = InstrumentationRegistry.getInstrumentation()
                    .getUiAutomation().executeShellCommand("settings get global " + key);
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(new FileInputStream(pfd.getFileDescriptor())))) {
                String s = br.readLine();
                return (s == null || s.trim().isEmpty()) ? "1" : s.trim();
            }
        }

        private static void put(String key, String value) throws Exception {
            InstrumentationRegistry.getInstrumentation().getUiAutomation()
                    .executeShellCommand("settings put global " + key + " " + value)
                    .close();
        }
    }
    // ---------- END: disable animations rule ----------

    /** Launch launcher activity and set our layout as content. */
    private ActivityScenario<? extends Activity> launchWithLayout() {
        Context ctx = ApplicationProvider.getApplicationContext();
        Intent launch = ctx.getPackageManager().getLaunchIntentForPackage(ctx.getPackageName());
        if (launch == null) launch = new Intent(Intent.ACTION_MAIN);
        launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        ActivityScenario<? extends Activity> sc = ActivityScenario.launch(launch);
        sc.onActivity(a -> a.setContentView(R.layout.admin_profile_screen));
        return sc;
    }

    @Test
    public void views_areDisplayed_test() {
        try (ActivityScenario<? extends Activity> sc = launchWithLayout()) {
            onView(withId(R.id.top_bar)).check(matches(isDisplayed()));
            onView(withId(R.id.btn_back)).check(matches(isDisplayed()));
            onView(withId(R.id.tv_title)).check(matches(isDisplayed()));
            onView(withId(R.id.tv_logout)).check(matches(isDisplayed()));
            onView(withId(R.id.cv_avatar_container)).perform(scrollTo()).check(matches(isDisplayed()));
            onView(withId(R.id.img_avatar)).check(matches(isDisplayed()));
            onView(withId(R.id.badge_admin)).check(matches(isDisplayed()));
            onView(withText("Name")).perform(scrollTo()).check(matches(isDisplayed()));
            onView(withId(R.id.tv_name_value)).check(matches(isDisplayed()));
            onView(withText("Email")).check(matches(isDisplayed()));
            onView(withId(R.id.tv_email_value)).check(matches(isDisplayed()));
            onView(withText("Password")).check(matches(isDisplayed()));
            onView(withId(R.id.tv_pwd_value)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void texts_and_accessibility_areCorrect_test() {
        try (ActivityScenario<? extends Activity> sc = launchWithLayout()) {
            onView(withId(R.id.tv_title)).check(matches(withText("My Profile")));
            onView(withId(R.id.btn_back)).check(matches(withContentDescription("Back")));
            onView(withId(R.id.tv_logout)).check(matches(withText("Log Out")));
            onView(withId(R.id.img_avatar)).check(matches(withContentDescription("Profile Picture")));
            onView(withId(R.id.badge_admin)).check(matches(withText("ADMIN")));
            onView(withId(R.id.tv_name_value)).check(matches(withText("Admin User")));
            onView(withId(R.id.tv_email_value)).check(matches(withText("admin@example.com")));
            onView(withId(R.id.tv_pwd_value)).check(matches(withText("••••••••")));
        }
    }

    @Test
    public void buttons_areClickable_noCrash_test() {
        try (ActivityScenario<? extends Activity> sc = launchWithLayout()) {

            java.util.function.Consumer<Integer> swipeUntilVisible = (Integer viewId) -> {
                for (int i = 0; i < 6; i++) {
                    try {
                        onView(withId(viewId)).check(matches(isDisplayed()));
                        return; // already visible
                    } catch (AssertionError | androidx.test.espresso.NoMatchingViewException e) {
                        onView(withId(android.R.id.content))
                                .perform(androidx.test.espresso.action.ViewActions.swipeUp());
                    }
                }
                onView(withId(viewId)).check(matches(isDisplayed()));
            };

            onView(withId(R.id.btn_back)).check(matches(isClickable())).perform(click());

            swipeUntilVisible.accept(R.id.tv_logout);
            onView(withId(R.id.tv_logout)).check(matches(isDisplayed())).perform(click());
        }
    }
}
