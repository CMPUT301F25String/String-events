package com.example.string_events;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.allOf;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.google.firebase.FirebaseApp;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class NotificationAdapterTest {

    private static final int FIRST = 0;

    @Before
    public void ensureFirebaseIfUsed() {
        Context ctx = ApplicationProvider.getApplicationContext();
        if (FirebaseApp.getApps(ctx).isEmpty()) {
            FirebaseApp.initializeApp(ctx);
        }
    }

    @Test
    public void rowCoreWidgets_areVisible() {
        try (ActivityScenario<NotificationScreen> sc =
                     ActivityScenario.launch(NotificationScreen.class)) {

            Matcher<View> recyclerMatcher = isAssignableFromRecycler();

            onView(recyclerMatcher).check(matches(isDisplayed()));

            Matcher<View> row0 = nthChildOf(recyclerMatcher, FIRST);

            onView(allOf(withId(R.id.imgStatus), isDescendantOfA(row0)))
                    .check(matches(isDisplayed()));
            onView(allOf(withId(R.id.imgThumb), isDescendantOfA(row0)))
                    .check(matches(isDisplayed()));
            onView(allOf(withId(R.id.tvMessage), isDescendantOfA(row0)))
                    .check(matches(isDisplayed()));
            onView(allOf(withId(R.id.tvEventName), isDescendantOfA(row0)))
                    .check(matches(isDisplayed()));
            onView(allOf(withId(R.id.imgOpen), isDescendantOfA(row0)))
                    .check(matches(isDisplayed()));
        }
    }

    @Test
    public void clickOpen_onFirstRow_doesNotCrash() {
        try (ActivityScenario<NotificationScreen> sc =
                     ActivityScenario.launch(NotificationScreen.class)) {

            Matcher<View> row0 = nthChildOf(isAssignableFromRecycler(), FIRST);

            onView(allOf(withId(R.id.imgOpen), isDescendantOfA(row0)))
                    .perform(click());
        }
    }


    private static Matcher<View> isAssignableFromRecycler() {
        return new TypeSafeMatcher<View>() {
            @Override public void describeTo(Description description) {
                description.appendText("is assignable from RecyclerView");
            }
            @Override protected boolean matchesSafely(View view) {
                return view instanceof RecyclerView && view.isShown();
            }
        };
    }

    public static Matcher<View> nthChildOf(final Matcher<View> parentMatcher, final int childPosition) {
        return new TypeSafeMatcher<View>() {
            @Override public void describeTo(Description description) {
                description.appendText("nth child of parent at position " + childPosition + " ");
                parentMatcher.describeTo(description);
            }
            @Override protected boolean matchesSafely(View view) {
                if (!(view.getParent() instanceof ViewGroup)) return false;
                ViewGroup parent = (ViewGroup) view.getParent();
                if (!parentMatcher.matches(parent)) return false;

                int visibleIndex = 0;
                for (int i = 0; i < parent.getChildCount(); i++) {
                    View child = parent.getChildAt(i);
                    if (child.getVisibility() == View.VISIBLE) {
                        if (visibleIndex == childPosition) {
                            return child == view;
                        }
                        visibleIndex++;
                    }
                }
                return false;
            }
        };
    }
}
