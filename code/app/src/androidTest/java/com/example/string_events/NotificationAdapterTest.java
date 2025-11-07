package com.example.string_events;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.List;


@RunWith(AndroidJUnit4.class)
public class NotificationAdapterTest {

    @Rule
    public ActivityScenarioRule<UiHostActivity> rule = new ActivityScenarioRule<>(
            new Intent(ApplicationProvider.getApplicationContext(), UiHostActivity.class)
                    .putExtra(UiHostActivity.EXTRA_LAYOUT_RES_ID, R.layout.test_notifications_recycler)
    );

    @Test
    public void recycler_binds_scrolls_and_clicks_ok() {
        rule.getScenario().onActivity(activity -> {
            RecyclerView rv = activity.findViewById(R.id.recycler_notifications);
            rv.setLayoutManager(new LinearLayoutManager(activity));

            List<String> data = Arrays.asList(
                    "Notice #1", "Notice #2", "Notice #3",
                    "Notice #4", "Notice #5", "Notice #6", "Notice #7"
            );
            rv.setAdapter(new SimpleStringAdapter(data));
        });

        onView(withId(R.id.recycler_notifications)).check(matches(isDisplayed()));

        onView(withId(R.id.recycler_notifications)).perform(scrollToPosition(0));
        onView(recyclerItemText("Notice #1")).check(matches(isDisplayed()));

        onView(withId(R.id.recycler_notifications)).perform(scrollToPosition(6));
        onView(recyclerItemText("Notice #7")).check(matches(isDisplayed()));

        onView(withId(R.id.recycler_notifications)).perform(actionOnItemAtPosition(2, click()));
    }

    private static class SimpleStringAdapter extends RecyclerView.Adapter<SimpleStringAdapter.VH> {
        private final List<String> data;
        SimpleStringAdapter(List<String> data) { this.data = data; }

        static class VH extends RecyclerView.ViewHolder {
            final TextView tv;
            VH(View itemView) {
                super(itemView);
                tv = itemView.findViewById(android.R.id.text1);
            }
        }

        @Override public VH onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(android.R.layout.simple_list_item_1, parent, false);
            return new VH(v);
        }

        @Override public void onBindViewHolder(VH holder, int position) {
            holder.tv.setText(data.get(position));
        }

        @Override public int getItemCount() { return data.size(); }
    }

    private static Matcher<View> recyclerItemText(String text) {
        return withText(text);
    }
}
