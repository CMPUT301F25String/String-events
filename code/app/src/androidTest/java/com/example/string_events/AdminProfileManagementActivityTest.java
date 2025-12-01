package com.example.string_events;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.swipeDown;
import static androidx.test.espresso.action.ViewActions.swipeUp;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.text.IsEqualCompressingWhiteSpace.equalToCompressingWhiteSpace;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.view.View;

import androidx.annotation.IdRes;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.espresso.matcher.BoundedMatcher;

import org.junit.Rule;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;


@RunWith(AndroidJUnit4.class)
public class AdminProfileManagementActivityTest {

    @Rule
    public ActivityScenarioRule<AdminProfileManagementActivity> rule =
            new ActivityScenarioRule<>(AdminProfileManagementActivity.class);

    @Test
    public void initialRender_displaysCoreViews() {
        onView(withId(R.id.top_bar_layout)).check(matches(isDisplayed()));
        onView(withId(R.id.btnBack)).check(matches(isDisplayed()));
        onView(withText("Profile Management")).check(matches(isDisplayed()));
        onView(withId(R.id.recyclerView)).check(matches(isDisplayed()));
    }

    private static Matcher<View> atPosition(int position, Matcher<View> itemMatcher) {
        return new BoundedMatcher<View, RecyclerView>(RecyclerView.class) {
            @Override public void describeTo(Description description) {
                description.appendText("has item at position " + position + ": ");
                itemMatcher.describeTo(description);
            }
            @Override protected boolean matchesSafely(RecyclerView recyclerView) {
                RecyclerView.ViewHolder vh =
                        recyclerView.findViewHolderForAdapterPosition(position);
                if (vh == null) return false;
                return itemMatcher.matches(vh.itemView);
            }
        };
    }

    private static Matcher<View> atPositionOnView(
            int position, @IdRes int targetViewId, Matcher<View> itemMatcher) {

        return new BoundedMatcher<View, RecyclerView>(RecyclerView.class) {
            @Override public void describeTo(Description description) {
                description.appendText("has view id " + targetViewId +
                        " at position " + position + ": ");
                itemMatcher.describeTo(description);
            }
            @Override protected boolean matchesSafely(RecyclerView recyclerView) {
                RecyclerView.ViewHolder vh =
                        recyclerView.findViewHolderForAdapterPosition(position);
                if (vh == null) return false;
                View target = vh.itemView.findViewById(targetViewId);
                if (target == null) return false;
                return itemMatcher.matches(target);
            }
        };
    }

    @Test
    public void recycler_canScroll_noCrash() {
        onView(withId(R.id.recyclerView)).perform(swipeUp());
        onView(withId(R.id.recyclerView)).perform(swipeDown());
        onView(withId(R.id.recyclerView)).check(matches(isDisplayed()));
    }

    @Test
    public void row0_bindsCoreFields_and_isVisible() {
        Intent i = new Intent(
                androidx.test.platform.app.InstrumentationRegistry.getInstrumentation()
                        .getTargetContext(),
                UiHostActivity.class);
        i.putExtra(UiHostActivity.EXTRA_LAYOUT_RES_ID, R.layout.admin_profile_management_screen);

        try (ActivityScenario<UiHostActivity> sc = ActivityScenario.launch(i)) {
            sc.onActivity(host -> {
                RecyclerView rv = host.findViewById(R.id.recyclerView);
                rv.setLayoutManager(new LinearLayoutManager(host));

                ArrayList<AdminProfiles> data = new ArrayList<>();
                data.add(new AdminProfiles("Alice Zhang", "alice@test.com", "p@ssw0rd", "doc-1"));
                data.add(new AdminProfiles("Bob Lee",    "bob@test.com",   "secret",   "doc-2"));

                AdminProfileAdapter adapter = new AdminProfileAdapter(data, p -> { });
                rv.setAdapter(adapter);
            });

            onView(withId(R.id.recyclerView)).perform(scrollToPosition(0));

            onView(withId(R.id.recyclerView))
                    .check(matches(atPositionOnView(
                            0, R.id.profile_name,
                            anyOf(
                                    withText(containsString("Alice Zhang")),
                                    withText(equalToIgnoringCase("Alice Zhang")),
                                    withText(equalToCompressingWhiteSpace("Alice Zhang"))
                            ))));

            onView(withId(R.id.recyclerView))
                    .check(matches(atPositionOnView(
                            0, R.id.profile_email,
                            anyOf(
                                    withText(containsString("alice@test.com")),
                                    withText(equalToCompressingWhiteSpace("alice@test.com"))
                            ))));

            onView(withId(R.id.recyclerView))
                    .check(matches(atPositionOnView(
                            0, R.id.profile_password,
                            anyOf(
                                    withText(containsString("p@ssw0rd")),
                                    withText(equalToCompressingWhiteSpace("p@ssw0rd")),
                                    withText(equalToIgnoringCase("p@ssw0rd"))
                            ))));

            onView(withId(R.id.recyclerView))
                    .check(matches(atPosition(0, isDisplayed())));
        }
    }

    @Test
    public void backButton_clickable() {
        onView(withId(R.id.btnBack)).perform(click());
    }

    @Test
    public void orientationChange_persistsUi() {
        ActivityScenario<AdminProfileManagementActivity> scenario = rule.getScenario();

        scenario.onActivity(a ->
                a.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE));
        onView(withText("Profile Management")).check(matches(isDisplayed()));
        onView(withId(R.id.recyclerView)).check(matches(isDisplayed()));

        scenario.onActivity(a ->
                a.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT));
        onView(withText("Profile Management")).check(matches(isDisplayed()));
        onView(withId(R.id.recyclerView)).check(matches(isDisplayed()));
    }

    @Test
    public void scroll_to_last_row_and_click_doesNotCrash() {
        Intent i = new Intent(
                androidx.test.platform.app.InstrumentationRegistry.getInstrumentation()
                        .getTargetContext(),
                UiHostActivity.class);
        i.putExtra(UiHostActivity.EXTRA_LAYOUT_RES_ID, R.layout.admin_profile_management_screen);

        try (ActivityScenario<UiHostActivity> sc = ActivityScenario.launch(i)) {
            final int N = 25;

            sc.onActivity(host -> {
                RecyclerView rv = host.findViewById(R.id.recyclerView);
                rv.setLayoutManager(new LinearLayoutManager(host));

                ArrayList<AdminProfiles> data = new ArrayList<>();
                for (int idx = 0; idx < N; idx++) {
                    data.add(new AdminProfiles(
                            "User " + idx,
                            "user" + idx + "@test.com",
                            "pwd" + idx,
                            "doc-" + idx));
                }
                AdminProfileAdapter adapter = new AdminProfileAdapter(data, p -> { });
                rv.setAdapter(adapter);
            });

            onView(withId(R.id.recyclerView)).perform(scrollToPosition(N - 1));
            onView(withId(R.id.recyclerView))
                    .perform(actionOnItemAtPosition(N - 1, click()));
        }
    }
}
