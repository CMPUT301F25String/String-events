package com.example.string_events;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.not;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.hamcrest.Matcher;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class LotteryAfterRollLayoutTest {

    private static int rid(String name) {
        Context app = ApplicationProvider.getApplicationContext();
        return app.getResources().getIdentifier(name, "id", app.getPackageName());
    }

    private void launch() {
        Intent i = new Intent(ApplicationProvider.getApplicationContext(), UiHostActivity.class);
        i.putExtra(UiHostActivity.EXTRA_LAYOUT_RES_ID, R.layout.lottery_after_roll);
        i.putExtra("fromTest", true);
        ActivityScenario.launch(i);
    }

    @Test
    public void showsCoreWidgets_andRollDisabled() {
        launch();

        onView(withId(rid("btnBack"))).check(matches(isDisplayed()));
        onView(withId(rid("tvTitle"))).check(matches(isDisplayed()));

        Matcher<View> rollBtn = anyOf(
                withId(rid("btnRoll")),
                withId(rid("roll_button")),
                withId(rid("apply_button"))
        );

        onView(rollBtn).perform(scrollTo());
        onView(rollBtn).check(matches(isDisplayed()));
        onView(rollBtn).check(matches(not(isEnabled())));
    }
}
