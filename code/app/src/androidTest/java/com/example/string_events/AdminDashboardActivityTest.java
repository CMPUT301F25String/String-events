package com.example.string_events;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.firebase.FirebaseApp;

import org.junit.Rule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class AdminDashboardActivityTest {

    @Rule
    public ActivityScenarioRule<AdminDashboardActivity> rule =
            new ActivityScenarioRule<>(AdminDashboardActivity.class);

    @Before
    public void initFirebase() {
        Context ctx = ApplicationProvider.getApplicationContext();
        if (FirebaseApp.getApps(ctx).isEmpty()) {
            FirebaseApp.initializeApp(ctx);
        }
    }

    private ActivityScenario<?> launchDashboardOrHost() {
        Context ctx = ApplicationProvider.getApplicationContext();
        try {
            Intent real = new Intent(Intent.ACTION_MAIN);
            real.setComponent(new ComponentName(
                    "com.example.string_events",
                    "com.example.string_events.AdminDashboardActivity"
            ));
            return ActivityScenario.launch(real);
        } catch (Throwable ignore) {
            Intent host = new Intent(ctx, UiHostActivity.class);
            host.putExtra(UiHostActivity.EXTRA_LAYOUT_RES_ID, R.layout.admin_dashboard);
            return ActivityScenario.launch(host);
        }
    }

    @Test
    public void initialRender_displaysCoreViews() {
        onView(withId(R.id.header_container)).check(matches(isDisplayed()));
        onView(withId(R.id.tv_title)).check(matches(withText("Admin Dashboard")));
        onView(withId(R.id.tv_subtitle)).check(matches(isDisplayed()));

        onView(withId(R.id.btnEvents)).check(matches(isDisplayed()));
        onView(withId(R.id.btnProfiles)).check(matches(isDisplayed()));
        onView(withId(R.id.btnImages)).perform(scrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.btnNotifLog)).perform(scrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.btnMyProfile)).perform(scrollTo()).check(matches(isDisplayed()));
    }

    @Test
    public void scroll_reachesMyProfileCard() {
        onView(withId(R.id.btnMyProfile)).perform(scrollTo()).check(matches(isDisplayed()));
    }

    @Test
    public void subtitle_isVisible() {
        onView(withId(R.id.tv_subtitle)).check(matches(isDisplayed()));
    }
}
